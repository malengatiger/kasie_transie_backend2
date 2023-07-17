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
@Document(collection = "Association")
public class Association {
    private String _partitionKey;
    @Id
    private String _id;
    String associationId;
    String cityId;
    String countryId;
    String associationName;
    int active;
    String countryName;
    String cityName;
    String dateRegistered;
    Position position;
    //String geoHash;
    String adminUserFirstName;
    String adminUserLastName;
    String userId;
    String adminCellphone;
    String adminEmail;

    private static final Logger logger = Logger.getLogger(Association.class.getSimpleName());
    private static final String XX = E.COFFEE + E.COFFEE + E.COFFEE;

    public static void createIndex(MongoDatabase db) {
        MongoCollection<org.bson.Document> dbCollection =
                db.getCollection(Association.class.getSimpleName());

        dbCollection.createIndex(
                Indexes.ascending("associationId"));

        dbCollection.createIndex(
                Indexes.ascending("countryId","associationName"),
                new IndexOptions().unique(true));

        logger.info(XX + "Association indexes done");
    }


}
