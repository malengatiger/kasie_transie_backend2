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
@Document(collection = "CommuterRequest")

public class CommuterRequest {
    private String _partitionKey;
    @Id
    private String _id;
    String commuterRequestId;
    String commuterId;
    String dateRequested;
    String dateNeeded;

    Position currentPosition;

    String routeId;
    String routeName;

    String routeLandmarkId;
    String routeLandmarkName;

    int routePointIndex;
    int numberOfPassengers;

    double distanceToRouteLandmarkInMetres;
    double distanceToRoutePointInMetres;

    String associationId;
    boolean scanned;

    String destinationCityId;
    String destinationCityName;
    String originCityId;
    String originCityName;

    private static final Logger logger = Logger.getLogger(CommuterRequest.class.getSimpleName());
    private static final String XX = E.COFFEE + E.COFFEE + E.COFFEE;

    public static void createIndex(MongoDatabase db) {
        MongoCollection<org.bson.Document> dbCollection =
                db.getCollection(CommuterRequest.class.getSimpleName());

        dbCollection.createIndex(
                Indexes.ascending("dateRequested","commuterId"));

        dbCollection.createIndex(
                Indexes.ascending("routeId"));

        dbCollection.createIndex(
                Indexes.ascending("routeLandmarkId"));


        logger.info(XX + "CommuterRequest indexes done; result: ");
    }
}
