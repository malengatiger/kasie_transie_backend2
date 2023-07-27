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
@Document(collection = "VehicleArrival")
public class VehicleArrival {
    private String _partitionKey;
    @Id
    private String _id;
    String vehicleArrivalId;
    String landmarkId;
    String landmarkName;
    Position position;
    //String geoHash;
    String created;
    String vehicleId;
    String associationId;
    String associationName;
    String vehicleReg;
    String make;
    String model;
    String ownerId;
    String ownerName;
    boolean dispatched;

    private static final Logger logger = Logger.getLogger(VehicleArrival.class.getSimpleName());
    private static final String XX = E.COFFEE + E.COFFEE + E.COFFEE;

    public static void createIndex(MongoDatabase db) {
        MongoCollection<org.bson.Document> dbCollection =
                db.getCollection(VehicleArrival.class.getSimpleName());

        dbCollection.createIndex(
                Indexes.ascending("created","landmarkId"));

        dbCollection.createIndex(
                Indexes.ascending("vehicleId"));

        dbCollection.createIndex(
                Indexes.ascending("associationId"));

        dbCollection.createIndex(Indexes.geo2dsphere("position"));

        logger.info(XX + "VehicleArrival indexes done ");
    }
}
