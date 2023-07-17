package com.boha.kasietransie.data.dto;

import com.boha.kasietransie.util.E;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.logging.Logger;

@Data
@Document(collection = "RouteLandmark")
public class RouteLandmark {
    private String _partitionKey;
    @Id
    private String _id;
    String routeId;
    String routePointId;
    int index;
    int routePointIndex;
    String routeName;
    String landmarkId;
    String landmarkName;
    String created;
    String associationId;
    Position position;
    private static final Logger logger = Logger.getLogger(RouteLandmark.class.getSimpleName());
    private static final String XX = E.COFFEE + E.COFFEE + E.COFFEE;

    public static void createIndex(MongoDatabase db) {
        MongoCollection<org.bson.Document> dbCollection =
                db.getCollection(RouteLandmark.class.getSimpleName());

        dbCollection.createIndex(
                Indexes.ascending("routeId"));

        dbCollection.createIndex(
                Indexes.ascending("routeId", "landmarkId"),
                new IndexOptions().unique(true));

        dbCollection.createIndex(
                Indexes.geo2dsphere("position"));


        logger.info(XX + "RouteLandmark indexes done");
    }
}
