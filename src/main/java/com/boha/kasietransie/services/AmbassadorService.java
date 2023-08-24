package com.boha.kasietransie.services;

import com.boha.kasietransie.data.dto.*;
import com.boha.kasietransie.data.repos.*;
import com.boha.kasietransie.util.E;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.joda.time.DateTime;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
public class AmbassadorService {

    final AmbassadorCheckInRepository ambassadorCheckInRepository;
    final AmbassadorPassengerCountRepository ambassadorPassengerCountRepository;
    final MongoTemplate mongoTemplate;
    final MessagingService messagingService;

    final VehicleRepository vehicleRepository;
    final UserRepository userRepository;
    final RouteRepository routeRepository;
    final RouteService routeService;

    public AmbassadorService(AmbassadorCheckInRepository ambassadorCheckInRepository,
                             AmbassadorPassengerCountRepository ambassadorPassengerCountRepository,
                             MongoTemplate mongoTemplate, MessagingService messagingService,
                             VehicleRepository vehicleRepository, UserRepository userRepository, RouteRepository routeRepository, RouteService routeService) {
        this.ambassadorCheckInRepository = ambassadorCheckInRepository;
        this.ambassadorPassengerCountRepository = ambassadorPassengerCountRepository;
        this.mongoTemplate = mongoTemplate;
        this.messagingService = messagingService;
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
        this.routeRepository = routeRepository;
        this.routeService = routeService;
    }

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger logger = Logger.getLogger(VehicleService.class.getSimpleName());

    private static final String XX = E.PRESCRIPTION + E.PRESCRIPTION + E.PRESCRIPTION;

    public List<AmbassadorCheckIn> getAssociationAmbassadorCheckIn(String associationId, String startDate) {
        Criteria c = Criteria.where("associationId").is(associationId)
                .and("created").gte(startDate);
        Query query = new Query(c);
        return mongoTemplate.find(query, AmbassadorCheckIn.class);
    }

    public List<AmbassadorCheckIn> getVehicleAmbassadorCheckIn(String vehicleId, String startDate) {
        Criteria c = Criteria.where("vehicleId").is(vehicleId)
                .and("created").gte(startDate);
        Query query = new Query(c);
        return mongoTemplate.find(query, AmbassadorCheckIn.class);
    }

    public List<AmbassadorCheckIn> getUserAmbassadorCheckIn(String userId, String startDate) {
        Criteria c = Criteria.where("userId").is(userId)
                .and("created").gte(startDate);
        Query query = new Query(c);
        return mongoTemplate.find(query, AmbassadorCheckIn.class);
    }

    public AmbassadorPassengerCount addAmbassadorCheckIn(AmbassadorPassengerCount count, String startDate) {
        ambassadorPassengerCountRepository.insert(count);
        return count;
    }

    public List<AmbassadorPassengerCount> getUserAmbassadorPassengerCounts(String userId, String startDate) {
        Criteria c = Criteria.where("userId").is(userId)
                .and("created").gte(startDate);
        Query query = new Query(c);
        return mongoTemplate.find(query, AmbassadorPassengerCount.class);
    }

    public List<AmbassadorPassengerCount> getAssociationAmbassadorPassengerCounts(String associationId, String startDate) {
        Criteria c = Criteria.where("associationId").is(associationId)
                .and("created").gte(startDate);
        Query query = new Query(c);
        return mongoTemplate.find(query, AmbassadorPassengerCount.class);
    }

    public List<AmbassadorPassengerCount> getRoutePassengerCounts(String routeId, String startDate) {
        Criteria c = Criteria.where("routeId").is(routeId)
                .and("created").gte(startDate);
        Query query = new Query(c).with(Sort.by("created").descending());
        return mongoTemplate.find(query, AmbassadorPassengerCount.class);
    }

    public List<AmbassadorPassengerCount> getVehicleAmbassadorPassengerCounts(String vehicleId, String startDate) {
        Criteria c = Criteria.where("vehicleId").is(vehicleId)
                .and("created").gte(startDate);
        Query query = new Query(c);
        return mongoTemplate.find(query, AmbassadorPassengerCount.class);
    }

    public AmbassadorCheckIn addAmbassadorCheckIn(AmbassadorCheckIn checkIn) {
        ambassadorCheckInRepository.insert(checkIn);
        return checkIn;
    }

