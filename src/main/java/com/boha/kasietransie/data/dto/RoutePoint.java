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
@Document(collection = "RoutePoint")
public class RoutePoint {
    private String _partitionKey;
    @Id
    private String _id;
    private String routePointId;
    double latitude;
    double longitude;
    double heading;
    int index;
    String created;
    String routeId;
    String associationId;
    String routeName;
    Position position;
    //String geoHash;

    private static final Logger logger = Logger.getLogger(RoutePoint.class.getSimpleName());
    private static final String XX = E.COFFEE + E.COFFEE + E.COFFEE;

    public static void createIndex(MongoDatabase db) {
        MongoCollection<org.bson.Document> dbCollection =
                db.getCollection(RoutePoint.class.getSimpleName());

        dbCollection.createIndex(
                Indexes.ascending("routeId"));

        dbCollection.createIndex(
                Indexes.ascending( "landmarkId"));

        dbCollection.createIndex(
                Indexes.geo2dsphere("position"));

        logger.info(XX + "RoutePoint  \uD83D\uDECE indexes done");
    }
}
