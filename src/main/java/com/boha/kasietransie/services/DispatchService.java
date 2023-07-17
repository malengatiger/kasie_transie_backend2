package com.boha.kasietransie.services;

import com.boha.kasietransie.data.BigBag;
import com.boha.kasietransie.data.CounterBag;
import com.boha.kasietransie.data.DispatchRecordList;
import com.boha.kasietransie.data.dto.*;
import com.boha.kasietransie.data.repos.*;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
public class DispatchService {
    private final DispatchRecordRepository dispatchRecordRepository;
    private final VehicleArrivalRepository vehicleArrivalRepository;
    private final VehicleDepartureRepository vehicleDepartureRepository;
    final VehicleHeartbeatRepository vehicleHeartbeatRepository;
    private final MessagingService messagingService;
    final HeartbeatService heartbeatService;
    private final MongoTemplate mongoTemplate;
    final AmbassadorPassengerCountRepository ambassadorPassengerCountRepository;
    private static final Logger logger = LoggerFactory.getLogger(DispatchService.class);


    public DispatchService(DispatchRecordRepository dispatchRecordRepository,
                           VehicleArrivalRepository vehicleArrivalRepository,
                           VehicleDepartureRepository vehicleDepartureRepository,
                           VehicleHeartbeatRepository vehicleHeartbeatRepository, MessagingService messagingService,
                           HeartbeatService heartbeatService, MongoTemplate mongoTemplate, AmbassadorPassengerCountRepository ambassadorPassengerCountRepository) {
        this.dispatchRecordRepository = dispatchRecordRepository;
        this.vehicleArrivalRepository = vehicleArrivalRepository;
        this.vehicleDepartureRepository = vehicleDepartureRepository;
        this.vehicleHeartbeatRepository = vehicleHeartbeatRepository;
        this.messagingService = messagingService;
        this.heartbeatService = heartbeatService;
        this.mongoTemplate = mongoTemplate;
        this.ambassadorPassengerCountRepository = ambassadorPassengerCountRepository;
    }

    public DispatchRecord addDispatchRecord(DispatchRecord dispatchRecord) {
        DispatchRecord rec = dispatchRecordRepository.insert(dispatchRecord);
        messagingService.sendMessage(rec);
        return rec;
    }

    public List<DispatchRecord> addDispatchRecords(DispatchRecordList dispatchRecordList) {

        List<DispatchRecord> list = new ArrayList<>();
        for (DispatchRecord dispatchRecord : dispatchRecordList.getDispatchRecords()) {
            DispatchRecord rec = addDispatchRecord(dispatchRecord);
            list.add(rec);
        }
        return list;
    }

    public List<DispatchRecord> getLandmarkDispatchRecords(String landmarkId) {
        return dispatchRecordRepository.findByRouteLandmarkId(landmarkId);
    }

    public List<DispatchRecord> getVehicleDispatchRecords(String vehicleId) {
        return dispatchRecordRepository.findByVehicleId(vehicleId);
    }

    public List<DispatchRecord> getMarshalDispatchRecords(String userId, String startDate) {
        Criteria c = Criteria.where("marshalId").is(userId)
                .and("created").gte(startDate);
        Query query = new Query(c);
        return mongoTemplate.find(query, DispatchRecord.class);
    }

    public long countMarshalDispatchRecords(String userId) {
        return mongoTemplate.count(query(where("marshalId").is(userId)), DispatchRecord.class);

    }

    public List<DispatchRecord> getAssociationDispatchRecords(String associationId) {
        return dispatchRecordRepository.findByAssociationId(associationId);
    }

    //
    public VehicleArrival addVehicleArrival(VehicleArrival vehicleArrival) {
        VehicleArrival v = vehicleArrivalRepository.insert(vehicleArrival);
        messagingService.sendMessage(v);
        return v;
    }

    public VehicleDeparture addVehicleDeparture(VehicleDeparture vehicleDeparture) {
        VehicleDeparture v = vehicleDepartureRepository.insert(vehicleDeparture);
        messagingService.sendMessage(v);
        return v;
    }

    public List<VehicleArrival> getLandmarkVehicleArrivals(String landmarkId) {
        return vehicleArrivalRepository.findByLandmarkId(landmarkId);
    }

    public List<VehicleArrival> getVehicleArrivals(String vehicleId) {
        return vehicleArrivalRepository.findByVehicleId(vehicleId);
    }

    public long countVehicleArrivals(String vehicleId) {
        return mongoTemplate.count(query(where("vehicleId").is(vehicleId)), VehicleArrival.class);
    }
    public long countVehiclePassengerCounts(String vehicleId) {
        return mongoTemplate.count(query(where("vehicleId").is(vehicleId)), AmbassadorPassengerCount.class);
    }
    public long countVehicleDepartures(String vehicleId) {
        return mongoTemplate.count(query(where("vehicleId").is(vehicleId)), VehicleDeparture.class);
    }
    public long countVehicleDeparturesByDate(String vehicleId, String startDate) {
        Query query = new Query();
        query.addCriteria(Criteria.where("vehicleId").is(vehicleId)
                .andOperator(Criteria.where("created").gte(startDate)));
        return mongoTemplate.count(query, VehicleDeparture.class);
    }

