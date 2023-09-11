package com.boha.kasietransie.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

/**
 * Generate all data models to TypeScript for use with Node/NestJS
 */
public class TypeScriptGenerator {

    // 🌼 🌀🌀🌀  🍎
    static final Logger logger = Logger.getLogger(TypeScriptGenerator.class.getSimpleName());
    public static void main(String[] args) {
        File directory = getDirectory("schemaFiles");
        if (directory == null) return; // Exit the program

        List<Class<?>> classes = getClassesInPackage("com.boha.kasietransie.data.dto");
        for (Class<?> clazz : classes) {
            generateSchemaFiles(clazz, directory);
        }
        //
        File directory2 = getDirectory("other_models");
        if (directory2 == null) return;

        List<Class<?>> classes2 = getClassesInPackage("com.boha.kasietransie.helpermodels");
        for (Class<?> clazz : classes2) {
            generateOtherFiles(clazz, directory2);
        }

    }

    public static File getDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            if (directory.mkdir()) {
                logger.info("Directory created successfully.");
            } else {
                logger.severe("Failed to create directory.");
                return null;
            }
        }
        return directory;
    }

    public static List<Class<?>> getClassesInPackage(String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = packageName.replace('.', '/');
            Enumeration<URL> resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                File directory = new File(resource.getFile());
                if (directory.exists() && directory.isDirectory()) {
                    File[] files = directory.listFiles();
                    if (files != null) {
                        handleFiles(packageName, classes, files);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        for (Class<?> aClass : classes) {
            logger.info(" \uD83C\uDF4E\uD83C\uDF4E class: " + aClass.getSimpleName());
        }
        return classes;
    }

    private static void handleFiles(String packageName, List<Class<?>> classes, File[] files) throws ClassNotFoundException {
        for (File file : files) {
            String fileName = file.getName();
            if (fileName.endsWith(".class")) {
                String className = packageName + '.' + fileName.substring(0, fileName.length() - 6);
                Class<?> clazz = Class.forName(className);
                classes.add(clazz);
            }
        }
    }

    public static void generateOtherFiles(Class<?> clazz, File directory) {
        StringBuilder sb = new StringBuilder();

        sb.append("export class ").append(clazz.getSimpleName()).append(" {\n");

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers())) {
                String mType = getTypeScriptType(field.getType());
                sb.append("\t").append(field.getName()).append(": ")
                        .append(mType).append(";\n");
            }
        }

        sb.append("}\n\n");

        logger.info("\n\n\n \uD83C\uDF00\uD83C\uDF00\uD83C\uDF00 NON_SCHEMA GENERATED CLASS: " + clazz.getSimpleName());
        logger.info(sb.toString());

        String fileName = clazz.getSimpleName() + ".ts";

        // Create the file inside the directory
        File file = new File(directory, fileName);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(sb.toString());
            logger.info("TypeScript class file generated: " + file.getName());
        } catch (IOException e) {
            logger.severe("Error writing TypeScript class file: " + e.getMessage());
        }
        logger.info("\n\n");

    }

    public static void generateSchemaFiles(Class<?> clazz, File directory) {
        StringBuilder sb = new StringBuilder();
        String m1 = "import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';\n";
        String imp = "import { Position } from './Position';\n";

        sb.append(m1);
        sb.append(imp);
        sb.append("@Schema({\n");
        sb.append("\t timestamps: true,\n");
        sb.append("\t collection: '");
        sb.append(clazz.getSimpleName()).append("', \n");
        sb.append("})\n\n");
        sb.append("export class ").append(clazz.getSimpleName()).append(" {\n");

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers())) {
                sb.append("\t@Prop()\n");
                sb.append("\t").append(field.getName()).append(": ")
                        .append(getTypeScriptType(field.getType())).append(";\n");
            }
        }

        sb.append("}\n\n");
        sb.append("export const ");
        sb.append(clazz.getSimpleName()).append("Schema = SchemaFactory.createForClass(").append(clazz.getSimpleName());
        sb.append(");\n");
        logger.info("\n\n\n \uD83C\uDF00\uD83C\uDF00\uD83C\uDF00 GENERATED SCHEMA CLASS: " + clazz.getSimpleName());
        logger.info(sb.toString());

        String fileName = clazz.getSimpleName() + ".ts";

        // Create the file inside the directory
        File file = new File(directory, fileName);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(sb.toString());
            logger.info("TypeScript class file generated: " + file.getName());
        } catch (IOException e) {
            logger.severe("Error writing TypeScript class file: " + e.getMessage());
        }
        logger.info("\n\n");

    }

    public static String getTypeScriptType(Class<?> type) {
        if (type == String.class) {
            return "string";
        } else if (type == int.class || type == Integer.class) {
            return "number";
        } else if (type == boolean.class || type == Boolean.class) {
            return "boolean";
        } else if (type.isArray()) {
            return getTypeScriptType(type.getComponentType()) + "[]";
        } else if (type.getSimpleName().contains("Position")) {
            return "Position";
        } else if (type == double.class) {
            return "number";
        } else if (type == List.class) {
            return "[]";
        } else if (type == long.class) {
            return "number";
        } else {
            return type.getSimpleName();
        }
    }


}