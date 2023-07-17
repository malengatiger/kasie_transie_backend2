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
@Document(collection = "City")
public class City  {

    private String _partitionKey;
    @Id
    private String _id;
    private String name;
    private String cityId;
    private String country;
    private String countryId;
    private String stateId;
    private String stateName;
    private String countryName;
    private String province;
    //private //String geoHash;
    private Position position;
    private double latitude;
    private double longitude;

    public City() {
    }

    private static final Logger logger = Logger.getLogger(City.class.getSimpleName());
    private static final String XX = E.COFFEE + E.COFFEE + E.COFFEE;

    public static void createIndex(MongoDatabase db) {
        MongoCollection<org.bson.Document> dbCollection =
                db.getCollection(City.class.getSimpleName());

        dbCollection.createIndex(
                Indexes.geo2dsphere("position"));

        dbCollection.createIndex(
                Indexes.ascending("countryId", "stateId", "name"),
                new IndexOptions().unique(true));

        logger.info(XX + "City indexes done");
    }
}