    public long countVehicleDispatches(String vehicleId) {
        return mongoTemplate.count(query(where("vehicleId").is(vehicleId)), DispatchRecord.class);
    }
    public long countVehicleArrivalsByDate(String vehicleId, String startDate) {
        Query query = new Query();
        query.addCriteria(Criteria.where("vehicleId").is(vehicleId)
                .andOperator(Criteria.where("created").gte(startDate)));
        return mongoTemplate.count(query, VehicleArrival.class);
    }
    public long countVehicleHeartbeatsByDate(String vehicleId, String startDate) {
        Query query = new Query();
        query.addCriteria(Criteria.where("vehicleId").is(vehicleId)
                .andOperator(Criteria.where("created").gte(startDate)));
        return mongoTemplate.count(query, VehicleHeartbeat.class);
    }
    public long countPassengerCountsByDate(String vehicleId, String startDate) {
        Query query = new Query();
        query.addCriteria(Criteria.where("vehicleId").is(vehicleId)
                .andOperator(Criteria.where("created").gte(startDate)));
        return mongoTemplate.count(query, AmbassadorPassengerCount.class);
    }
    public long countDispatchesByDate(String vehicleId, String startDate) {
        Query query = new Query();
        query.addCriteria(Criteria.where("vehicleId").is(vehicleId)
                .andOperator(Criteria.where("created").gte(startDate)));
        return mongoTemplate.count(query, DispatchRecord.class);
    }
    public List<VehicleArrival> getAssociationVehicleArrivals(String associationId) {
        return vehicleArrivalRepository.findByAssociationId(associationId);
    }

    public List<CounterBag> getVehicleCountsByDate(String vehicleId, String startDate) {
        Instant start = Instant.now();
        long departures = countVehicleDeparturesByDate(vehicleId, startDate);
        long dispatches = countDispatchesByDate(vehicleId, startDate);
        long arrivals = countVehicleArrivalsByDate(vehicleId, startDate);
        long heartbeats = countVehicleHeartbeatsByDate(vehicleId, startDate);
        long passCounts = countPassengerCountsByDate(vehicleId, startDate);

        List<CounterBag> list = new ArrayList<>();
        CounterBag dep = new CounterBag(departures, "VehicleDeparture");
        CounterBag dis = new CounterBag(dispatches, "DispatchRecord");
        CounterBag arr = new CounterBag(arrivals, "VehicleArrival");
        CounterBag hb = new CounterBag(heartbeats, "VehicleHeartbeat");
        CounterBag cc = new CounterBag(passCounts, "AmbassadorPassengerCount");

        list.add(dep);
        list.add(dis);
        list.add(arr);
        list.add(hb);
        list.add(cc);

        logger.info("Vehicle counts performed elapsed time: "
                + Duration.between(start, Instant.now()).toSeconds() + " seconds");


        return list;
    }
    public List<CounterBag> getVehicleCounts(String vehicleId) {
        Instant start = Instant.now();
        long departures = countVehicleDepartures(vehicleId);
        long dispatches = countVehicleDispatches(vehicleId);
        long arrivals = countVehicleArrivals(vehicleId);
        long heartbeats = heartbeatService.countVehicleHeartbeats(vehicleId);
        long passCounts = countVehiclePassengerCounts(vehicleId);

        List<CounterBag> list = new ArrayList<>();
        CounterBag dep = new CounterBag(departures, "VehicleDeparture");
        CounterBag dis = new CounterBag(dispatches, "DispatchRecord");
        CounterBag arr = new CounterBag(arrivals, "VehicleArrival");
        CounterBag hb = new CounterBag(heartbeats, "VehicleHeartbeat");
        CounterBag cc = new CounterBag(passCounts, "AmbassadorPassengerCount");

        list.add(dep);
        list.add(dis);
        list.add(arr);
        list.add(hb);
        list.add(cc);

        logger.info("Vehicle counts performed elapsed time: "
                + Duration.between(start, Instant.now()).toSeconds() + " seconds");


        return list;
    }
    //
    public List<VehicleArrival> findVehicleArrivalsByLocation(String associationId,
                                                              double latitude,
                                                              double longitude,
                                                              double radiusInKM,
                                                              int minutes,
                                                              int limit) {


        //calculate start date
        var date = DateTime.now().toDateTimeISO().minusMinutes(minutes);
        String startDate = date.toDateTimeISO().toString();
        Criteria firstOrCriteria = where("associationId")
                .is(associationId);
        Criteria secondOrCriteria = where("created")
                .gte(startDate);
        Criteria andCriteria = new Criteria().andOperator(firstOrCriteria, secondOrCriteria);

        Query query = new Query(andCriteria);
        Point searchPoint = new Point(latitude, longitude);
        NearQuery nearQuery = NearQuery.near(searchPoint);
        nearQuery.spherical(true);
        nearQuery.inKilometers();
        nearQuery.maxDistance(radiusInKM); //16 kms
        nearQuery.limit(limit); //return only 10 objects
        nearQuery.query(query);

        GeoResults<VehicleArrival> arrivals = mongoTemplate.geoNear(
                nearQuery, VehicleArrival.class,
                VehicleArrival.class.getSimpleName(), VehicleArrival.class);

        List<VehicleArrival> list = new ArrayList<>();
        for (GeoResult<VehicleArrival> arrivalGeoResult : arrivals) {
            list.add(arrivalGeoResult.getContent());
        }

        return list;
    }
    public List<VehicleDeparture> findVehicleDeparturesByLocation(String associationId,
                                                              double latitude,
                                                              double longitude,
                                                              double radiusInKM,
                                                              int minutes,
                                                              int limit) {


        //calculate start date
        var date = DateTime.now().toDateTimeISO().minusMinutes(minutes);
        String startDate = date.toDateTimeISO().toString();
        Criteria firstOrCriteria = where("associationId")
                .is(associationId);
        Criteria secondOrCriteria = where("created")
                .gte(startDate);
        Criteria andCriteria = new Criteria().andOperator(firstOrCriteria, secondOrCriteria);

        Query query = new Query(andCriteria);
        Point searchPoint = new Point(latitude, longitude);
        NearQuery nearQuery = NearQuery.near(searchPoint);
        nearQuery.spherical(true);
        nearQuery.inKilometers();
        nearQuery.maxDistance(radiusInKM);
        nearQuery.limit(limit);
        nearQuery.query(query);

        GeoResults<VehicleDeparture> arrivals = mongoTemplate.geoNear(
                nearQuery, VehicleDeparture.class,
                VehicleDeparture.class.getSimpleName(), VehicleDeparture.class);

        List<VehicleDeparture> list = new ArrayList<>();
        for (GeoResult<VehicleDeparture> departureGeoResult : arrivals) {
            list.add(departureGeoResult.getContent());
        }

        return list;
    }

