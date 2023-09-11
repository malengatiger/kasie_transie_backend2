package com.boha.kasietransie.services;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class DataFileService {

    public File createZippedFile() throws IOException {
        // Create a temporary directory to store the files
        Path tempDir = Files.createTempDirectory("data");

        // Create the users.json file
        File usersFile = new File(tempDir.toFile(), "users.json");
        // Write the JSON data to the users.json file

        // Create the vehicles.json file
        File vehiclesFile = new File(tempDir.toFile(), "vehicles.json");
        // Write the JSON data to the vehicles.json file

        // Create the zip file
        File zipFile = new File(tempDir.toFile(), "data.zip");
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile))) {
            addToZip(usersFile, zipOutputStream);
            addToZip(vehiclesFile, zipOutputStream);
        }

        return zipFile;
    }

    private void addToZip(File file, ZipOutputStream zipOutputStream) throws IOException {
        ZipEntry zipEntry = new ZipEntry(file.getName());
        zipOutputStream.putNextEntry(zipEntry);
        Files.copy(file.toPath(), zipOutputStream);
        zipOutputStream.closeEntry();
    }

    public byte[] createZippedByteArray() throws IOException {
        File zipFile = createZippedFile();
        return Files.readAllBytes(zipFile.toPath());
    }
}
