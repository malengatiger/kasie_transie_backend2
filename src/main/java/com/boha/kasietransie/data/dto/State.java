package com.boha.kasietransie.data.dto;

import com.boha.kasietransie.util.E;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.logging.Logger;

@Data
@Document(collection = "State")
public class State {
    private String _partitionKey;
    @Id
    private String _id;
    private String stateId;
    private String countryId;
    private String name;
    private String countryName;
    @JsonProperty("state_code")
    private String stateCode;
    private double latitude;
    private double longitude;
    //private //String geoHash;

    private static final Logger logger = Logger.getLogger(State.class.getSimpleName());
    private static final String XX = E.COFFEE + E.COFFEE + E.COFFEE;

    public static void createIndex(MongoDatabase db) {
        MongoCollection<org.bson.Document> dbCollection =
                db.getCollection(State.class.getSimpleName());

        dbCollection.createIndex(
                Indexes.ascending( "countryId"));

        dbCollection.createIndex(
                Indexes.geo2dsphere("position"));

//        dbCollection.createIndex(
//                Indexes.ascending("countryId","stateName"),
//                new IndexOptions().unique(true));

        logger.info(XX + "State indexes done");
    }
}