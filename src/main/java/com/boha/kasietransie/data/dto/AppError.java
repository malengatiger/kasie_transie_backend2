package com.boha.kasietransie.data.dto;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "AppError")
public class AppError {
    String appErrorId;
    String errorMessage;
    String manufacturer;
    String model;
    String created;
    String brand;
    String userId;
    String associationId;
    String userName;
    Position errorPosition;
    //String geoHash;
    String iosName;
    String versionCodeName;
    String baseOS;
    String deviceType;
    String iosSystemName;
    String userUrl;
    String uploadedDate;
}
