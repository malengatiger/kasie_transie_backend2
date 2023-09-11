package com.boha.kasietransie.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;

public class MyControllerGenerator extends TypeScriptGenerator {

    // 🌼 🌀🌀🌀  🍎
    static final Logger logger = LoggerFactory.getLogger(MyControllerGenerator.class);
    static final String mm = "🌀🌀🌀🍎 Controller Generator: 🍎🍎🍎🍎🍎🍎🍎🍎";
    public static void main(String[] args) {
        File directory = getDirectory("controllers");

        List<Class<?>> controllers = getClassesInPackage("com.boha.kasietransie.controllers");
        List<Class<?>> services = getClassesInPackage("com.boha.kasietransie.services");

        for (Class<?> controller : controllers) {
            StringBuilder sb = buildTopOfFile(services, controller.getSimpleName());
            Method[] methods = controller.getDeclaredMethods();
            for (Method method : methods) {
                if (!Modifier.isStatic(method.getModifiers())) {
                    if (controller.getSimpleName().contains("Data")) {
                        sb.append("@Post('").append(method.getName()).append("')\n");
                    }
                    if (controller.getSimpleName().contains("List")) {
                        sb.append("@Get('").append(method.getName()).append("')\n");
                    }
                    sb.append(getTypeScriptMethodSignature(method)).append("\n");
                }
            }
            sb.append("\n}\n");
            logger.info("\n\n" +mm+ " CONTROLLER FILE " + controller.getSimpleName());
            logger.info(sb.toString());
            writeFile(controller.getSimpleName(), directory, sb);
        }

    }


    public static StringBuilder buildTopOfFile(List<Class<?>> services, String controllerName) {
        StringBuilder sb = new StringBuilder();
        //build imports
        sb.append("""
                import { Body, Controller, Post, Get } from '@nestjs/common';
                import { ConfigService } from '@nestjs/config';
                
                """);

        String src = "src/controllers/";
        for (Class<?> service : services) {
                sb.append("import { ").append(service.getSimpleName()).append(" } from '")
                        .append(src).append(service.getSimpleName()).append("';\n");

        }

        sb.append("\n\n").append("const mm = '").append(controllerName).append("';\n\n");
        //build top of class
        sb.append("@Controller()\n");
        sb.append("export class ").append(controllerName).append(" {\n");
        sb.append("constructor(\n");
        sb.append("\tprivate configService: ConfigService,\n");

        for (Class<?> service : services) {
                String name = makeFirstCharacterLowercase(service.getSimpleName());
                String m2 = "private readonly " + name + ": " + service.getSimpleName()+  ",\n";
                sb.append(m2);

        }
        sb.append(") {}\n\n");
        logger.info(sb.toString());

        return sb;

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
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            if (rawType == List.class) {
                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                String typeArgumentString = getTypeScriptType(typeArguments[0]);
                return typeArgumentString + "[]";
            } else {
                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                List<String> typeArgumentStrings = Arrays.stream(typeArguments)
                        .map(MyControllerGenerator::getTypeScriptType)
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
