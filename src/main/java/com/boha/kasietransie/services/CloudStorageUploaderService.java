package com.boha.kasietransie.services;


import com.boha.kasietransie.util.E;
import com.google.cloud.storage.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class CloudStorageUploaderService {
    public static final Logger LOGGER = LoggerFactory.getLogger(CloudStorageUploaderService.class.getSimpleName());
    private static final Gson G = new GsonBuilder().setPrettyPrinting().create();

    @Value("${storageBucket}")
    private String bucketName;
    @Value("${projectId}")
    private String projectId;

    @Value("${cloudStorageDirectory}")
    private String cloudStorageDirectory;

    public String getSignedUrl(String objectName, String contentType  ) {
        LOGGER.info("%s%s%s getSignedUrl for cloud storage: %s".formatted(E.CHIPS, E.CHIPS, E.CHIPS, objectName));
        Storage storage = StorageOptions.newBuilder()
                .setProjectId(projectId).build().getService();
        BlobId blobId = BlobId.of(bucketName, cloudStorageDirectory
                + "/" + objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .build();
        URL vv = storage
                .signUrl(blobInfo, (365*10), TimeUnit.DAYS, Storage.SignUrlOption.withPathStyle());
        LOGGER.info(E.CHIPS+E.CHIPS+E.CHIPS +
                "  signed url acquired. Cool! " + vv.toString());
       return vv.toString();
    }

    public String uploadFile(String objectName, File file) throws IOException {

        LOGGER.info(E.CHIPS+E.CHIPS+E.CHIPS +
                " uploadFile to cloud storage: " + objectName);
        String contentType = Files.probeContentType(file.toPath());
        Storage storage = StorageOptions.newBuilder()
                .setProjectId(projectId).build().getService();
        BlobId blobId = BlobId.of(bucketName, cloudStorageDirectory
                + "/" + objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .build();

        LOGGER.info(E.CHIPS+E.CHIPS+E.CHIPS +
                " uploadFile to cloud storage, contentType: " + contentType);

        storage.createFrom(blobInfo, Paths.get(file.getPath()));
        String signedUrl = getSignedUrl(objectName,contentType);
        LOGGER.info(E.CHIPS+E.CHIPS+E.CHIPS +
                " file uploaded to cloud storage; url = "+ E.RED_APPLE + signedUrl
                + E.RED_APPLE+E.RED_APPLE);
        return signedUrl;
    }
}
