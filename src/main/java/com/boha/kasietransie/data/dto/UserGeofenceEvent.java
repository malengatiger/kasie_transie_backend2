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
@Document(collection = "UserGeofenceEvent")
public class UserGeofenceEvent {
    private String _partitionKey;
    @Id
    private String _id;
    String userGeofenceId;
    String landmarkId;
    String activityType;
    String action;
    String userId;
    long longDate;
    String created;
    String landmarkName;
    int confidence;
    double odometer;
    boolean moving;
    String associationId;
    Position position;
    //String geoHash;

    private static final Logger logger = Logger.getLogger(UserGeofenceEvent.class.getSimpleName());
    private static final String XX = E.COFFEE + E.COFFEE + E.COFFEE;

    public static void createIndex(MongoDatabase db) {
        MongoCollection<org.bson.Document> dbCollection =
                db.getCollection(UserGeofenceEvent.class.getSimpleName());

        dbCollection.createIndex(
                Indexes.ascending( "associationId", "created"));

        dbCollection.createIndex(
                Indexes.ascending( "landmarkId", "created"));

        dbCollection.createIndex(
                Indexes.ascending( "userId", "created"));

        dbCollection.createIndex(
                Indexes.geo2dsphere("position"));

        logger.info(XX + "UserGeofenceEvent indexes done");
    }
}
