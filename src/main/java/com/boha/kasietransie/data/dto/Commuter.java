package com.boha.kasietransie.data.dto;

import com.boha.kasietransie.util.E;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.logging.Logger;

@Data
@Document(collation = "Commuter")
public class Commuter {
    private String _partitionKey;
    @Id
    private String _id;
    String commuterId;
    String cellphone;
    String email;
    String name;
    String dateRegistered;
    String qrCodeUrl;
    String profileUrl;
    String profileThumbnail;
    String countryId;
    String password;
    String gender;

    private static final Logger logger = Logger.getLogger(Commuter.class.getSimpleName());
    private static final String XX = E.COFFEE + E.COFFEE + E.COFFEE;

    public static void createIndex(MongoDatabase db) {
        MongoCollection<org.bson.Document> dbCollection =
                db.getCollection(Commuter.class.getSimpleName());
        dbCollection.createIndex(
                Indexes.ascending( "countryId"));

        logger.info(XX + "Commuter indexes done");
    }
}