    public List<VehicleDeparture> getLandmarkVehicleDepartures(String landmarkId) {
        return vehicleDepartureRepository.findByLandmarkId(landmarkId);
    }
    public List<VehicleDeparture> getOwnerVehicleDepartures(String userId, String startDate) {
        Query query = new Query();
        query.addCriteria(Criteria.where("ownerId").is(userId)
                .andOperator(Criteria.where("created").gte(startDate)));

        return mongoTemplate.find(query,VehicleDeparture.class);

    }
    public List<VehicleArrival> getOwnerVehicleArrivals(String userId,  String startDate) {
        Query query = new Query();
        query.addCriteria(Criteria.where("ownerId").is(userId)
                .andOperator(Criteria.where("created").gte(startDate)));

        return mongoTemplate.find(query,VehicleArrival.class);
    }
    public List<DispatchRecord> getOwnerDispatchRecords(String userId,  String startDate) {
        Query query = new Query();
        query.addCriteria(Criteria.where("ownerId").is(userId)
                .andOperator(Criteria.where("created").gte(startDate)));

        return mongoTemplate.find(query,DispatchRecord.class);
    }
    public List<VehicleHeartbeat> getOwnerVehicleHeartbeats(String userId, String startDate) {
        Query query = new Query();
        query.addCriteria(Criteria.where("ownerId").is(userId)
                .andOperator(Criteria.where("created").gte(startDate)));

        return mongoTemplate.find(query,VehicleHeartbeat.class);
    }
    public List<AmbassadorPassengerCount> getOwnerPassengerCounts(String userId, String startDate) {
        Query query = new Query();
        query.addCriteria(Criteria.where("ownerId").is(userId)
                .andOperator(Criteria.where("created").gte(startDate)));

        return mongoTemplate.find(query,AmbassadorPassengerCount.class);
    }
    public BigBag getOwnersBag(String userId, String startDate) {

        BigBag bag = new BigBag();
        bag.setDispatchRecords(getOwnerDispatchRecords(userId,startDate));
        bag.setVehicleArrivals(getOwnerVehicleArrivals(userId,startDate));
        bag.setVehicleDepartures(getOwnerVehicleDepartures(userId,startDate));
        bag.setVehicleHeartbeats(getOwnerVehicleHeartbeats(userId,startDate));
        bag.setPassengerCounts(getOwnerPassengerCounts(userId,startDate));
        return bag;
    }

    public List<VehicleDeparture> getVehicleDepartures(String vehicleId) {
        return vehicleDepartureRepository.findByVehicleId(vehicleId);
    }

    public List<VehicleDeparture> getAssociationVehicleDepartures(String associationId) {
        return vehicleDepartureRepository.findByAssociationId(associationId);
    }

    public String fixOwnerToPassengerCounts(String userId, String ownerId, String ownerName) {
        List<AmbassadorPassengerCount> counts = ambassadorPassengerCountRepository.findByUserId(userId);
        for (AmbassadorPassengerCount count : counts) {
            count.setOwnerId(ownerId);
            count.setOwnerName(ownerName);
        }
        ambassadorPassengerCountRepository.saveAll(counts);

        return  "Passenger counts fixed: " + counts.size();

    }

}
