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
@Document(collection = "VehiclePhoto")
public class VehiclePhoto {
    private String _partitionKey;
    @Id
    private String _id;
    private String vehicleId;
    private String vehicleReg;
    private String associationId;
    private String userName;
    private String created;
    private String vehiclePhotoId;
    private String landmarkName;
    private String userId;
    private String url;
    private String thumbNailUrl;
    //private //String geoHash;
    private String landmarkId;
    private Position position;
    //
    private static final Logger logger = Logger.getLogger(VehiclePhoto.class.getSimpleName());
    private static final String XX = E.COFFEE + E.COFFEE + E.COFFEE;

    public static void createIndex(MongoDatabase db) {
        MongoCollection<org.bson.Document> dbCollection =
                db.getCollection(VehiclePhoto.class.getSimpleName());

        dbCollection.createIndex(
                Indexes.ascending( "vehicleId", "created"));

        logger.info(XX + "VehiclePhoto indexes done");
    }
}
