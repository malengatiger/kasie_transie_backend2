package com.boha.kasietransie.controllers;

import com.boha.kasietransie.data.BigBag;
import com.boha.kasietransie.data.CounterBag;
import com.boha.kasietransie.data.RouteBag;
import com.boha.kasietransie.data.dto.*;
import com.boha.kasietransie.services.*;
import com.boha.kasietransie.util.CustomErrorResponse;
import com.boha.kasietransie.util.E;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class ListController {
    private static final Gson G = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger logger = LoggerFactory.getLogger(ListController.class);

    private final MongoService mongoService;
    private final UserService userService;
    private final VehicleService vehicleService;
    private final AssociationService associationService;
    private final UserGeofenceService userGeofenceService;
    private final DispatchService dispatchService;
    private final CityService cityService;
    private final RouteService routeService;
    private final HeartbeatService heartbeatService;
    private final LandmarkService landmarkService;
    private final MediaService mediaService;
    final AmbassadorService ambassadorService;

    @GetMapping("/getUserById")
    public ResponseEntity<Object> getUserById(@RequestParam String userId) {
        try {
            User user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getUserById failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getUserByEmail")
    public ResponseEntity<Object> getUserByEmail(@RequestParam String email) {
        try {
            User user = userService.getUserByEmail(email);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getUserByEmail failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getAssociationById")
    public ResponseEntity<Object> getAssociationById(@RequestParam String associationId) {
        try {
            Association ass = associationService.getAssociationById(associationId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getAssociationById failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getAssociationSettings")
    public ResponseEntity<Object> getAssociationSettingsModels(@RequestParam String associationId) {
        try {
            List<SettingsModel> ass = associationService
                    .getAssociationSettingsModels(associationId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getAssociationSettingsModels failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getAssociationUsers")
    public ResponseEntity<Object> getAssociationUsers(@RequestParam String associationId) {
        try {
            List<User> ass = userService
                    .getAssociationUsers(associationId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getAssociationRoutes failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getAssociationRoutes")
    public ResponseEntity<Object> getAssociationRoutes(@RequestParam String associationId) {
        try {
            List<Route> ass = routeService
                    .getAssociationRoutes(associationId);
            logger.info(E.DOG + E.DOG + " Association Routes found: " + ass.size());
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getAssociationRoutes failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getAssociationVehicles")
    public ResponseEntity<Object> getAssociationVehicles(@RequestParam String associationId, @RequestParam int page) {
        try {
            List<Vehicle> ass = vehicleService
                    .getAssociationVehicles(associationId, page);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getAssociationVehicles failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getAssociations")
    public ResponseEntity<Object> getAssociations() {
        try {
            List<Association> ass = associationService
                    .getAssociations();
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getAssociations failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getAssociationVehicleHeartbeats")
    public ResponseEntity<Object> getAssociationVehicleHeartbeats(@RequestParam String associationId,
                                                                  @RequestParam int cutoffHours) {
        try {
            List<VehicleHeartbeat> ass = heartbeatService
                    .getAssociationVehicleHeartbeats(associationId, cutoffHours);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getAssociationVehicleHeartbeats failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getVehicleHeartbeats")
    public ResponseEntity<Object> getVehicleHeartbeats(@RequestParam String associationId,
                                                       @RequestParam int cutoffHours) {
        try {
            List<VehicleHeartbeat> ass = heartbeatService
                    .getVehicleHeartbeats(associationId, cutoffHours);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getVehicleHeartbeats failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getOwnerVehicleHeartbeats")
    public ResponseEntity<Object> getOwnerVehicleHeartbeats(@RequestParam String associationId,
                                                            @RequestParam int cutoffHours) {
        try {
            List<VehicleHeartbeat> ass = heartbeatService
                    .getOwnerVehicleHeartbeats(associationId, cutoffHours);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getOwnerVehicleHeartbeats failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getAssociationAppErrors")
    public ResponseEntity<Object> getAssociationAppErrors(@RequestParam String associationId) {
        try {
            List<AppError> ass = associationService
                    .getAssociationAppErrors(associationId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getAssociationAppErrors failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getRoutePoints")
    public ResponseEntity<Object> getRoutePoints(@RequestParam String routeId, @RequestParam int page) {
        try {
            List<RoutePoint> ass = routeService
                    .getRoutePoints(routeId, page);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getRoutePoints failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getAssociationRoutePoints")
    public ResponseEntity<Object> getAssociationRoutePoints(@RequestParam String associationId) {
        try {
            List<RoutePoint> ass = routeService
                    .getAssociationRoutePoints(associationId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getAssociationRoutePoints failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getAssociationRouteCities")
    public ResponseEntity<Object> getAssociationRouteCities(@RequestParam String associationId) {
        try {
            List<RouteCity> ass = routeService
                    .getAssociationRouteCities(associationId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getAssociationRouteCities failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getAssociationRouteLandmarks")
    public ResponseEntity<Object> getAssociationRouteLandmarks(@RequestParam String associationId) {
        try {
            List<RouteLandmark> ass = routeService
                    .getAssociationRouteLandmarks(associationId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getAssociationRouteLandmarks failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getRouteCities")
    public ResponseEntity<Object> getRouteCities(@RequestParam String routeId) {
        try {
            List<RouteCity> ass = routeService
                    .getRouteCities(routeId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getRouteCities failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getMarshalDispatchRecords")
    public ResponseEntity<Object> getMarshalDispatchRecords(@RequestParam String userId, @RequestParam String startDate) {
        try {
            List<DispatchRecord> ass = dispatchService
                    .getMarshalDispatchRecords(userId, startDate);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getMarshalDispatchRecords failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }
    @GetMapping("/getAssociationAmbassadorCheckIn")
    public ResponseEntity<Object> getAssociationAmbassadorCheckIn(@RequestParam String associationId, @RequestParam String startDate) {
        try {
            List<AmbassadorCheckIn> ass = ambassadorService
                    .getAssociationAmbassadorCheckIn(associationId, startDate);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getAssociationAmbassadorCheckIn failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }
    @GetMapping("/getVehicleAmbassadorCheckIn")
    public ResponseEntity<Object> getVehicleAmbassadorCheckIn(@RequestParam String vehicleId, @RequestParam String startDate) {
        try {
            List<AmbassadorCheckIn> ass = ambassadorService
                    .getVehicleAmbassadorCheckIn(vehicleId, startDate);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getVehicleAmbassadorCheckIn failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }
    @GetMapping("/getUserAmbassadorCheckIn")
    public ResponseEntity<Object> getUserAmbassadorCheckIn(@RequestParam String userId, @RequestParam String startDate) {
        try {
            List<AmbassadorCheckIn> ass = ambassadorService
                    .getUserAmbassadorCheckIn(userId, startDate);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getUserAmbassadorCheckIn failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getAssociationAmbassadorPassengerCounts")
    public ResponseEntity<Object> getAssociationAmbassadorPassengerCounts(@RequestParam String associationId, @RequestParam String startDate) {
        try {
            List<AmbassadorPassengerCount> ass = ambassadorService
                    .getAssociationAmbassadorPassengerCounts(associationId, startDate);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getAssociationAmbassadorPassengerCounts failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }
    @GetMapping("/getUserAmbassadorPassengerCounts")
    public ResponseEntity<Object> getUserAmbassadorPassengerCounts(@RequestParam String userId, @RequestParam String startDate) {
        try {
            List<AmbassadorPassengerCount> ass = ambassadorService
                    .getUserAmbassadorPassengerCounts(userId, startDate);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getUserAmbassadorPassengerCounts failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }
    @GetMapping("/getVehicleAmbassadorPassengerCounts")
    public ResponseEntity<Object> getVehicleAmbassadorPassengerCounts(@RequestParam String vehicleId, @RequestParam String startDate) {
        try {
            List<AmbassadorPassengerCount> ass = ambassadorService
                    .getVehicleAmbassadorPassengerCounts(vehicleId, startDate);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getVehicleAmbassadorPassengerCounts failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }
    @GetMapping("/getOwnersBag")
    public ResponseEntity<Object> getOwnersBag(@RequestParam String userId, @RequestParam String startDate) {
        try {
            BigBag ass = dispatchService
                    .getOwnersBag(userId, startDate);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getOwnersBag failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }


    @GetMapping("/getVehicleCounts")
    public ResponseEntity<Object> getVehicleCounts(@RequestParam String vehicleId) {
        try {
            List<CounterBag> ass = dispatchService
                    .getVehicleCounts(vehicleId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getVehicleCounts failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getVehicleCountsByDate")
    public ResponseEntity<Object> getVehicleCountsByDate(
            @RequestParam String vehicleId, @RequestParam String startDate) {
        try {
            List<CounterBag> ass = dispatchService
                    .getVehicleCountsByDate(vehicleId, startDate);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getVehicleCountsByDate failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/countVehicleArrivals")
    public ResponseEntity<Object> countVehicleArrivals(@RequestParam String vehicleId) {
        try {
            long ass = dispatchService
                    .countVehicleArrivals(vehicleId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "countVehicleArrivals failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/countVehicleDepartures")
    public ResponseEntity<Object> countVehicleDeparture(@RequestParam String vehicleId) {
        try {
            long ass = dispatchService
                    .countVehicleDepartures(vehicleId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "countVehicleDepartures failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/countVehicleDispatches")
    public ResponseEntity<Object> countVehicleDispatches(@RequestParam String vehicleId) {
        try {
            long ass = dispatchService
                    .countVehicleDispatches(vehicleId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "countVehicleDispatches failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/countVehicleHeartbeats")
    public ResponseEntity<Object> countVehicleHeartbeats(@RequestParam String vehicleId) {
        try {
            long ass = heartbeatService
                    .countVehicleHeartbeats(vehicleId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "countVehicleHeartbeats failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }
    @GetMapping("/getOwnerVehicles")
    public ResponseEntity<Object> getOwnerVehicles(@RequestParam String userId) {
        try {
            List<Vehicle> ass = vehicleService
                    .getOwnerVehicles(userId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getOwnerVehicles failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }
    @GetMapping("/downloadExampleVehiclesFile")
    public ResponseEntity<Object> downloadExampleVehiclesFile(HttpServletRequest request, final HttpServletResponse response) {
        try {
            File file = associationService
                    .downloadExampleVehiclesFile();
            response.setHeader("Cache-Control", "must-revalidate");
            response.setHeader("Pragma", "public");
            response.setHeader("Content-Transfer-Encoding", "binary");
            response.setHeader("Content-disposition", "attachment; ");

            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            FileCopyUtils.copy(bufferedInputStream, response.getOutputStream());
            return ResponseEntity.ok(file);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "downloadExampleVehiclesFile failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/downloadExampleUsersFile")
    public ResponseEntity<Object> downloadExampleUsersFile(HttpServletRequest request, final HttpServletResponse response) {
        try {
            File file = associationService
                    .downloadExampleUsersFile();
            response.setHeader("Cache-Control", "must-revalidate");
            response.setHeader("Pragma", "public");
            response.setHeader("Content-Transfer-Encoding", "binary");
            response.setHeader("Content-disposition", "attachment; ");

            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            FileCopyUtils.copy(bufferedInputStream, response.getOutputStream());
            return ResponseEntity.ok(file);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "downloadExampleVehiclesFile failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getVehicleMediaRequests")
    public ResponseEntity<Object> getVehicleMediaRequests(@RequestParam String vehicleId) {
        try {
            List<VehicleMediaRequest> ass = mediaService
                    .getVehicleMediaRequests(vehicleId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getVehicleMediaRequests failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }
    @GetMapping("/getAssociationVehicleMediaRequests")
    public ResponseEntity<Object> getAssociationVehicleMediaRequests(
            @RequestParam String associationId, @RequestParam String startDate) {
        try {
            List<VehicleMediaRequest> ass = mediaService
                    .getAssociationVehicleMediaRequests(associationId, startDate);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getAssociationVehicleMediaRequests failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getVehiclePhotos")
    public ResponseEntity<Object> getVehiclePhotos(@RequestParam String vehicleId) {
        try {
            List<VehiclePhoto> ass = mediaService
                    .getVehiclePhotos(vehicleId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getVehiclePhotos failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }
    @GetMapping("/getVehicleVideos")
    public ResponseEntity<Object> getVehicleVideos(@RequestParam String vehicleId) {
        try {
            List<VehicleVideo> ass = mediaService
                    .getVehicleVideos(vehicleId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getVehicleVideos failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }


    @GetMapping("/getCalculatedDistances")
    public ResponseEntity<Object> getCalculatedDistances(@RequestParam String routeId) {
        try {
            List<CalculatedDistance> ass = routeService
                    .getCalculatedDistances(routeId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getCalculatedDistances failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/refreshRoute")
    public ResponseEntity<Object> refreshRoute(@RequestParam String routeId) {
        try {
            RouteBag bag = routeService
                    .refreshRoute(routeId);
            return ResponseEntity.ok(bag);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "refreshRoute failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getCountryStates")
    public ResponseEntity<Object> getCountryStates(@RequestParam String countryId) {
        try {
            List<State> ass = cityService
                    .getCountryStates(countryId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getCountryStates failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/findCitiesByLocation")
    public ResponseEntity<Object> findCitiesByLocation(@RequestParam double latitude,
                                                       @RequestParam double longitude,
                                                       @RequestParam int limit,
                                                       @RequestParam double radiusInKM) {
        try {
            List<City> cities = cityService.findCitiesByLocation(
                    latitude, longitude, radiusInKM, limit);
            return ResponseEntity.ok(cities);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(500,
                            "findCitiesByLocation failed: "
                                    + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/findRoutesByLocation")
    public ResponseEntity<Object> findRoutesByLocation(@RequestParam double latitude,
                                                       @RequestParam double longitude,
                                                       @RequestParam double radiusInKM) {
        try {
            List<Route> r = routeService.findRoutesByLocation(latitude, longitude, radiusInKM);
            return ResponseEntity.ok(r);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "findRoutesByLocation failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/findRouteLandmarksByLocation")
    public ResponseEntity<Object> findRouteLandmarksByLocation(@RequestParam String associationId, @RequestParam double latitude,
                                                               @RequestParam double longitude,
                                                               @RequestParam double radiusInKM) {
        try {
            List<RouteLandmark> r = routeService.findRouteLandmarksByLocation(
                    associationId,
                    latitude, longitude, radiusInKM);

            return ResponseEntity.ok(r);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "findRouteLandmarksByLocation failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/findLandmarksByLocation")
    public ResponseEntity<Object> findLandmarksByLocation(

            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam double radiusInKM) {
        try {
            List<Landmark> r = landmarkService.findLandmarksByLocation(latitude, longitude, radiusInKM);
            return ResponseEntity.ok(r);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "findLandmarksByLocation failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getRouteLandmarks")
    public ResponseEntity<Object> getRouteLandmarks(@RequestParam String routeId) {
        try {
            List<RouteLandmark> r = routeService.getRouteLandmarks(routeId);
            return ResponseEntity.ok(r);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getRouteLandmarks failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }
    @GetMapping("/getRouteUpdateRequests")
    public ResponseEntity<Object> getRouteUpdateRequests(@RequestParam String routeId) {
        try {
            List<RouteUpdateRequest> r = routeService.getRouteUpdateRequests(routeId);
            return ResponseEntity.ok(r);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getRouteUpdateRequests failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/findAssociationRoutesByLocation")
    public ResponseEntity<Object> findAssociationRoutesByLocation(@RequestParam String associationId, @RequestParam double latitude,
                                                                  @RequestParam double longitude,
                                                                  @RequestParam double radiusInKM) {
        try {
            List<Route> r = routeService.findAssociationRoutesByLocation(associationId, latitude, longitude, radiusInKM);
            return ResponseEntity.ok(r);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "findAssociationRoutesByLocation failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getCountryCities")
    public ResponseEntity<Object> getCountryCities(@RequestParam String countryId) {
        try {
            List<City> cities = cityService.getCountryCities(countryId);
            return ResponseEntity.ok(cities);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getCountryCities failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getCountries")
    public ResponseEntity<Object> getCountries() {
        try {
            List<Country> cities = cityService.getCountries();
            return ResponseEntity.ok(cities);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomErrorResponse(400,
                            "getCountries failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }
}
