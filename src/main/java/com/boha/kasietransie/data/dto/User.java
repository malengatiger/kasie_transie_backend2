package com.boha.kasietransie.data.dto;

import com.boha.kasietransie.util.E;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.logging.Logger;

@Data
@Document(collection = "User")
public class User {
    private String _partitionKey;
    @Id
    private String _id;
    String userType;
    String userId;
    String firstName;
    String lastName;
    String gender;
    String countryId;
    String associationId;
    String associationName;
    String fcmToken;
    String email;
    String cellphone;
    String password;
    String countryName;
    String dateRegistered;
    String qrCodeUrl;
    String profileUrl;
    String profileThumbnail;

    private static final Logger logger = Logger.getLogger(User.class.getSimpleName());
    private static final String XX = E.COFFEE + E.COFFEE + E.COFFEE;

    public static void createIndex(MongoDatabase db) {
        MongoCollection<org.bson.Document> dbCollection =
                db.getCollection(User.class.getSimpleName());

        dbCollection.createIndex(
                Indexes.ascending("email"),
                new IndexOptions().unique(true));
        dbCollection.createIndex(
                Indexes.ascending("cellphone"),
                new IndexOptions().unique(true));

        dbCollection.createIndex(
                Indexes.ascending("associationId","lastName","firstName"),
                new IndexOptions().unique(true));

        logger.info(XX + "User indexes done; result: ");
    }
    public String getName() {
        return firstName + " " + lastName;
    }
}
