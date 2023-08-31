package com.boha.kasietransie.services;

import com.boha.kasietransie.data.*;
import com.boha.kasietransie.data.dto.*;
import com.boha.kasietransie.data.repos.*;
import com.boha.kasietransie.util.E;
import com.boha.kasietransie.util.Zipper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    final RouteService routeService;
    final VehicleRepository vehicleRepository;
    final RouteRepository routeRepository;
    final UserRepository userRepository;
    final RoutePointRepository routePointRepository;
    final VehicleService vehicleService;
    final AmbassadorService ambassadorService;
    private static final Logger logger = LoggerFactory.getLogger(DispatchService.class);


    public DispatchService(DispatchRecordRepository dispatchRecordRepository,
                           VehicleArrivalRepository vehicleArrivalRepository,
                           VehicleDepartureRepository vehicleDepartureRepository,
                           VehicleHeartbeatRepository vehicleHeartbeatRepository, MessagingService messagingService,
                           HeartbeatService heartbeatService, MongoTemplate mongoTemplate, AmbassadorPassengerCountRepository ambassadorPassengerCountRepository, RouteService routeService, VehicleRepository vehicleRepository, RouteRepository routeRepository, UserRepository userRepository, RoutePointRepository routePointRepository, VehicleService vehicleService, AmbassadorService ambassadorService) {
        this.dispatchRecordRepository = dispatchRecordRepository;
        this.vehicleArrivalRepository = vehicleArrivalRepository;
        this.vehicleDepartureRepository = vehicleDepartureRepository;
        this.vehicleHeartbeatRepository = vehicleHeartbeatRepository;
        this.messagingService = messagingService;
        this.heartbeatService = heartbeatService;
        this.mongoTemplate = mongoTemplate;
        this.ambassadorPassengerCountRepository = ambassadorPassengerCountRepository;
        this.routeService = routeService;
        this.vehicleRepository = vehicleRepository;
        this.routeRepository = routeRepository;
        this.userRepository = userRepository;
        this.routePointRepository = routePointRepository;
        this.vehicleService = vehicleService;
        this.ambassadorService = ambassadorService;
    }

    public List<DispatchRecord> getAssociationDispatchRecords(String associationId, String startDate) {
        Criteria c = Criteria.where("associationId").is(associationId)
                .and("created").gte(startDate);

        Query query = new Query(c).with(Sort.by("created").descending());
        return mongoTemplate.find(query, DispatchRecord.class);
    }

    public List<DispatchRecord> getRouteDispatchRecords(String routeId, String startDate) {
        Criteria c = Criteria.where("routeId").is(routeId)
                .and("created").gte(startDate);

        Query query = new Query(c).with(Sort.by("created").descending());
        return mongoTemplate.find(query, DispatchRecord.class);
    }

    public List<VehicleArrival> getAssociationVehicleArrivals(String associationId, String startDate) {
        Criteria c = Criteria.where("associationId").is(associationId)
                .and("created").gte(startDate);

        Query query = new Query(c).with(Sort.by("created").descending());
        return mongoTemplate.find(query, VehicleArrival.class);
    }

    public List<CommuterRequest> getAssociationCommuterRequests(String associationId, String startDate) {
        Criteria c = Criteria.where("associationId").is(associationId)
                .andOperator(Criteria.where("dateRequested").gte(startDate));

        Query query = new Query(c).with(Sort.by("dateRequested").descending());
        return mongoTemplate.find(query, CommuterRequest.class);
    }

    public List<VehicleArrival> getRouteVehicleArrivals(String routeId, String startDate) {
        Criteria c = Criteria.where("routeId").is(routeId)
                .and("created").gte(startDate);

        Query query = new Query(c).with(Sort.by("created").descending());
        return mongoTemplate.find(query, VehicleArrival.class);
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

    public List<DispatchRecord> getVehicleDispatchRecords(String vehicleId, String startDate) {
        Criteria c = Criteria.where("vehicleId").is(vehicleId)
                .and("created").gte(startDate);
        Query query = new Query(c);
        return mongoTemplate.find(query, DispatchRecord.class);
    }

    public List<VehicleHeartbeat> getVehicleHeartbeats(String vehicleId, String startDate) {
        Criteria c = Criteria.where("vehicleId").is(vehicleId)
                .and("created").gte(startDate);
        Query query = new Query(c);
        return mongoTemplate.find(query, VehicleHeartbeat.class);
    }

    public List<AmbassadorPassengerCount> getVehiclePassengerCounts(String vehicleId, String startDate) {
        Criteria c = Criteria.where("vehicleId").is(vehicleId)
                .and("created").gte(startDate);
        Query query = new Query(c);
        return mongoTemplate.find(query, AmbassadorPassengerCount.class);
    }

    public List<VehicleDeparture> getVehicleDepartures(String vehicleId, String startDate) {
        Criteria c = Criteria.where("vehicleId").is(vehicleId)
                .and("created").gte(startDate);
        Query query = new Query(c);
        return mongoTemplate.find(query, VehicleDeparture.class);
    }

    public List<VehicleArrival> getVehicleArrivals(String vehicleId, String startDate) {
        Criteria c = Criteria.where("vehicleId").is(vehicleId)
                .and("created").gte(startDate);
        Query query = new Query(c);
        return mongoTemplate.find(query, VehicleArrival.class);
    }

    public VehicleBag getVehicleBag(String vehicleId, String startDate) {
        VehicleBag bag = new VehicleBag();
        bag.setVehicleId(vehicleId);
        bag.setCreated(startDate);
        bag.setArrivals(getVehicleArrivals(vehicleId, startDate));
        bag.setHeartbeats(getVehicleHeartbeats(vehicleId, startDate));
        bag.setDispatchRecords(getVehicleDispatchRecords(vehicleId, startDate));
        bag.setPassengerCounts(getVehiclePassengerCounts(vehicleId, startDate));
        bag.setDepartures(getVehicleDepartures(vehicleId, startDate));

        return bag;
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

//        logger.info("Vehicle counts performed elapsed time: "
//                + Duration.between(start, Instant.now()).toSeconds() + " seconds");


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
//
//        logger.info("Vehicle counts performed elapsed time: "
//                + Duration.between(start, Instant.now()).toSeconds() + " seconds");


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

        return mongoTemplate.find(query, VehicleDeparture.class);

    }

    public List<VehicleArrival> getOwnerVehicleArrivals(String userId, String startDate) {
        Query query = new Query();
        query.addCriteria(Criteria.where("ownerId").is(userId)
                .andOperator(Criteria.where("created").gte(startDate)));

        return mongoTemplate.find(query, VehicleArrival.class);
    }

    public List<DispatchRecord> getOwnerDispatchRecords(String userId, String startDate) {
        Query query = new Query();
        query.addCriteria(Criteria.where("ownerId").is(userId)
                .andOperator(Criteria.where("created").gte(startDate)));

        return mongoTemplate.find(query, DispatchRecord.class);
    }

    public List<VehicleHeartbeat> getOwnerVehicleHeartbeats(String userId, String startDate) {
        Query query = new Query();
        query.addCriteria(Criteria.where("ownerId").is(userId)
                .andOperator(Criteria.where("created").gte(startDate)));

        return mongoTemplate.find(query, VehicleHeartbeat.class);
    }

    public List<AmbassadorPassengerCount> getOwnerPassengerCounts(String userId, String startDate) {
        Query query = new Query();
        query.addCriteria(Criteria.where("ownerId").is(userId)
                .andOperator(Criteria.where("created").gte(startDate)));

        return mongoTemplate.find(query, AmbassadorPassengerCount.class);
    }

    public BigBag getOwnersBag(String userId, String startDate) {

        BigBag bag = new BigBag();
        bag.setDispatchRecords(getOwnerDispatchRecords(userId, startDate));
        bag.setVehicleArrivals(getOwnerVehicleArrivals(userId, startDate));
        bag.setVehicleDepartures(getOwnerVehicleDepartures(userId, startDate));
        bag.setVehicleHeartbeats(getOwnerVehicleHeartbeats(userId, startDate));
        bag.setPassengerCounts(getOwnerPassengerCounts(userId, startDate));
        return bag;
    }

    public List<VehicleDeparture> getVehicleDepartures(String vehicleId) {
        return vehicleDepartureRepository.findByVehicleId(vehicleId);
    }

    public List<VehicleDeparture> getAssociationVehicleDepartures(String associationId, String startDate) {
        Criteria c = Criteria.where("associationId").is(associationId)
                .and("created").gte(startDate);
        Query query = new Query(c);
        return mongoTemplate.find(query, VehicleDeparture.class);
    }

    public AssociationBag getAssociationBag(String associationId, String startDate) {
//        logger.info(E.GLOBE + " getAssociationBag starting ...");
        AssociationBag bag = new AssociationBag();
        bag.setDepartures(getAssociationVehicleDepartures(associationId, startDate));
        bag.setHeartbeats(heartbeatService.getAssociationVehicleHeartbeats(associationId, startDate));
        bag.setDispatchRecords(getAssociationDispatchRecords(associationId, startDate));
        bag.setPassengerCounts(ambassadorService.getAssociationAmbassadorPassengerCounts(associationId, startDate));
        bag.setArrivals(getAssociationVehicleArrivals(associationId, startDate));
        bag.setCommuterRequests(getAssociationCommuterRequests(associationId, startDate));

        String sb = E.LEAF + E.LEAF + E.LEAF + " Association Bag contains:\n" + E.CHECK +
                "Arrivals: " + bag.getArrivals().size() + "\n" + E.CHECK +
                "Departures: " + bag.getDepartures().size() + "\n" + E.CHECK +
                "Commuter Requests: " + bag.getCommuterRequests().size() + "\n" + E.CHECK +
                "Dispatch Records: " + bag.getDispatchRecords().size() + "\n" + E.CHECK +
                "PassengerCounts: " + bag.getPassengerCounts().size() + "\n" + E.CHECK +
                "Heartbeats: " + bag.getHeartbeats().size() + "\n";

        logger.info(sb);
        return bag;
    }
    public AssociationCounts getAssociationCounts(String associationId, String startDate) {
//        logger.info(E.GLOBE + " getAssociationBag starting ...");
        AssociationCounts bag = new AssociationCounts();
        bag.setCreated(DateTime.now().toDateTimeISO().toString());
        Query query = new Query(Criteria.where("associationId").is(associationId));
        query.addCriteria(Criteria.where("created").gte(startDate));
        long hbCount = mongoTemplate.count(query, VehicleHeartbeat.class);
        long depCount = mongoTemplate.count(query, VehicleDeparture.class);
        long arrCount = mongoTemplate.count(query, VehicleArrival.class);
        long disCount = mongoTemplate.count(query, DispatchRecord.class);

        Query query2 = new Query(Criteria.where("associationId").is(associationId));
        query2.addCriteria(Criteria.where("dateRequested").gte(startDate));
        long comCount = mongoTemplate.count(query2, CommuterRequest.class);

        Query query3 = new Query(Criteria.where("associationId").is(associationId));
        query3.addCriteria(Criteria.where("created").gte(startDate));
        List<AmbassadorPassengerCount> ambCnts = mongoTemplate.find(query3, AmbassadorPassengerCount.class);
        int passCount = 0;
        for (AmbassadorPassengerCount ambCnt : ambCnts) {
            passCount += ambCnt.getPassengersIn();
        }

        bag.setDepartures(depCount);
        bag.setHeartbeats(hbCount);
        bag.setDispatchRecords(disCount);
        bag.setPassengerCounts(passCount);
        bag.setArrivals(arrCount);
        bag.setCommuterRequests(comCount);

        String sb = E.LEAF + E.LEAF + E.LEAF + " Association Bag contains:\n" + E.CHECK +
                "Arrivals: " + arrCount + "\n" + E.CHECK +
                "Departures: " + depCount + "\n" + E.CHECK +
                "Commuter Requests: " + comCount+ "\n" + E.CHECK +
                "Dispatch Records: " + disCount + "\n" + E.CHECK +
                "Passenger Counts: " + passCount + "\n" + E.CHECK +
                "Heartbeats: " + hbCount + "\n";

        logger.info(sb);
        return bag;
    }
    public File getAssociationBagZippedFile(String associationId, String startDate) throws Exception {
        logger.info(E.PANDA + E.PANDA +E.PANDA +E.PANDA +
                " getAssociationBagZippedFile, associationId: " + associationId);

        AssociationBag bag = getAssociationBag(associationId, startDate);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(bag);

        return Zipper.getZippedFile(json);
    }


    public String fixOwnerToPassengerCounts(String userId, String ownerId, String ownerName) {
        List<AmbassadorPassengerCount> counts = ambassadorPassengerCountRepository.findByUserId(userId);
        for (AmbassadorPassengerCount count : counts) {
            count.setOwnerId(ownerId);
            count.setOwnerName(ownerName);
        }
        ambassadorPassengerCountRepository.saveAll(counts);

        return "Passenger counts fixed: " + counts.size();

    }

    Random random = new Random(System.currentTimeMillis());


    @Async
    public void generateRouteDispatchRecords(Route route,
                                             Vehicle vehicle,
                                             List<RouteLandmark> routeLandmarks,
                                             List<User> users,
                                             int intervalInSeconds) {

        logger.info(E.BLUE_DOT + E.BLUE_DOT + E.BLUE_DOT
                + " generateRouteDispatchRecords (parallel ASYNC): " + vehicle.getVehicleReg()
                + " " + routeLandmarks.get(0).getLandmarkName()
                + " on " + routeLandmarks.get(0).getRouteName() + " "
                + E.RED_DOT + E.RED_DOT + E.RED_DOT + E.RED_DOT);

        int wait = random.nextInt(10);
        if (wait == 0) wait = 2;
        try {
//            logger.info(E.BLUE_DOT + E.BLUE_DOT + E.BLUE_DOT
//                    + " generateRouteDispatchRecords ... sleeping for " + wait + " seconds" +
//                    " for " + vehicle.getVehicleReg() + " before execution");
            Thread.sleep(wait * 1000L);
        } catch (InterruptedException e) {
            //ignore
        }
        DateTime minutesAgo = DateTime.now().toDateTimeISO().minusHours(1);
//        logger.info(E.BLUE_DOT + route.getName() + " will be used for "
//                + vehicle.getVehicleReg() + " starting at: " + minutesAgo +
//                " number of routeLandmarks on route: " + routeLandmarks.size());

        AmbassadorPassengerCount previousAPC = null;


        int index = 0;
        for (RouteLandmark mark : routeLandmarks) {
//            logger.info(E.BLUE_DOT + " ...... processing landmark: " + E.FLOWER_RED +
//                    E.FLOWER_RED + " " + mark.getLandmarkName());
            try {
                // generate arrival and dispatch at landmark
                handleArrivalAndDispatch(users, vehicle, minutesAgo, mark);
                minutesAgo = handleDateAndSleep(minutesAgo, intervalInSeconds / 2);

                AmbassadorPassengerCount count = generateAmbassadorPassengerCount(vehicle, routeLandmarks,
                        users, minutesAgo, previousAPC, mark);

                minutesAgo = handleDateAndSleep(minutesAgo, intervalInSeconds / 2);

                generateDeparture(vehicle, mark, minutesAgo);

                //generate heartbeats if nextLandmark is not null
                RouteLandmark nextLandmark;
                try {
                    nextLandmark = routeLandmarks.get(index + 1);
                    if (nextLandmark != null) {
                       // logger.info(E.BLUE_DISC + E.BLUE_DISC + E.BLUE_DISC + " next landmark: " + nextLandmark.getLandmarkName());
                        try {
                            Thread.sleep(random.nextInt(5) * 1000L);
                        } catch (InterruptedException e) {
                            //ignore
                        }
                        generateHeartbeatBetweenLandmarks(vehicle, mark, nextLandmark, minutesAgo);
                    }
                } catch (Exception e) {
                    logger.info(E.RED_DOT + " We have reached the end of the route: " + mark.getRouteName());
                }


//                logger.info(E.CROISSANT + " apc: getCurrentPassengers: " + count.getCurrentPassengers());
                previousAPC = count;
                minutesAgo = handleDateAndSleep(minutesAgo, intervalInSeconds / 2);
                index++;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        logger.info(E.FROG + E.FROG + " Dispatch records for: " + vehicle.getVehicleReg()
                + E.FLOWER_RED + " route: " + route.getName());
    }

    private void generateHeartbeatBetweenLandmarks(Vehicle vehicle, RouteLandmark mark,
                                                   RouteLandmark next, DateTime minutesAgo) {
        int fromIndex = mark.getRoutePointIndex();
        int toIndex = next.getRoutePointIndex();

        Query query = new Query(Criteria.where(
                "routeId").is(mark.getRouteId())
                .andOperator(Criteria.where("index").gte(fromIndex).lte(toIndex)));
        query.with(Sort.by(Sort.Direction.ASC, "index"));
        //
        List<RoutePoint> points = mongoTemplate.find(query, RoutePoint.class);
        logger.info(E.BLUE_DISC + E.BLUE_DISC + E.BLUE_DISC + " next landmark, points: " + points.size());

        //get a stop somewhere ... x2 conditionally
        int index = random.nextInt(points.size() - 1);
        RoutePoint rp = points.get(index);
        writeHeartbeatBetween(vehicle, minutesAgo, rp);
        int index2 = random.nextInt(points.size() - 1);
        if (index2 > index && ((index2 - index) > 20)) {
            rp = points.get(index2);
            writeHeartbeatBetween(vehicle, minutesAgo, rp);
        }
        /* todo - generate heartbeats between this one and the next ... */


    }

    private void writeHeartbeatBetween(Vehicle vehicle, DateTime minutesAgo, RoutePoint rp) {
        VehicleHeartbeat hb = new VehicleHeartbeat();
        hb.setVehicleHeartbeatId(UUID.randomUUID().toString());
        hb.setModel(vehicle.getModel());
        hb.setMake(vehicle.getMake());
        hb.setOwnerName(vehicle.getOwnerName());
        hb.setOwnerId(vehicle.getOwnerId());
        hb.setVehicleId(vehicle.getVehicleId());
        hb.setVehicleReg(vehicle.getVehicleReg());
        hb.setCreated(minutesAgo.plusMinutes(random.nextInt(10)).toDateTimeISO().toString());
        hb.setPosition(rp.getPosition());
        hb.setAssociationId(vehicle.getAssociationId());

        heartbeatService.addVehicleHeartbeat(hb);
//        logger.info(E.BLUE_DISC + E.BLUE_DISC + E.BLUE_DISC
//                + " added heartbeat between landmarks: " + vehicle.getVehicleReg());

        try {
            int wait = random.nextInt(5);
            if (wait == 0) wait = 1;
            Thread.sleep(wait * 1000L);
        } catch (InterruptedException e) {
            //ignore
        }

    }

    private void generateDeparture(Vehicle vehicle, RouteLandmark mark, DateTime minutesAgo) {
        VehicleDeparture d = new VehicleDeparture();
        d.setVehicleDepartureId(UUID.randomUUID().toString());
        d.setCreated(minutesAgo.plusMinutes(random.nextInt(10)).toDateTimeISO().toString());
        d.setMake(vehicle.getMake());
        d.setModel(vehicle.getModel());
        d.setOwnerId(vehicle.getOwnerId());
        d.setOwnerName(vehicle.getOwnerName());
        d.setAssociationId(vehicle.getAssociationId());
        d.setAssociationName(vehicle.getAssociationName());
        d.setLandmarkId(mark.getLandmarkId());
        d.setLandmarkName(mark.getLandmarkName());
        d.setPosition(mark.getPosition());
        d.setVehicleId(vehicle.getVehicleId());
        d.setVehicleReg(vehicle.getVehicleReg());

        addVehicleDeparture(d);
//        logger.info("Vehicle departure: " + vehicle.getVehicleReg() + " from " + mark.getLandmarkName());
    }

    public AmbassadorPassengerCount generateAmbassadorPassengerCount(Vehicle vehicle,
                                                                     List<RouteLandmark> routeLandmarks,
                                                                     List<User> users, DateTime minutesAgo,
                                                                     AmbassadorPassengerCount previousAPC,
                                                                     RouteLandmark mark) {
        // generate passenger counts at this landmark
        List<AmbassadorPassengerCount> counts = new ArrayList<>();
        AmbassadorPassengerCount apc = ambassadorService.getAmbassadorPassengerCount(
                users, counts, vehicle, routeLandmarks,
                minutesAgo, mark.getIndex(), previousAPC, mark);

        logger.info(E.HAND1 + E.HAND1 + " Passenger Count: " + apc.getPassengersIn()
                + " out: " + apc.getPassengersOut()
                + " current: " + apc.getCurrentPassengers());
        return apc;
    }


    private DateTime handleDateAndSleep(DateTime minutesAgo, int intervalInSeconds) {
        int addMin = random.nextInt(20);
        if (addMin == 0) {
            addMin = 5;
        }
        minutesAgo = minutesAgo.plusMinutes(addMin);
        try {
            Thread.sleep(intervalInSeconds * 1000L);
        } catch (InterruptedException e) {
            //ignore
        }
        return minutesAgo;
    }


    private void handleArrivalAndDispatch(List<User> users,
                                          Vehicle vehicle, DateTime minutesAgo,
                                          RouteLandmark mark) {

        int userIndex = random.nextInt(users.size() - 1);
        User user = users.get(userIndex);
        handleArrival(vehicle, minutesAgo, mark);

        DispatchRecord dp = getDispatchRecord(vehicle, minutesAgo, mark, user);

        writeHeartbeat(vehicle, minutesAgo, mark);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            //ignore
        }
        int passengers = random.nextInt(12);
        if (passengers < 3) passengers = 12;
        dp.setPassengers(passengers);
        //
        addDispatchRecord(dp);
        //
//        logger.info(E.FROG + E.FROG + " dispatch record added: " + dp.getVehicleReg()
//                + " passengers: " + dp.getPassengers() + " at landmark: " + dp.getLandmarkName()
//                + " of route: " + dp.getRouteName() + " " + E.BLUE_BIRD);
    }

    private void writeHeartbeat(Vehicle vehicle, DateTime minutesAgo, RouteLandmark mark) {
        VehicleHeartbeat hb = new VehicleHeartbeat();
        hb.setVehicleHeartbeatId(UUID.randomUUID().toString());
        hb.setModel(vehicle.getModel());
        hb.setMake(vehicle.getMake());
        hb.setOwnerName(vehicle.getOwnerName());
        hb.setOwnerId(vehicle.getOwnerId());
        hb.setVehicleId(vehicle.getVehicleId());
        hb.setVehicleReg(vehicle.getVehicleReg());
        hb.setCreated(minutesAgo.toString());
        hb.setPosition(mark.getPosition());
        hb.setAssociationId(vehicle.getAssociationId());

        heartbeatService.addVehicleHeartbeat(hb);
        /* todo - generate heartbeats between this one and the next ... */
    }

    private AmbassadorPassengerCount getAmbassadorPassengerCount(Vehicle vehicle, DateTime minutesAgo, RouteLandmark mark, User user, int passengers) {
        AmbassadorPassengerCount apc = new AmbassadorPassengerCount();
        apc.setCreated(minutesAgo.toDateTimeISO().toString());
        apc.setPassengersIn(passengers);
        apc.setPassengersOut(0);
        apc.setCurrentPassengers(passengers);
        apc.setVehicleId(vehicle.getVehicleId());
        apc.setVehicleReg(vehicle.getVehicleReg());
        apc.setPosition(mark.getPosition());
        apc.setOwnerId(vehicle.getOwnerId());
        apc.setOwnerName(vehicle.getOwnerName());
        apc.setAssociationId(vehicle.getAssociationId());
        apc.setRouteId(mark.getRouteId());
        apc.setRouteName(mark.getRouteName());
        apc.setUserId(user.getUserId());
        apc.setUserName(user.getName());
        apc.setRouteLandmarkId(mark.getLandmarkId());
        apc.setRouteLandmarkName(mark.getLandmarkName());
        apc.setPassengerCountId(UUID.randomUUID().toString());

        return ambassadorService.addAmbassadorPassengerCount(apc);
    }

    private void handleArrival(Vehicle vehicle, DateTime minutesAgo, RouteLandmark mark) {
        VehicleArrival va = new VehicleArrival();
        va.setDispatched(false);
        va.setCreated(minutesAgo.toString());
        va.setAssociationName(vehicle.getAssociationName());
        va.setAssociationId(vehicle.getAssociationId());
        va.setPosition(mark.getPosition());
        va.setLandmarkName(mark.getLandmarkName());
        va.setOwnerId(vehicle.getOwnerId());
        va.setOwnerName(vehicle.getOwnerName());
        va.setMake(vehicle.getMake());
        va.setModel(vehicle.getModel());
        va.setLandmarkId(mark.getLandmarkId());
        va.setVehicleId(vehicle.getVehicleId());
        va.setVehicleReg(vehicle.getVehicleReg());
        va.setVehicleArrivalId(UUID.randomUUID().toString());

        addVehicleArrival(va);
        logger.info(E.LEAF + E.LEAF + E.LEAF + E.LEAF +
                " arrival record added: " + va.getVehicleReg()
                + " at landmark: " + va.getLandmarkName() + " " + E.BLUE_BIRD);
    }

    public static DispatchRecord getDispatchRecord(Vehicle vehicle, DateTime minutesAgo, RouteLandmark mark, User user) {
        DispatchRecord dp = new DispatchRecord();
        dp.setDispatched(true);
        dp.setMarshalId(user.getUserId());
        dp.setMarshalName(user.getName());
        dp.setCreated(minutesAgo.toString());
        dp.setLandmarkName(mark.getLandmarkName());
        dp.setDispatchRecordId(UUID.randomUUID().toString());
        dp.setOwnerId(vehicle.getOwnerId());
        dp.setPosition(mark.getPosition());
        dp.setAssociationId(vehicle.getAssociationId());
        dp.setVehicleReg(vehicle.getVehicleReg());
        dp.setVehicleId(vehicle.getVehicleId());
        dp.setRouteLandmarkId(mark.getLandmarkId());
        dp.setAssociationId(vehicle.getAssociationId());
        dp.setAssociationName(vehicle.getAssociationName());
        dp.setRouteId(mark.getRouteId());
        dp.setRouteName(mark.getRouteName());
        dp.setLandmarkName(mark.getLandmarkName());
        dp.setOwnerId(vehicle.getOwnerId());
        return dp;
    }


}