    public AmbassadorPassengerCount addAmbassadorPassengerCount(AmbassadorPassengerCount count) {
        ambassadorPassengerCountRepository.insert(count);
        messagingService.sendMessage(count);
        return count;
    }

    Random random = new Random(System.currentTimeMillis());


    public List<AmbassadorPassengerCount> generateRoutePassengerCounts(
            String routeId, int numberOfCars, int intervalInSeconds) {

        Route route = null;
        List<Route> routes = routeRepository.findByRouteId(routeId);
        if (!routes.isEmpty()) {
            route = routes.get(0);
        }
        if (route == null) {
            return new ArrayList<>();
        }
        List<Vehicle> all = vehicleRepository.findByAssociationId(route.getAssociationId());
        logger.info(E.BLUE_DOT + " found " + all.size()
                + " cars for route: " + routeId + " ----- " + E.RED_DOT + E.RED_DOT);

        List<Vehicle> vehicleList = getCars(all, numberOfCars);
        logger.info(E.BLUE_DOT + " processing " + vehicleList.size()
                + " cars for passengerCount generation ...");
        for (Vehicle vehicle : vehicleList) {
            logger.info(E.OK + E.OK + " vehicle included: " + E.RED_APPLE + vehicle.getVehicleReg()
                    + " " + E.FLOWER_RED + " owner: " + vehicle.getOwnerName());
        }
        List<User> users = userRepository.findByAssociationId(route.getAssociationId());
        Criteria c = Criteria.where("routeId").is(routeId);
        Query query = new Query(c).with(Sort.by("index"));
        List<RouteLandmark> routeLandmarks = mongoTemplate.find(query, RouteLandmark.class);
        List<AmbassadorPassengerCount> passengerCounts = new ArrayList<>();

        for (Vehicle vehicle : vehicleList) {
            DateTime minutesAgo = DateTime.now().toDateTimeISO().minusHours(1);
            int landmarkIndex = 0;
            AmbassadorPassengerCount previousAPC = null;

            for (RouteLandmark mark : routeLandmarks) {
                AmbassadorPassengerCount apc = getAmbassadorPassengerCount(users, passengerCounts,
                        vehicle, routeLandmarks, minutesAgo, landmarkIndex, previousAPC, mark);
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
                previousAPC = apc;
                landmarkIndex++;
                logger.info(E.LEAF + E.LEAF + " passenger count added: " + apc.getVehicleReg()
                        + " in: " + apc.getPassengersIn() + " out: " + apc.getPassengersOut()
                        + " current: " + apc.getCurrentPassengers() + E.LEAF + " at landmark: " + apc.getRouteLandmarkName()
                        + " of route: " + apc.getRouteName() + " " + E.BLUE_BIRD);
            }
        }

        return passengerCounts;
    }

    public List<AmbassadorPassengerCount> generateAmbassadorPassengerCounts(
            String associationId, int numberOfCars, int intervalInSeconds) {
        List<Vehicle> all = vehicleRepository.findByAssociationId(associationId);
        logger.info(E.BLUE_DOT + " found " + all.size()
                + " cars for association: " + associationId + " ----- " + E.RED_DOT + E.RED_DOT);

        List<Vehicle> vehicleList = getCars(all, numberOfCars);
        logger.info(E.BLUE_DOT + " processing " + vehicleList.size()
                + " cars for heartbeat generation ...");
        logger.info(E.BLUE_DOT + " processing " + vehicleList.size()
                + " cars for passengerCount generation ...");
        for (Vehicle vehicle : vehicleList) {
            logger.info(E.OK + E.OK + " vehicle included: " + E.RED_APPLE + vehicle.getVehicleReg()
                    + " " + E.FLOWER_RED + " owner: " + vehicle.getOwnerName());
        }
        List<Route> routes = routeService.getAssociationRoutes(associationId);
        List<Route> filteredRoutes = new ArrayList<>();
        for (Route route : routes) {
            long cnt = mongoTemplate.count(query(
                    where("routeId").is(route.getRouteId())), RoutePoint.class);
            if (cnt > 100) {
                filteredRoutes.add(route);
            }
        }
        logger.info(E.BLUE_DOT + " routes in play: " + filteredRoutes.size() + " routes ...");
        List<User> users = userRepository.findByAssociationId(associationId);
        List<AmbassadorPassengerCount> passengerCounts = new ArrayList<>();

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
            int landmarkIndex = 0;
            AmbassadorPassengerCount previousAPC = null;
            for (RouteLandmark mark : marks) {
                AmbassadorPassengerCount apc = getAmbassadorPassengerCount(users, passengerCounts,
                        vehicle, marks, minutesAgo, landmarkIndex, previousAPC, mark);
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
                previousAPC = apc;
                landmarkIndex++;
                logger.info(E.LEAF + E.LEAF + " passenger count added: " + apc.getVehicleReg()
                        + " in: " + apc.getPassengersIn() + " out: " + apc.getPassengersOut()
                        + " current: " + apc.getCurrentPassengers() + " at landmark: " + apc.getRouteLandmarkName()
                        + " of route: " + apc.getRouteName() + " " + E.BLUE_BIRD);
            }
        }

