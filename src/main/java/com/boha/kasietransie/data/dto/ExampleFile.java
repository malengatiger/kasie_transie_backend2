package com.boha.kasietransie.data.dto;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "ExampleFile")
public class ExampleFile {
    String type;
    String fileName;
    String downloadUrl;

    public ExampleFile(String type, String fileName, String downloadUrl) {
        this.type = type;
        this.fileName = fileName;
        this.downloadUrl = downloadUrl;
    }
}
