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
@Document(collection = "AmbassadorPassengerCount")
public class AmbassadorPassengerCount {
    private String _partitionKey;
    @Id
    private String _id;
    String associationId;
    String vehicleId;
    String vehicleReg;
    String created;
    String userId;
    String userName;
    String routeId;
    String routeName;
    String routeLandmarkId;
    String routeLandmarkName;
    String ownerId;
    String ownerName;
    int passengersIn;
    int passengersOut;
    int currentPassengers;
    Position position;

    private static final Logger logger = Logger.getLogger(AmbassadorPassengerCount.class.getSimpleName());
    private static final String XX = E.COFFEE + E.COFFEE + E.COFFEE;

    public static void createIndex(MongoDatabase db) {
        MongoCollection<org.bson.Document> dbCollection =
                db.getCollection(AmbassadorPassengerCount.class.getSimpleName());

        dbCollection.createIndex(
                Indexes.ascending( "created"));

        dbCollection.createIndex(
                Indexes.ascending( "associationId"));

        dbCollection.createIndex(
                Indexes.ascending(  "routeId"));

        dbCollection.createIndex(
                Indexes.ascending(  "vehicleId"));


        logger.info(XX + "AmbassadorPassengerCount indexes done");
    }
}
