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
@Document(collection = "SettingsModel")
public class SettingsModel {
    private String _partitionKey;
    @Id
    private String _id;
    String associationId;
    String locale;
    int refreshRateInSeconds;
    int themeIndex;
    int geofenceRadius;
    int commuterGeofenceRadius;
    int vehicleSearchMinutes;
    int heartbeatIntervalSeconds;
    int loiteringDelay;
    int commuterSearchMinutes;
    int commuterGeoQueryRadius;
    int vehicleGeoQueryRadius;
    int numberOfLandmarksToScan;
    int distanceFilter;
    String created;

    private static final Logger logger = Logger.getLogger(SettingsModel.class.getSimpleName());
    private static final String XX = E.COFFEE + E.COFFEE + E.COFFEE;

    public static void createIndex(MongoDatabase db) {
        MongoCollection<org.bson.Document> dbCollection =
                db.getCollection(SettingsModel.class.getSimpleName());

        dbCollection.createIndex(
                Indexes.ascending("associationId"));

        logger.info(XX + "SettingsModel indexes done");
    }
}
