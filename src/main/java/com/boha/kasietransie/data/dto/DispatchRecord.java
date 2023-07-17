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
@Document(collection = "DispatchRecord")
public class DispatchRecord {
    private String _partitionKey;
    @Id
    private String _id;
    String dispatchRecordId;
    String routeLandmarkId;
    String marshalId;
    int passengers;
    String ownerId;
    String created;
    Position position;
    //String geoHash;
    String landmarkName;
    String marshalName;
    String routeName;
    String routeId;
    String vehicleId;
    String vehicleArrivalId;
    String vehicleReg;
    String associationId;
    String associationName;
    boolean dispatched;

    private static final Logger logger = Logger.getLogger(DispatchRecord.class.getSimpleName());
    private static final String XX = E.COFFEE + E.COFFEE + E.COFFEE;

    public static void createIndex(MongoDatabase db) {
        MongoCollection<org.bson.Document> dbCollection =
                db.getCollection(DispatchRecord.class.getSimpleName());

        dbCollection.createIndex(
                Indexes.ascending( "landmarkId", "created"));

        dbCollection.createIndex(
                Indexes.ascending( "vehicleId", "created"));

        dbCollection.createIndex(
                Indexes.ascending( "marshalId", "created"));

        dbCollection.createIndex(
                Indexes.ascending( "routeId", "created"));

        dbCollection.createIndex(
                Indexes.geo2dsphere("position"));


        logger.info(XX + "DispatchRecord indexes done");
    }

}
