package com.boha.kasietransie.services;

import com.boha.kasietransie.data.GenerationRequest;
import com.boha.kasietransie.data.dto.*;
import com.boha.kasietransie.data.repos.*;
import com.boha.kasietransie.util.E;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@RequiredArgsConstructor
@EnableAsync
@Service
public class HeartbeatService {

    final HeartbeatRepository heartbeatRepository;
    final MongoTemplate mongoTemplate;
    final MessagingService messagingService;
    final RoutePointRepository routePointRepository;
    final RouteLandmarkRepository routeLandmarkRepository;
    final RouteRepository routeRepository;
    final DispatchRecordRepository dispatchRecordRepository;
    final VehicleArrivalRepository vehicleArrivalRepository;
    final VehicleDepartureRepository vehicleDepartureRepository;
    final VehicleRepository vehicleRepository;
    final UserRepository userRepository;
    final AmbassadorService ambassadorService;
    final VehicleHeartbeatTimeSeriesRepository vehicleHeartbeatTimeSeriesRepository;
    final TimeSeriesService timeSeriesService;
    private static final Logger logger = LoggerFactory.getLogger(HeartbeatService.class);

    @Async
    public void generateRouteHeartbeats(GenerationRequest request) {
        logger.info(E.DOG + E.DOG + E.DOG + " heartbeat generation started: cars: " + request.getVehicleIds().size()
                + " intervalInSeconds: " + request.getIntervalInSeconds());
        Route route = null;
        List<Route> routes = routeRepository.findByRouteId(request.getRouteId());
        if (!routes.isEmpty()) {
            route = routes.get(0);
        }
        if (route == null) {
            return;
        }
        ExecutorService executorService = Executors.newFixedThreadPool(request.getVehicleIds().size());
        logger.info(E.DOG + E.DOG + E.DOG + " executorService newFixedThreadPool built: " + executorService);

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (String vehicleId : request.getVehicleIds()) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(()
                    -> generateVehicleRouteHeartbeats(vehicleId, request.getRouteId(),
                    request.getStartDate(), request.getIntervalInSeconds()), executorService);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executorService.shutdown();


        logger.info(E.DOG + E.DOG + E.DOG + " parallel heartbeat generation completed for : "
                + route.getName() + "\n\n");


    }

    @Async
    public void generateVehicleRouteHeartbeats(String vehicleId, String routeId,
                                               String startDate, int intervalInSeconds) {

        logger.info(E.FERN + E.FERN + E.FERN + " ... start generateVehicleRouteHeartbeats, hopefully in parallel: " + vehicleId);
        List<RoutePoint> points = routePointRepository.findByRouteIdOrderByCreatedAsc(routeId);

        Vehicle vehicle = null;
        List<Vehicle> cars = vehicleRepository.findByVehicleId(vehicleId);
        if (!cars.isEmpty()) {
            vehicle = cars.get(0);
        }
        if (vehicle == null) {
            return;
        }

        logger.info(E.FERN + E.FERN + E.FERN + " generateVehicleRouteHeartbeats: car: " + vehicle.getVehicleReg());

        List<User> users = userRepository.findByAssociationId(vehicle.getAssociationId());
        User user = null;
        if (!users.isEmpty()) {
            user = users.get(0);
        }
        if (user == null) {
            return;
        }
        final int DIVISOR = 100;
        int rem = points.size() % DIVISOR;
        int pages = points.size() / DIVISOR;
        if (rem > 0) {
            pages++;
        }

        logger.info(E.RED_CAR + E.RED_CAR + " generateVehicleRouteHeartbeats, route chunks: " + pages);

        List<RoutePoint> pointsFiltered = new ArrayList<>();

        for (int i = 0; i < pages; i++) {
            int index = i * DIVISOR;
            try {
                int choice = random.nextInt(100);
                if (choice > 25) {
                    index += random.nextInt(150);
                }
                RoutePoint rp = points.get(index);
                pointsFiltered.add(rp);
            } catch (Exception e) {
                pointsFiltered.add(points.get(points.size() - 1));
            }
        }

        logger.info(E.RED_CAR + E.RED_CAR + " Number of Points for Heartbeat: " + pointsFiltered.size());

        writeHeartbeat(vehicleId, startDate, intervalInSeconds, vehicle, pointsFiltered);
        //
        logger.info(E.DOG + E.DOG + E.DOG + " heartbeats added " + vehicle.getVehicleReg());
    }

