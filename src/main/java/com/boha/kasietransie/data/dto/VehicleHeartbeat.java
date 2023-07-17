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
@Document(collection = "VehicleHeartbeat")
public class VehicleHeartbeat {
    private String _partitionKey;
    @Id
    private String _id;
    String vehicleHeartbeatId;
    String vehicleId;
    String vehicleReg;
    String associationId;
    String ownerId;
    String ownerName;
    Position position;
    //String geoHash;
    String created;
    long  longDate;
    String  make;
    String  model;

    private static final Logger logger = Logger.getLogger(VehicleHeartbeat.class.getSimpleName());
    private static final String XX = E.COFFEE + E.COFFEE + E.COFFEE;

    public static void createIndex(MongoDatabase db) {
        MongoCollection<org.bson.Document> dbCollection =
                db.getCollection(VehicleHeartbeat.class.getSimpleName());

        dbCollection.createIndex(
                Indexes.ascending("landmarkId"));

        dbCollection.createIndex(
                Indexes.ascending("vehicleId"));

        dbCollection.createIndex(
                Indexes.ascending("associationId"));

        dbCollection.createIndex(Indexes.geo2dsphere("position"));

        logger.info(XX + "VehicleHeartbeat indexes done ");
    }
}
