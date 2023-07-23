package com.boha.kasietransie.data.dto;

import com.boha.kasietransie.util.E;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.logging.Logger;

@Data
@Document(collection = "Route")
public class Route {
    private String _partitionKey;
    @Id
    private String _id;
    String routeId;
    String countryId;
    String countryName;
    String name;
    String routeNumber;
    String created;
    String updated;
    String color;
    String userId;
    String userName;
    int active;
    String activationDate;
    String associationId;
    String associationName;
    String qrCodeUrl;
    RouteStartEnd routeStartEnd;
    List<CalculatedDistance> calculatedDistances;
    double heading;
    int lengthInMetres;

    private static final Logger logger = Logger.getLogger(Route.class.getSimpleName());
    private static final String XX = E.COFFEE + E.COFFEE + E.COFFEE;

    public static void createIndex(MongoDatabase db) {
        MongoCollection<org.bson.Document> dbCollection =
                db.getCollection(Route.class.getSimpleName());
        dbCollection.createIndex(
                Indexes.geo2dsphere("routeStartEnd.endCityPosition"));

        dbCollection.createIndex(
                Indexes.geo2dsphere("routeStartEnd.startCityPosition"));

        dbCollection.createIndex(
                Indexes.ascending("associationId", "name"),
                new IndexOptions().unique(true));

        dbCollection.createIndex(
                Indexes.ascending( "routeId"));

        logger.info(XX + "Route indexes done");
    }
}
