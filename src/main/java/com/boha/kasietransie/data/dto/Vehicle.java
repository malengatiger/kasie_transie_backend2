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
@Document(collection = "Vehicle")
public class Vehicle {
    private String _partitionKey;
    @Id
    private String _id;
    String ownerId;
    String cellphone;
    String vehicleId;
    String associationId;
    String countryId;
    String ownerName;
    String associationName;
    String vehicleReg;
    String model;
    String make;
    String year;
    int passengerCapacity;
    int active;
    String created;
    String updated;
    String dateInstalled;
    String qrCodeUrl;

    private static final Logger logger = Logger.getLogger(Vehicle.class.getSimpleName());
    private static final String XX = E.COFFEE + E.COFFEE + E.COFFEE;

    public static void createIndex(MongoDatabase db) {
        MongoCollection<org.bson.Document> dbCollection =
                db.getCollection(Vehicle.class.getSimpleName());

        dbCollection.createIndex(
                Indexes.ascending("countryId"));

        dbCollection.createIndex(
                Indexes.ascending("associationId"));

        dbCollection.createIndex(
                Indexes.ascending("ownerId"));

        dbCollection.createIndex(
                Indexes.ascending("associationId","vehicleReg"),
                new IndexOptions().unique(true));
        logger.info(XX + "Vehicle indexes done ");
    }

}
