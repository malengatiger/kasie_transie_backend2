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
@Document(collection = "CalculatedDistance")
public class CalculatedDistance {
    private String _partitionKey;
    @Id
    private String _id;
    String routeName;
    String routeId;
    String fromLandmark;
    String toLandmark;
    String fromLandmarkId;
    String toLandmarkId;
    int index;
    double distanceInMetres;
    double distanceFromStart;
    int fromRoutePointIndex;
    int toRoutePointIndex;

    private static final Logger logger = Logger.getLogger(CalculatedDistance.class.getSimpleName());
    private static final String XX = E.COFFEE + E.COFFEE + E.COFFEE;

    public static void createIndex(MongoDatabase db) {
        MongoCollection<org.bson.Document> dbCollection =
                db.getCollection(CalculatedDistance.class.getSimpleName());

        dbCollection.createIndex(
                Indexes.ascending("routeId"));


        logger.info(XX + "CalculatedDistance  \uD83D\uDECE indexes done");
    }
}
