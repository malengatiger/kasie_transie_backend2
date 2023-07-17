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
@Document(collection = "LocationRequest")
public class LocationRequest {
    private String _partitionKey;
    @Id
    private String _id;
    String associationId;
    String vehicleId;
    String vehicleReg;
    String created;
    String userId;
    String userName;


    private static final Logger logger = Logger.getLogger(LocationRequest.class.getSimpleName());
    private static final String XX = E.COFFEE + E.COFFEE + E.COFFEE;

    public static void createIndex(MongoDatabase db) {
        MongoCollection<org.bson.Document> dbCollection =
                db.getCollection(LocationRequest.class.getSimpleName());

        dbCollection.createIndex(
                Indexes.ascending( "associationId", "created"));

        logger.info(XX + "LocationRequest indexes done");
    }
}