    private void writeHeartbeat(String vehicleId, String startDate, int intervalInSeconds, Vehicle vehicle, List<RoutePoint> pointsFiltered) {
        DateTime date = DateTime.parse(startDate);
        for (RoutePoint point : pointsFiltered) {
            date = date.plusMinutes(random.nextInt(10));
            int choice = random.nextInt(100);
            if (choice > 10) {
                VehicleHeartbeat hb = new VehicleHeartbeat();
                hb.setVehicleId(vehicleId);
                hb.setVehicleHeartbeatId(UUID.randomUUID().toString());
                hb.setVehicleReg(vehicle.getVehicleReg());
                hb.setMake(vehicle.getMake());
                hb.setModel(vehicle.getModel());
                hb.setPosition(point.getPosition());
                hb.setOwnerId(vehicle.getOwnerId());
                hb.setOwnerName(vehicle.getOwnerName());
                hb.setCreated(date.toDateTimeISO().toString());
                hb.setAssociationId(vehicle.getAssociationId());
                try {
                    heartbeatRepository.insert(hb);
                    logger.info(E.DOG + E.DOG + E.DOG + E.DOG + " heartbeat added " + vehicle.getVehicleReg() + " - " + hb.getCreated());
                    messagingService.sendMessage(hb);
                    Thread.sleep(intervalInSeconds * 1000L);
                } catch (InterruptedException e) {
                    //ignore
                }

            }
        }
    }


    Random random = new Random(System.currentTimeMillis());

    public VehicleHeartbeat addVehicleHeartbeat(VehicleHeartbeat heartbeat) {
        VehicleHeartbeat hb = heartbeatRepository.insert(heartbeat);
        HeartbeatMeta meta = new HeartbeatMeta();
        meta.setVehicleId(heartbeat.getVehicleId());
        meta.setVehicleReg(heartbeat.getVehicleReg());
        meta.setLatitude(heartbeat.getPosition().getCoordinates().get(1));
        meta.setLongitude(heartbeat.getPosition().getCoordinates().get(0));

        meta.setOwnerId(heartbeat.getOwnerId());
        meta.setAssociationId(heartbeat.getAssociationId());

        timeSeriesService.addHeartbeatTimeSeries(heartbeat.getAssociationId(),
                heartbeat.getVehicleId(), heartbeat.getVehicleReg());

        messagingService.sendMessage(hb);
        return hb;
    }

    public List<VehicleHeartbeat> getAssociationVehicleHeartbeats(String associationId, String startDate) {

        Criteria c = Criteria.where("associationId").is(associationId)
                .and("created").gte(startDate);
        Query query = new Query(c);
        return mongoTemplate.find(query, VehicleHeartbeat.class);
    }

    public List<VehicleHeartbeat> getVehicleHeartbeats(String vehicleId, int cutoffHours) {
        DateTime dt = DateTime.now().minusHours(cutoffHours);
        String startDate = dt.toDateTimeISO().toString();
        Criteria c = Criteria.where("vehicleId").is(vehicleId)
                .and("date").gte(startDate);
        Query query = new Query(c);
        return mongoTemplate.find(query, VehicleHeartbeat.class);
    }

    public List<VehicleHeartbeat> getOwnerVehicleHeartbeats(String userId, int cutoffHours) {
        DateTime dt = DateTime.now().minusHours(cutoffHours);
        String startDate = dt.toDateTimeISO().toString();
        Criteria c = Criteria.where("userId").is(userId)
                .and("date").gte(startDate);
        Query query = new Query(c);
        return mongoTemplate.find(query, VehicleHeartbeat.class);
    }

    public long countVehicleHeartbeats(String vehicleId) {
        return mongoTemplate.count(query(where("vehicleId").is(vehicleId)), VehicleHeartbeat.class);
    }
}
