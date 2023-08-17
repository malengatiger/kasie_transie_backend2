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
@Document(collection = "RouteAssignment")
public class RouteAssignment {
    private String _partitionKey;
    @Id
    private String _id;
    String associationId;
    String routeId;
    String vehicleId;
    int active;
    String created;
    String routeName;
    String associationName;
    String vehicleReg;

    private static final Logger logger = Logger.getLogger(RouteAssignment.class.getSimpleName());
    private static final String XX = E.COFFEE + E.COFFEE + E.COFFEE;

    public static void createIndex(MongoDatabase db) {
        MongoCollection<org.bson.Document> dbCollection =
                db.getCollection(RouteAssignment.class.getSimpleName());

        dbCollection.createIndex(
                Indexes.ascending( "vehicleId"));
        dbCollection.createIndex(
                Indexes.ascending( "routeId","vehicleId"),new IndexOptions().unique(true));

        logger.info(XX + "RouteAssignment indexes done");
    }
}
