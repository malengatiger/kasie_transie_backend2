package com.boha.kasietransie.util;

import com.boha.kasietransie.data.dto.User;
import com.boha.kasietransie.data.dto.Vehicle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.json.CDL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FileToVehicles {
    private FileToVehicles(){};
    private static final Gson G = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LoggerFactory.getLogger(FileToVehicles.class);

    public static List<Vehicle> getVehiclesFromJSONFile(File file) throws IOException {
        List<Vehicle> users = new ArrayList<>();

        try {
            Path filePath = Path.of(file.getPath());
            String json = Files.readString(filePath);
            Type listType = new TypeToken<ArrayList<Vehicle>>() {
            }.getType();
            users = G.fromJson(json, listType);
            LOGGER.info("\uD83C\uDF4E\uD83C\uDF4E\uD83C\uDF4E " +
                    "Vehicle objects created: " + users.size());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }

    public static List<Vehicle> getVehiclesFromCSVFile(File file) throws IOException {
        List<Vehicle> vehicles;
        try (FileInputStream is = new FileInputStream(file)) {
            String csv = new BufferedReader(
                    new InputStreamReader(Objects.requireNonNull(is), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            String json = CDL.toJSONArray(csv).toString(2);
            Type listType = new TypeToken<ArrayList<Vehicle>>() {
            }.getType();
            vehicles = G.fromJson(json, listType);

        }
        LOGGER.info("\uD83C\uDF4E\uD83C\uDF4E\uD83C\uDF4E Vehicle objects created: " + vehicles.size());
        return vehicles;
    }
    public static List<User> getOwnersFromCSVFile(File file) throws IOException {
        List<User> users;
        try (FileInputStream is = new FileInputStream(file)) {
            String csv = new BufferedReader(
                    new InputStreamReader(Objects.requireNonNull(is), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            String json = CDL.toJSONArray(csv).toString(2);
            Type listType = new TypeToken<ArrayList<User>>() {
            }.getType();
            users = G.fromJson(json, listType);

        }
        LOGGER.info("\uD83C\uDF4E\uD83C\uDF4E\uD83C\uDF4E Vehicle objects created: " + users.size());
        return users;
    }
}
