package com.boha.kasietransie.data.dto;

import com.boha.kasietransie.util.E;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import lombok.Data;
import org.bson.BsonDateTime;
import org.bson.Document;
import org.springframework.data.mongodb.core.mapping.TimeSeries;
import org.springframework.data.mongodb.core.timeseries.Granularity;

import java.util.logging.Logger;


@Data
@TimeSeries(collection = "VehicleHeartbeatTimeSeries", timeField = "timeStamp", metaField = "metaData",
        granularity = Granularity.MINUTES)

public class VehicleHeartbeatTimeSeries {
    BsonDateTime timestamp;
    HeartbeatMeta metaData;
    String associationId;
    String vehicleId;
    int count;
    //
    private static final Logger logger = Logger.getLogger(VehicleHeartbeatTimeSeries.class.getSimpleName());
    private static final String XX = E.COFFEE + E.COFFEE + E.COFFEE;

    public static void createIndex(MongoDatabase db) {
        MongoCollection<Document> dbCollection =
                db.getCollection(VehicleHeartbeatTimeSeries.class.getSimpleName());

        dbCollection.createIndex(
                Indexes.ascending("timestamp"));
        dbCollection.createIndex(
                Indexes.ascending("associationId"));
        dbCollection.createIndex(
                Indexes.ascending("vehicleId"));

        logger.info(XX + "VehicleHeartbeatTimeSeries indexes done ");
    }
}
