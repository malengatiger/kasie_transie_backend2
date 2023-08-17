package com.boha.kasietransie.services;

import com.boha.kasietransie.data.GenerationRequest;
import com.boha.kasietransie.data.dto.*;
import com.boha.kasietransie.data.repos.RoutePointRepository;
import com.boha.kasietransie.data.repos.RouteRepository;
import com.boha.kasietransie.data.repos.UserRepository;
import com.boha.kasietransie.data.repos.VehicleRepository;
import com.boha.kasietransie.util.E;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
@RequiredArgsConstructor
@EnableAsync
public class DispatchAsyncHelperService {
    final RouteRepository routeRepository;
    final RoutePointRepository routePointRepository;
    final UserRepository userRepository;
    final VehicleRepository vehicleRepository;
    final MongoTemplate mongoTemplate;

    final DispatchService dispatchService;
    final RouteService routeService;
    final CommuterService commuterService;
    private static final Logger logger = LoggerFactory.getLogger(DispatchAsyncHelperService.class);

    @Async
    public void generateRouteDispatchRecordsInParallel(GenerationRequest request) throws Exception {

        Route route = null;
        List<Route> routes = routeRepository.findByRouteId(request.getRouteId());
        if (routes.isEmpty()) {
            throw new Exception("Route not found");
        }
        route = routes.get(0);

        List<Vehicle> vehicleList = new ArrayList<>();

        for (String vehicleId : request.getVehicleIds()) {
            Query q = new Query(Criteria.where("vehicleId").is(vehicleId));
            List<Vehicle> list = mongoTemplate.find(q, Vehicle.class);
            vehicleList.addAll(list);
        }

        List<User> users = userRepository.findByAssociationId(route.getAssociationId());
        Criteria c = Criteria.where("routeId").is(request.getRouteId());
        Query query = new Query(c).with(Sort.by("index"));
        List<RouteLandmark> routeLandmarks = mongoTemplate.find(query, RouteLandmark.class);

        for (Vehicle vehicle : vehicleList) {
            dispatchService.generateRouteDispatchRecords(route, vehicle,
                    routeLandmarks, users, request.getIntervalInSeconds());
        }

    }

    public List<CommuterRequest> generateCommuterRequestsInParallel(
            String associationId, int numberOfCommuters, int intervalInSeconds) {
        logger.info(E.BLUE_DOT + " generateCommuterRequestsInParallel ASYNC " + E.RED_DOT + E.RED_DOT);

        List<Route> routes = routeService.getAssociationRoutes(associationId);
        List<Route> filteredRoutes = new ArrayList<>();

        for (Route route : routes) {
            long cnt = mongoTemplate.count(query(
                    where("routeId").is(route.getRouteId())), RouteLandmark.class);
            if (cnt > 0) {
                filteredRoutes.add(route);
            }
        }

        for (Route filteredRoute : filteredRoutes) {
            commuterService.generateRouteCommuterRequests(
                    filteredRoute.getRouteId(), numberOfCommuters, intervalInSeconds);
        }

        return new ArrayList<>();
    }

    private List<Vehicle> getList(int numberOfCars, Route route) {
        List<Vehicle> vehicleList;
        Page<Vehicle> all = vehicleRepository.findByAssociationId(
                route.getAssociationId(), Pageable.ofSize(numberOfCars));

        logger.info(E.BLUE_DOT + " found " + all.toList().size()
                + " cars for route: " + route.getName() + " ----- " + E.RED_DOT + E.RED_DOT);


        vehicleList = all.toList();
        return vehicleList;
    }
}