        return passengerCounts;
    }

    public AmbassadorPassengerCount getAmbassadorPassengerCount(List<User> users,
                                                                List<AmbassadorPassengerCount> passengerCounts,
                                                                Vehicle vehicle, List<RouteLandmark> marks,
                                                                DateTime minutesAgo,
                                                                int landmarkIndex,
                                                                AmbassadorPassengerCount previousAPC,
                                                                RouteLandmark mark) {
        int userIndex = random.nextInt(users.size() - 1);
        User user = users.get(userIndex);
        int initialPassengers = random.nextInt(16);
        if (initialPassengers == 0) initialPassengers = 16;
        //
        AmbassadorPassengerCount apc = new AmbassadorPassengerCount();
        apc.setCreated(minutesAgo.toString());
        apc.setOwnerId(vehicle.getOwnerId());
        apc.setOwnerName(vehicle.getOwnerName());
        apc.setVehicleId(vehicle.getVehicleId());
        apc.setVehicleReg(vehicle.getVehicleReg());
        apc.setAssociationId(vehicle.getAssociationId());
        apc.setRouteId(mark.getRouteId());
        apc.setRouteName(mark.getRouteName());
        apc.setPosition(mark.getPosition());
        apc.setUserId(user.getUserId());
        apc.setUserName(user.getName());
        apc.setRouteLandmarkId(mark.getLandmarkId());
        apc.setRouteLandmarkName(mark.getLandmarkName());
        apc.setPassengerCountId(UUID.randomUUID().toString());

        if (landmarkIndex == 0) {
            apc.setPassengersOut(0);
            apc.setPassengersIn(initialPassengers);
            apc.setCurrentPassengers(initialPassengers);
        } else {
            int passengersIn = random.nextInt(16);
            if (passengersIn < 2) passengersIn = 6;
            apc.setPassengersIn(passengersIn);
            int passengersOut = random.nextInt(12);
            apc.setPassengersOut(passengersOut);
            if (previousAPC != null) {
                int count = getCurrentPassengers(apc.getPassengersIn(), apc.getPassengersOut(),
                        previousAPC.getCurrentPassengers());
                apc.setCurrentPassengers(count);
            } else {
                apc.setCurrentPassengers(initialPassengers);
            }
        }
        if (landmarkIndex == marks.size() - 1) {
            apc.setPassengersIn(0);
            if (previousAPC != null) {
                apc.setPassengersOut(previousAPC.getCurrentPassengers());
                apc.setCurrentPassengers(0);
            }
        }
        AmbassadorPassengerCount pc = addAmbassadorPassengerCount(apc);
        passengerCounts.add(pc);
        return apc;
    }

    private int getCurrentPassengers(int passengersIn, int passengersOut, int currentPassengers) {
        int num = currentPassengers + passengersIn - passengersOut;
        if (num < 0) {
            num = 0;
        }
        return num;
    }

    public List<Vehicle> getCars(List<Vehicle> list, int numberOfCars) {
        List<Vehicle> map = new ArrayList<>();
        for (Vehicle vehicle : list) {
            map.add(vehicle);
            if (map.size() == numberOfCars) {
                break;
            }
        }
        //
        return map;
    }
}

















