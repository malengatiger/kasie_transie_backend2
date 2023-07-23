package com.boha.kasietransie.data.dto;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collation = "Commuter")
public class Commuter {
    String commuterId;
    String cellphone;
    String email;
    String name;
    String dateRegistered;
    String qrCodeUrl;
    String profileUrl;
    String profileThumbnail;
}
