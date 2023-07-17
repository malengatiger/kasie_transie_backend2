package com.boha.kasietransie.data.dto;

import com.boha.kasietransie.util.E;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.logging.Logger;

@Data
@Document(collection = "RouteUpdateRequest")
public class RouteUpdateRequest {
     String associationId;
     String routeId;
     String routeName;
     String created;
     String userId;
     String userName;

     private static final Logger logger = Logger.getLogger(RouteUpdateRequest.class.getSimpleName());
     private static final String XX = E.COFFEE + E.COFFEE + E.COFFEE;

     public static void createIndex(MongoDatabase db) {
          MongoCollection<org.bson.Document> dbCollection =
                  db.getCollection(RouteUpdateRequest.class.getSimpleName());

          dbCollection.createIndex(
                  Indexes.ascending( "associationId"));
          dbCollection.createIndex(
                  Indexes.ascending( "routeId"));

          logger.info(XX + "RouteUpdateRequest indexes done");
     }
}
