package com.boha.kasietransie.data.dto;

import com.boha.kasietransie.util.E;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@Document(collection = "Country")
public class Country {
    private String _partitionKey;
    @Id
    private String _id;
    private String countryId;
    private String name;
    private String iso3;
    private String iso2;
    @JsonProperty("phone_code")
    private String phoneCode;
    private String capital;
    private String currency;
    @JsonProperty("currency_name")
    private String currencyName;
    @JsonProperty("currency_symbol")
    private String currencySymbol;
    private String tld;
    private String region;
    private String subregion;
    private List<Timezone> timezones;
    private double latitude;
    private double longitude;
    private String emoji;
    private String emojiU;
    private Position position;
    //String geoHash;


    private static final Logger logger = Logger.getLogger(Country.class.getSimpleName());
    private static final String XX = E.COFFEE + E.COFFEE + E.COFFEE;

    public static void createIndex(MongoDatabase db) {
        MongoCollection<org.bson.Document> dbCollection =
                db.getCollection(Country.class.getSimpleName());


        dbCollection.createIndex(
                Indexes.ascending("name"),
                new IndexOptions().unique(true));

        logger.info(XX + "Country index done");
    }

}
