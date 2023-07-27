package com.boha.kasietransie.services;

import com.boha.kasietransie.data.BigBag;
import com.boha.kasietransie.data.CounterBag;
import com.boha.kasietransie.data.DispatchRecordList;
import com.boha.kasietransie.data.dto.*;
import com.boha.kasietransie.data.repos.*;
import com.boha.kasietransie.util.E;
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
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

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
    private static final Logger logger = LoggerFactory.getLogger(DispatchService.class);


    public DispatchService(DispatchRecordRepository dispatchRecordRepository,
                           VehicleArrivalRepository vehicleArrivalRepository,
                           VehicleDepartureRepository vehicleDepartureRepository,
                           VehicleHeartbeatRepository vehicleHeartbeatRepository, MessagingService messagingService,
                           HeartbeatService heartbeatService, MongoTemplate mongoTemplate, AmbassadorPassengerCountRepository ambassadorPassengerCountRepository, RouteService routeService, VehicleRepository vehicleRepository, RouteRepository routeRepository, UserRepository userRepository) {
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

        return "Passenger counts fixed: " + counts.size();

    }

    Random random = new Random(System.currentTimeMillis());

    public List<DispatchRecord> generateRouteDispatchRecords(
            String routeId, int numberOfCars, int intervalInSeconds) {
        List<Vehicle> all = vehicleRepository.findByAssociationId(routeId);
        logger.info(E.BLUE_DOT + " found " + all.size()
                + " cars for route: " + routeId + " ----- " + E.RED_DOT + E.RED_DOT);

        List<Vehicle> vehicleList;
        if (numberOfCars >= all.size()) {
            vehicleList = all;
        } else {
            vehicleList = getCars(all, numberOfCars);
        }
             Route route = null;
        List<Route> routes = routeRepository.findByRouteId(routeId);
        if (!routes.isEmpty()) {
            route = routes.get(0);
        }
        if (route == null) {
            return new ArrayList<>();
        }
        logger.info(E.BLUE_DOT + " processing " + vehicleList.size()
                + " cars for dispatch record generation ... on route: " + route.getName());

        List<User> users = userRepository.findByAssociationId(routeId);
        List<DispatchRecord> dispatchRecords = new ArrayList<>();
        Criteria c = Criteria.where("routeId").is(routeId);
        Query query = new Query(c).with(Sort.by("index"));
        List<RouteLandmark> routeLandmarks = mongoTemplate.find(query, RouteLandmark.class);

        for (Vehicle vehicle : vehicleList) {
            DateTime minutesAgo = DateTime.now().toDateTimeISO().minusHours(1);
            logger.info(E.BLUE_DOT + route.getName() + " will be used for "
                    + vehicle.getVehicleReg() + " starting at: " + minutesAgo +
                    " number of routeLandmarks on route: " + routeLandmarks.size());
            for (RouteLandmark mark : routeLandmarks) {
                minutesAgo = handleDispatch(intervalInSeconds, users, dispatchRecords, vehicle, minutesAgo, mark);
            }
        }

        return dispatchRecords;
    }

    public List<DispatchRecord> generateDispatchRecords(
            String associationId, int numberOfCars, int intervalInSeconds) {
        List<Vehicle> all = vehicleRepository.findByAssociationId(associationId);
        logger.info(E.BLUE_DOT + " found " + all.size()
                + " cars for association: " + associationId + " ----- " + E.RED_DOT + E.RED_DOT);

        List<Vehicle> vehicleList;
        if (numberOfCars >= all.size()) {
            vehicleList = all;
        } else {
            vehicleList = getCars(all, numberOfCars);
        }

        logger.info(E.BLUE_DOT + " processing " + vehicleList.size()
                + " cars for dispatch record generation ...");
        List<Route> routes = routeService.getAssociationRoutes(associationId);
        List<Route> filteredRoutes = new ArrayList<>();
        for (Route route : routes) {
            long cnt = mongoTemplate.count(query(
                    where("routeId").is(route.getRouteId())), RouteLandmark.class);
            if (cnt > 0) {
                filteredRoutes.add(route);
            }
        }
        logger.info(E.BLUE_DOT + " routes in play: " + filteredRoutes.size() + " routes ...");
        List<User> users = userRepository.findByAssociationId(associationId);
        List<DispatchRecord> dispatchRecords = new ArrayList<>();

        for (Vehicle vehicle : vehicleList) {
            int index = random.nextInt(filteredRoutes.size() - 1);
            Route route = filteredRoutes.get(index);
            Criteria c = Criteria.where("routeId").is(route.getRouteId());
            Query query = new Query(c).with(Sort.by("index"));
            List<RouteLandmark> marks = mongoTemplate.find(query, RouteLandmark.class);
            DateTime minutesAgo = DateTime.now().toDateTimeISO().minusHours(1);

            logger.info(E.BLUE_DOT + route.getName() + " will be used for "
                    + vehicle.getVehicleReg() + " starting at: " + minutesAgo +
                    " number of routeLandmarks on route: " + marks.size());

            for (RouteLandmark mark : marks) {
                minutesAgo = handleDispatch(intervalInSeconds, users, dispatchRecords, vehicle, minutesAgo, mark);
            }
        }

        return dispatchRecords;
    }

    private DateTime handleDispatch(int intervalInSeconds, List<User> users,
                                    List<DispatchRecord> dispatchRecords,
                                    Vehicle vehicle, DateTime minutesAgo,
                                    RouteLandmark mark) {
        int userIndex = random.nextInt(users.size() - 1);
        User user = users.get(userIndex);
        handleArrival(vehicle, minutesAgo, mark);
        try {
            Thread.sleep(random.nextInt(10) * 1000L);
        } catch (InterruptedException e) {
            //ignore
        }
        int addMin0 = random.nextInt(20);
        if (addMin0 == 0) {
            addMin0 = 5;
        }
        minutesAgo = minutesAgo.plusMinutes(addMin0);
        DispatchRecord dp = getDispatchRecord(vehicle, minutesAgo, mark, user);

        int passengers = random.nextInt(20);
        if (passengers < 3) passengers = 16;
        dp.setPassengers(passengers);
        //
        DispatchRecord rec = addDispatchRecord(dp);
        dispatchRecords.add(rec);

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
        //
        logger.info(E.LEAF + E.LEAF + " dispatch record added: " + dp.getVehicleReg()
                + " passengers: " + dp.getPassengers() + " at landmark: " + dp.getLandmarkName()
                + " of route: " + dp.getRouteName() + " " + E.BLUE_BIRD);
        return minutesAgo;
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
        logger.info(E.LEAF + E.LEAF + " arrival record added: " + va.getVehicleReg()
                + " at landmark: " + va.getLandmarkName() + " " + E.BLUE_BIRD);
    }

    private static DispatchRecord getDispatchRecord(Vehicle vehicle, DateTime minutesAgo, RouteLandmark mark, User user) {
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

    public List<Vehicle> getCars(List<Vehicle> list, int numberOfCars) {
        List<Vehicle> vehicles = new ArrayList<>();
        for (Vehicle vehicle : list) {
            vehicles.add(vehicle);
            if (vehicles.size() == numberOfCars) {
                break;
            }
        }
        //
        return vehicles;
    }


}
