package com.boha.kasietransie.services;

import com.boha.kasietransie.data.AssociationHeartbeatAggregationResult;
import com.boha.kasietransie.data.VehicleHeartbeatAggregationResult;
import com.boha.kasietransie.data.dto.HeartbeatMeta;
import com.boha.kasietransie.data.dto.VehicleHeartbeatTimeSeries;
import com.boha.kasietransie.data.repos.VehicleHeartbeatTimeSeriesRepository;
import com.boha.kasietransie.util.CustomResponse;
import com.boha.kasietransie.util.E;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.TimeSeriesGranularity;
import com.mongodb.client.model.TimeSeriesOptions;
import lombok.RequiredArgsConstructor;
import org.bson.BsonDateTime;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TimeSeriesService {
    final MongoTemplate mongoTemplate;
    final VehicleHeartbeatTimeSeriesRepository vehicleHeartbeatTimeSeriesRepository;
    private static final Logger logger = LoggerFactory.getLogger(TimeSeriesService.class);
    private static final Gson G = new GsonBuilder().setPrettyPrinting().create();

    public CustomResponse buildTimeSeries(String collectionName, String timeField, String metaField) {
        MongoDatabase db = mongoTemplate.getDb();

        CreateCollectionOptions options = new CreateCollectionOptions();
        TimeSeriesOptions tso = new TimeSeriesOptions(timeField);
        tso.metaField(metaField);
        tso.granularity(TimeSeriesGranularity.MINUTES);
        options.timeSeriesOptions(tso);

        db.createCollection(collectionName, options);
        CustomResponse cr = new CustomResponse(200, "TimeSeries: " + collectionName + " has been created",
                DateTime.now().toDateTimeISO().toString());
        logger.info(E.FERN + E.FERN + E.FERN + E.FERN + " TimeSeries collection added to MongoDB: " + collectionName);
        return cr;
    }


    public CustomResponse addHeartbeatTimeSeries(String associationId, String vehicleId, String vehicleReg) {
//        MongoDatabase db = mongoTemplate.getDb();
//        MongoCollection<Document> collection = db.getCollection("VehicleHeartbeatTimeSeries");

        HeartbeatMeta hbm = new HeartbeatMeta();
        hbm.setVehicleReg(vehicleReg);
        hbm.setVehicleId(vehicleId);
        hbm.setAssociationId(associationId);
        //
        VehicleHeartbeatTimeSeries series = new VehicleHeartbeatTimeSeries();
        series.setCount(1);
        series.setMetaData(hbm);
        series.setTimestamp(new BsonDateTime(System.currentTimeMillis()));
        series.setAssociationId(associationId);
        series.setVehicleId(vehicleId);
        series.setAssociationId(associationId);

        VehicleHeartbeatTimeSeries m = vehicleHeartbeatTimeSeriesRepository.insert(series);
        logger.info(E.FERN + E.FERN + E.FERN + E.FERN + " record added to VehicleHeartbeatTimeSeries: id: "
                + m.getTimestamp() + " " + E.RED_APPLE);
        return new CustomResponse(200, "added to VehicleHeartbeatTimeSeries: ",
                DateTime.now().toDateTimeISO().toString());
    }


    public List<VehicleHeartbeatAggregationResult> aggregateVehicleHeartbeatData(String vehicleId, String startDate) {
        try {
            BsonDateTime bst = new BsonDateTime(DateTime.parse(startDate).getMillis());
//            Date bsonDate = new Date(bst.getValue());
//            String stringDate = bsonDate.toString();
            Aggregation aggregation = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("timestamp").gte(bst)
                            .andOperator(Criteria.where("vehicleId").is(vehicleId))), // Add filtering based on startDate

                    Aggregation.project()
                            .and(DateOperators.DateToParts.datePartsOf("$timestamp")).as("date")
                            .andExpression("$count").as("count")
                            .andExpression("$metaData").as("meta")
                            .andExpression("$vehicleId").as("vehicleId"),

                    Aggregation.group(
                            Fields.from(
                                    Fields.field("year", "$date.year"),
                                    Fields.field("month", "$date.month"),
                                    Fields.field("day", "$date.day"),
                                    Fields.field("hour", "$date.hour"),
                                    Fields.field("vehicleId", "$vehicleId")

                            )
                    ).sum("$count").as("total")
            );

            AggregationResults<VehicleHeartbeatAggregationResult> results = mongoTemplate.aggregate(
                    aggregation, "VehicleHeartbeatTimeSeries", VehicleHeartbeatAggregationResult.class
            );

            List<VehicleHeartbeatAggregationResult> vehicleHeartbeatAggregationResults = results.getMappedResults();
            logger.info("HeartbeatAggregationResult: " + vehicleHeartbeatAggregationResults.size());
            if (!vehicleHeartbeatAggregationResults.isEmpty()) {
                for (VehicleHeartbeatAggregationResult result : vehicleHeartbeatAggregationResults) {
                    logger.info(E.RED_DOT + " HeartbeatAggregationResult: " + G.toJson(result));

                }
            }
            logger.info(E.RED_DOT + " Total aggregates: " + vehicleHeartbeatAggregationResults.size());
            return vehicleHeartbeatAggregationResults;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public List<AssociationHeartbeatAggregationResult> aggregateAssociationHeartbeatData(
            String associationId, String startDate) {
        try {
            BsonDateTime bst = new BsonDateTime(DateTime.parse(startDate).getMillis());
            Aggregation aggregation = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("timestamp").gte(bst)
                            .andOperator(Criteria.where("associationId").is(associationId))), // Add filtering based on startDate
                    Aggregation.project()
                            .and(DateOperators.DateToParts.datePartsOf("$timestamp")).as("date")
                            .andExpression("$count").as("count")
                            .andExpression("$metaData").as("meta"),
//                    Aggregation.match(Criteria.where("associationId").is(associationId)),
                    Aggregation.group(
                            Fields.from(
                                    Fields.field("year", "$date.year"),
                                    Fields.field("month", "$date.month"),
                                    Fields.field("day", "$date.day"),
                                    Fields.field("hour", "$date.hour"),
                                    Fields.field("associationId", "$meta.associationId")

                            )
                    ).sum("$count").as("total")
            );

            AggregationResults<AssociationHeartbeatAggregationResult> results = mongoTemplate.aggregate(
                    aggregation, "VehicleHeartbeatTimeSeries", AssociationHeartbeatAggregationResult.class
            );

            List<AssociationHeartbeatAggregationResult> associationHeartbeatAggregationResults = results.getMappedResults();
            logger.info("AssociationHeartbeatAggregationResult: " + associationHeartbeatAggregationResults.size());
            if (!associationHeartbeatAggregationResults.isEmpty()) {
                for (AssociationHeartbeatAggregationResult result : associationHeartbeatAggregationResults) {
                    logger.info(E.RED_DOT + " AssociationHeartbeatAggregationResult: " + G.toJson(result));

                }
            }
            logger.info(E.RED_DOT + " Total aggregates: " + associationHeartbeatAggregationResults.size());
            return associationHeartbeatAggregationResults;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

}
