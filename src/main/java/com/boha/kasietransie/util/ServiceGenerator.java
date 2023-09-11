package src.main.java.com.boha.kasietransie.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ServiceGenerator extends com.boha.kasietransie.util.TypeScriptGenerator {

    // 🌼 🌀🌀🌀  🍎
    static final Logger logger = Logger.getLogger(ServiceGenerator.class.getSimpleName());
    static final String mm = "🌀🌀🌀🍎 Service Generator: 🍎🍎🍎🍎🍎🍎🍎🍎";
    public static void main(String[] args) {
        File directory = getDirectory("serviceMethods");

        List<Class<?>> services = getClassesInPackage("com.boha.kasietransie.services");

        for (Class<?> service : services) {
            List<Class<?>> parameters = getParameterTypes(service);
            HashMap<String, Class<?>> hash = new HashMap<>();
            for (Class<?> param : parameters) {
                if (!param.isPrimitive()) {
                    hash.put(param.getSimpleName(), param);
                }
            }

            List<String> paramTypes = hash.keySet().stream().toList();
            StringBuilder sb = buildTopOfFile(paramTypes, service.getSimpleName());
            Method[] methods = service.getDeclaredMethods();
            for (Method method : methods) {
                if (!Modifier.isStatic(method.getModifiers())) {
                    sb.append(getTypeScriptMethodSignature(method)).append("\n");
                }
            }
            sb.append("\n}\n");
            logger.info("\n\n" +mm+ " SERVICE FILE " + service.getSimpleName());
            logger.info(sb.toString());
            writeFile(service.getSimpleName(), directory, sb);
        }


    }


    private static List<Class<?>> getParameterTypes(Class<?> service) {
        List<Class<?>> parameters = new ArrayList<>();
        Method[] methods = service.getDeclaredMethods();
        for (Method method : methods) {
            if (!Modifier.isStatic(method.getModifiers())) {
                Class<?>[] paramTypes = method.getParameterTypes();
                parameters.addAll(List.of(paramTypes));
            }
        }
        return parameters;
    }

    public static StringBuilder buildTopOfFile(List<String> models, String clzName) {
        StringBuilder sb = new StringBuilder();
        //build imports
        sb.append("""
                import { Injectable, Logger } from '@nestjs/common';
                import { ConfigService } from '@nestjs/config';
                import { InjectModel } from '@nestjs/mongoose';
                import mongoose from 'mongoose';
                """);

        String src = "src/models/";
        for (String model : models) {
            if (isValidClass(model)) {
                sb.append("import { ").append(model).append(" } from '")
                        .append(src).append(model).append("';\n");
            }
        }
        sb.append("\n\n").append("const mm = '").append(clzName).append("';\n\n");
        //build top of class
        sb.append("@Injectable()\n");
        sb.append("export class ").append(clzName).append(" {\n");
        sb.append("constructor(\n");
        sb.append("\tprivate configService: ConfigService,\n");

        for (String model : models) {
            if (isValidClass(model)) {
                String m1 = "@InjectModel(" + model + ".name)\n";
                String name = makeFirstCharacterLowercase(model);
                String m2 = "private " + name + "Model: " + "mongoose.Model<" + model + ">,\n\n";
                sb.append(m1).append(m2);
            }
        }
        sb.append(") {}\n\n");
        logger.info(sb.toString());

        return sb;

    }

    static boolean isValidClass(String model) {
        return !model.contains("List") && !model.contains("String")
                && !model.contains("int") && !model.contains("double");
    }

    private static void writeFile(String cName, File outputDirectory, StringBuilder sb) {
        //write file
        String fileName = cName + ".ts";
        File file = new File(outputDirectory, fileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(sb.toString());
            logger.info("TypeScript class file generated: " + cName + " filePath: " + file.getPath());
        } catch (IOException e) {
            logger.info("Error writing TypeScript class file: " + e.getMessage());
        }
    }

    public static String makeFirstCharacterLowercase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        char firstChar = input.charAt(0);
        if (Character.isLowerCase(firstChar)) {
            return input;
        }
        return Character.toLowerCase(firstChar) + input.substring(1);
    }
    private static String getTypeScriptMethodSignature(Method method) {
        StringBuilder sb = new StringBuilder();
        sb.append("public async ").append(method.getName()).append("(");

        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            sb.append(parameter.getName()).append(": ").append(getTypeScriptType(parameter.getParameterizedType()));
            if (i < parameters.length - 1) {
                sb.append(", ");
            }
        }
        String type = getTypeScriptType(method.getGenericReturnType());
        sb.append("): Promise<").append(type).append("> {\n");
//        sb.append("\t//todo - implement function\n");
        if (type.contains("[]")) {
            sb.append("\treturn []; \n");
        } else {
            sb.append("\treturn null; \n");
        }
        sb.append("}");
        return sb.toString();
    }

    private static String getTypeScriptType(Type type) {
        if (type instanceof Class<?>) {
            return getTypeScriptType((Class<?>) type);
        } else if (type instanceof TypeVariable<?>) {
            return ((TypeVariable<?>) type).getName();
        } else if (type instanceof java.lang.reflect.ParameterizedType) {
            java.lang.reflect.ParameterizedType parameterizedType = (java.lang.reflect.ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            if (rawType == List.class) {
                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                String typeArgumentString = getTypeScriptType(typeArguments[0]);
                return typeArgumentString + "[]";
            } else {
                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                List<String> typeArgumentStrings = Arrays.stream(typeArguments)
                        .map(ServiceGenerator::getTypeScriptType)
                        .toList();
                String typeArgumentsString = String.join(", ", typeArgumentStrings);
                return getTypeScriptType(rawType) + "<" + typeArgumentsString + ">";
            }
        } else if (type instanceof GenericArrayType genericArrayType) {
            String elementType = getTypeScriptType(genericArrayType.getGenericComponentType());
            return elementType + "[]";
        } else {
            return "any";
        }
    }



}
