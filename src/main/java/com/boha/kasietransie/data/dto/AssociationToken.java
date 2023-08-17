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
@Document(collection = "AssociationToken")
public class AssociationToken {
    private String _partitionKey;
    @Id
    private String _id;
    String userId;
    String token;
    String created;
    String associationId;
    String associationName;

    private static final Logger logger = Logger.getLogger(AssociationToken.class.getSimpleName());
    private static final String XX = E.COFFEE + E.COFFEE + E.COFFEE;

    public static void createIndex(MongoDatabase db) {
        MongoCollection<org.bson.Document> dbCollection =
                db.getCollection(AssociationToken.class.getSimpleName());

        dbCollection.createIndex(
                Indexes.ascending("associationId"));

        logger.info(XX + "AssociationToken indexes done");
    }
}
