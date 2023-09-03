package com.boha.kasietransie.controllers;

import com.boha.kasietransie.data.*;
import com.boha.kasietransie.data.dto.*;
import com.boha.kasietransie.services.*;
import com.boha.kasietransie.util.CustomResponse;
import com.boha.kasietransie.util.E;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class DataController {
    private static final Gson G = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger logger = LoggerFactory.getLogger(DataController.class);

    private final MongoService mongoService;
    private final UserService userService;
    private final VehicleService vehicleService;
    private final AssociationService associationService;
    private final UserGeofenceService userGeofenceService;
    private final DispatchService dispatchService;
    private final LandmarkService landmarkService;
    private final RouteService routeService;
    private final HeartbeatService heartbeatService;
    private final LocationRequestService locationRequestService;
    final CityService cityService;
    final GeoHashFixer geoHashFixer;
    final MessagingService messagingService;
    final TextTranslationService textTranslationService;
    final CloudStorageUploaderService cloudStorageUploaderService;
    final MediaService mediaService;
    final AmbassadorService ambassadorService;
    final CommuterService commuterService;
    final DispatchAsyncHelperService dispatchAsyncHelperService;
    final TimeSeriesService timeSeriesService;

    @PostMapping("/createUser")
    public ResponseEntity<Object> createUser(@RequestBody User user) {

        try {
            User v = userService.createUser(user);
            return ResponseEntity.ok(v);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "createUser failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    /*

     */
    @PostMapping("/addCity")
    public ResponseEntity<Object> addCity(@RequestBody City city) {

        try {
            City v = cityService.addCity(city);
            return ResponseEntity.ok(v);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "addCity failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @GetMapping("/updateRouteColor")
    public ResponseEntity<Object> updateRouteColor(@RequestParam String routeId, @RequestParam String color) {

        try {
            Route v = routeService.updateRouteColor(routeId, color);
            return ResponseEntity.ok(v);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "updateRouteColor failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @GetMapping("/deleteRoutePointsFromIndex")
    public ResponseEntity<Object> deleteRoutePointsFromIndex(
            @RequestParam String routeId, @RequestParam int index) {

        try {
            List<RoutePoint> v = routeService.deleteRoutePointsFromIndex(routeId, index);
            return ResponseEntity.ok(v);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "deleteRoutePointsFromIndex failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @PostMapping("/addVehicle")
    public ResponseEntity<Object> addVehicle(@RequestBody Vehicle vehicle) {

        try {
            Vehicle v = vehicleService.addVehicle(vehicle);
            return ResponseEntity.ok(v);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "addVehicle failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @PostMapping("/addCommuter")
    public ResponseEntity<Object> addCommuter(@RequestBody Commuter commuter) {

        try {
            Commuter v = commuterService.addCommuter(commuter);
            return ResponseEntity.ok(v);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "addCommuter failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @PostMapping("/addCommuterRequest")
    public ResponseEntity<Object> addCommuterRequest(@RequestBody CommuterRequest request) {

        try {
            CommuterRequest v = commuterService.addCommuterRequest(request);
            return ResponseEntity.ok(v);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "addCommuterRequest failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @PostMapping("/updateVehicle")
    public ResponseEntity<Object> updateVehicle(@RequestBody Vehicle vehicle) {

        try {
            Vehicle v = vehicleService.updateVehicle(vehicle);
            return ResponseEntity.ok(v);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "updateVehicle failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @PostMapping("/addBasicLandmark")
    public ResponseEntity<Object> addLandmark(@RequestBody Landmark landmark) {

        try {
            logger.info(E.RED_APPLE + " DataController: adding landmark: " + G.toJson(landmark));
            Landmark v = landmarkService.addBasicLandmark(landmark);
            return ResponseEntity.ok(v);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "addLandmark failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @PostMapping("/addRouteLandmark")
    public ResponseEntity<Object> addRouteLandmark(@RequestBody RouteLandmark landmark) {

        try {
            logger.info(E.RED_APPLE + " DataController: adding route landmark: " + G.toJson(landmark));
            RouteLandmark v = routeService.addRouteLandmark(landmark);
            return ResponseEntity.ok(v);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "addRouteLandmark failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @PostMapping("/addRouteCity")
    public ResponseEntity<Object> addRouteCity(@RequestBody RouteCity landmark) {

        try {
            RouteCity v = routeService.addRouteCity(landmark);
            return ResponseEntity.ok(v);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "addRouteCity failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @PostMapping("/addSettingsModel")
    public ResponseEntity<Object> addSettingsModel(@RequestBody SettingsModel model) {

        try {
            SettingsModel v = associationService.addSettingsModel(model);
            return ResponseEntity.ok(v);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "addSettingsModel failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @PostMapping("/addRoute")
    public ResponseEntity<Object> addRoute(@RequestBody Route route) {

        try {
            Route v = routeService.addRoute(route);
            return ResponseEntity.ok(v);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "addRoute failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @PostMapping("/addRoutePoints")
    public ResponseEntity<Object> addRoutePoints(@RequestBody RoutePointList routePoints) {

        try {
            int v = routeService.addRoutePoints(routePoints.getRoutePoints());
            return ResponseEntity.ok(v);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "addRoutePoints failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @PostMapping("/addCalculatedDistances")
    public ResponseEntity<Object> addCalculatedDistances(@RequestBody CalculatedDistanceList calculatedDistances) {

        try {
            List<CalculatedDistance> v = routeService.addCalculatedDistances(calculatedDistances);
            return ResponseEntity.ok(v);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "addCalculatedDistances failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @PostMapping("/addDispatchRecord")
    public ResponseEntity<Object> addDispatchRecord(@RequestBody DispatchRecord dispatchRecord) {

        try {
            DispatchRecord v = dispatchService.addDispatchRecord(dispatchRecord);
            return ResponseEntity.ok(v);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "addDispatchRecord failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @PostMapping("/addDispatchRecords")
    public ResponseEntity<Object> addDispatchRecords(@RequestBody DispatchRecordList dispatchRecordList) {

        try {
            List<DispatchRecord> v = dispatchService.addDispatchRecords(dispatchRecordList);
            return ResponseEntity.ok(v);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "addDispatchRecords failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @PostMapping("/addVehicleHeartbeat")
    public ResponseEntity<Object> addVehicleHeartbeat(@RequestBody VehicleHeartbeat vehicleHeartbeat) {

        try {
            VehicleHeartbeat v = heartbeatService.addVehicleHeartbeat(vehicleHeartbeat);
            return ResponseEntity.ok(v);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "addVehicleHeartbeat failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @PostMapping("/addVehicleArrival")
    public ResponseEntity<Object> addVehicleArrival(@RequestBody VehicleArrival vehicleArrival) {

        try {
            VehicleArrival v = dispatchService.addVehicleArrival(vehicleArrival);
            return ResponseEntity.ok(v);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "addVehicleArrival failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @PostMapping("/addLocationRequest")
    public ResponseEntity<Object> addLocationRequest(@RequestBody LocationRequest locationRequest) {

        try {
            LocationRequest v = locationRequestService.addLocationRequest(locationRequest);
            return ResponseEntity.ok(v);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "addLocationRequest failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @PostMapping("/addLocationResponse")
    public ResponseEntity<Object> addLocationResponse(@RequestBody LocationResponse locationResponse) {

        try {
            LocationResponse v = locationRequestService.addLocationResponse(locationResponse);
            return ResponseEntity.ok(v);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "addLocationResponse failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @PostMapping("/addUserGeofenceEvent")
    public ResponseEntity<Object> addUserGeofenceEvent(@RequestBody UserGeofenceEvent userGeofenceEvent) {

        try {
            UserGeofenceEvent v = userGeofenceService.addUserGeofenceEvent(userGeofenceEvent);
            return ResponseEntity.ok(v);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "addUserGeofenceEvent failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @PostMapping("/addVehicleDeparture")
    public ResponseEntity<Object> addVehicleDeparture(@RequestBody VehicleDeparture vehicleDeparture) {

        try {
            VehicleDeparture v = dispatchService.addVehicleDeparture(vehicleDeparture);
            return ResponseEntity.ok(v);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "addVehicleDeparture failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @PostMapping("/addAppError")
    public ResponseEntity<Object> addAppError(@RequestBody AppError appError) {

        try {
            AppError v = associationService.addAppError(appError);
            return ResponseEntity.ok(v);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "addAppError failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @PostMapping("/addAppErrors")
    public ResponseEntity<Object> addAppErrors(@RequestBody AppErrors appErrors) {

        try {
            List<AppError> v = associationService.addAppErrors(appErrors);
            return ResponseEntity.ok(v);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "addAppErrors failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @PostMapping("/registerAssociation")
    public ResponseEntity<Object> registerAssociation(@RequestBody Association association) {

        try {
            return ResponseEntity.ok(associationService.registerAssociation(association));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "registerAssociation failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @PostMapping("/updateUser")
    public ResponseEntity<Object> updateUser(@RequestBody User user) {

        try {
            return ResponseEntity.ok(userService.updateUser(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "updateUser failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @PostMapping("/addRouteUpdateRequest")
    public ResponseEntity<Object> addRouteUpdateRequest(
            @RequestBody RouteUpdateRequest routeUpdateRequest) {

        try {
            return ResponseEntity.ok(routeService.addRouteUpdateRequest(
                    routeUpdateRequest));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "addRouteUpdateRequest failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @PostMapping("/addVehicleMediaRequest")
    public ResponseEntity<Object> addVehicleMediaRequest(
            @RequestBody VehicleMediaRequest vehicleMediaRequest) {

        try {
            return ResponseEntity.ok(routeService.addVehicleMediaRequest(
                    vehicleMediaRequest));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "addVehicleMediaRequest failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @GetMapping("/sendVehicleUpdateMessage")
    public ResponseEntity<Object> sendVehicleUpdateMessage(
            @RequestParam String associationId,
            @RequestParam String vehicleId) {

        try {
            return ResponseEntity.ok(messagingService.sendVehicleUpdateMessage(
                    associationId, vehicleId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "sendVehicleUpdateMessage failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @PostMapping("/sendVehicleMediaRequestMessage")
    public ResponseEntity<Object> sendVehicleMediaRequestMessage(
            @RequestParam VehicleMediaRequest request) {

        try {
            return ResponseEntity.ok(messagingService.sendVehicleMediaRequestMessage(
                    request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "sendVehicleMediaRequestMessage failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @GetMapping("/generateFakeAssociation")
    public ResponseEntity<Object> generateFakeAssociation(@RequestParam String testCellphoneNumber,
                                                          @RequestParam String firstName,
                                                          @RequestParam String lastName,
                                                          @RequestParam String associationName,
                                                          @RequestParam String email) {

        try {
            return ResponseEntity.ok(associationService.generateFakeAssociation(
                    associationName, email, testCellphoneNumber, firstName, lastName));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "generateFakeAssociation failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @GetMapping("/generateFakeVehiclesFromFile")
    public ResponseEntity<Object> generateFakeVehiclesFromFile(@RequestParam String associationId) {

        try {
            return ResponseEntity.ok(vehicleService.generateFakeVehiclesFromFile(
                    associationId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "generateFakeVehiclesFromFile failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @GetMapping("/generateFakeVehicles")
    public ResponseEntity<Object> generateFakeVehicles(@RequestParam String associationId,
                                                       @RequestParam int number) {

        try {
            return ResponseEntity.ok(vehicleService.generateFakeVehicles(
                    associationId, number));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "generateFakeVehicles failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @GetMapping("/deleteRoutePoint")
    public ResponseEntity<Object> deleteRoutePoint(@RequestParam String routePointId) {

        try {
            return ResponseEntity.ok(routeService.deleteRoutePoint(routePointId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "deleteRoutePoint failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @GetMapping("/deleteByLandmarkId")
    public ResponseEntity<Object> deleteByLandmarkId(@RequestParam String landmarkId) {

        try {
            return ResponseEntity.ok(landmarkService.deleteLandmark(landmarkId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "deleteByLandmarkId failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @PostMapping("uploadUserFile")
    public ResponseEntity<Object> uploadUserFile(
            @RequestParam String associationId,
            @RequestPart MultipartFile document) throws IOException {

        List<User> users;
        String doc = document.getOriginalFilename();
        if (doc == null) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "Problem with user file ",
                            new DateTime().toDateTimeISO().toString()));
        }
        File file = new File(doc);
        Files.write(document.getBytes(), file);
        logger.info("\uD83C\uDF3C\uD83C\uDF3C we have a file: " + file.getName());
        if (file.getName().contains(".csv")) {
            logger.info("\uD83C\uDF3C\uD83C\uDF3C csv file to process: " + file.getName());
            try {
                users = userService.importUsersFromCSV(file, associationId);
                if (users.isEmpty()) {
                    logger.info("\uD83C\uDF3C\uD83C\uDF3C no users created ... wtfObject ");
                    return ResponseEntity.badRequest().body(
                            new CustomResponse(400,
                                    "Failed to create users; no users in file or file is not .json or .csv ",
                                    new DateTime().toDateTimeISO().toString()));
                }
                return ResponseEntity.ok(users);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(
                        new CustomResponse(400,
                                "Failed to create users: " + e.getMessage(),
                                new DateTime().toDateTimeISO().toString()));
            }
        }
        if (file.getName().contains(".json")) {
            logger.info("\uD83C\uDF3C\uD83C\uDF3C json file to process: " + file.getName());
            try {
                users = userService.importUsersFromJSON(file, associationId);
                if (users.isEmpty()) {
                    logger.info("\uD83C\uDF3C\uD83C\uDF3C no users created ... wtfObject ");
                    return ResponseEntity.badRequest().body(
                            new CustomResponse(400,
                                    "Failed to create users; no users in file or file is not .json or .csv ",
                                    new DateTime().toDateTimeISO().toString()));
                }

                return ResponseEntity.ok(users);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(
                        new CustomResponse(400,
                                "Failed to create users: " + e.getMessage(),
                                new DateTime().toDateTimeISO().toString()));
            }
        }
        if (file.exists()) {
            Path path = Paths.get(file.getAbsolutePath());
            java.nio.file.Files.delete(path);
        }

        return ResponseEntity.badRequest().body(
                new CustomResponse(400,
                        "Failed to create users; no users in file or file is not .json or .csv ",
                        new DateTime().toDateTimeISO().toString()));
    }

    @PostMapping("uploadVehicleFile")
    public ResponseEntity<Object> uploadVehicleFile(
            @RequestParam String associationId,
            @RequestPart MultipartFile document) throws IOException {

        List<Vehicle> vehicles;
        String doc = document.getOriginalFilename();
        if (doc == null) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "Problem with user file ",
                            new DateTime().toDateTimeISO().toString()));
        }
        File file = new File(doc);
        Files.write(document.getBytes(), file);
        logger.info("\uD83C\uDF3C\uD83C\uDF3C we have a file: " + file.getName());
        if (file.getName().contains(".csv")) {
            logger.info("\uD83C\uDF3C\uD83C\uDF3C csv file to process: " + file.getName());
            try {
                vehicles = vehicleService.importVehiclesFromCSV(file, associationId);
                if (vehicles.isEmpty()) {
                    logger.info("\uD83C\uDF3C\uD83C\uDF3C no vehicles created ... wtf?");
                    return ResponseEntity.badRequest().body(
                            new CustomResponse(400,
                                    "Failed to create vehicles; no vehicles in file or file is not .json or .csv ",
                                    new DateTime().toDateTimeISO().toString()));
                }

                return ResponseEntity.ok(vehicles);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(
                        new CustomResponse(400,
                                "Failed to create vehicles: " + e.getMessage(),
                                new DateTime().toDateTimeISO().toString()));
            }
        }
        if (file.getName().contains(".json")) {
            logger.info("\uD83C\uDF3C\uD83C\uDF3C json file to process: " + file.getName());
            try {
                vehicles = vehicleService.importVehiclesFromJSON(file, associationId);
                if (vehicles.isEmpty()) {
                    logger.info("\uD83C\uDF3C\uD83C\uDF3C no vehicles created ... wtfObject ");
                    return ResponseEntity.badRequest().body(
                            new CustomResponse(400,
                                    "Failed to create vehicles; no vehicles in file or file is not .json or .csv ",
                                    new DateTime().toDateTimeISO().toString()));
                }

                return ResponseEntity.ok(vehicles);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(
                        new CustomResponse(400,
                                "Failed to create vehicles: " + e.getMessage(),
                                new DateTime().toDateTimeISO().toString()));
            }
        }
        if (file.exists()) {
            Path path = Paths.get(file.getAbsolutePath());
            java.nio.file.Files.delete(path);

        }

        return ResponseEntity.badRequest().body(
                new CustomResponse(400,
                        "Failed to create vehicles; no vehicles in file or file is not .json or .csv ",
                        new DateTime().toDateTimeISO().toString()));
    }

    @PostMapping("upLoadExampleFiles")
    public ResponseEntity<Object> upLoadExampleFiles(
            @RequestPart MultipartFile userCSV,
            @RequestPart MultipartFile vehicleCSV,
            @RequestPart MultipartFile userJSON,
            @RequestPart MultipartFile vehicleJSON) throws IOException {

        String userCSVFileName = userCSV.getOriginalFilename();
        String vehicleCSVFileName = vehicleCSV.getOriginalFilename();

        String userJSONFileName = userJSON.getOriginalFilename();
        String vehicleJSONFileName = vehicleJSON.getOriginalFilename();

        if (userCSVFileName == null) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "Problem with user csv file ",
                            new DateTime().toDateTimeISO().toString()));
        }
        if (userJSONFileName == null) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "Problem with user json file ",
                            new DateTime().toDateTimeISO().toString()));
        }
        if (vehicleCSVFileName == null) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "Problem with vehicle csv file ",
                            new DateTime().toDateTimeISO().toString()));
        }
        if (vehicleJSONFileName == null) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "Problem with vehicle json file ",
                            new DateTime().toDateTimeISO().toString()));
        }
        //
        logger.info(E.CHECK+" userCSVFileName: " + userCSVFileName);
        logger.info(E.CHECK+" userJSONFileName: " + userJSONFileName);

        logger.info(E.CHECK+" vehicleCSVFileName: " + vehicleCSVFileName);
        logger.info(E.CHECK+" vehicleJSONFileName: " + vehicleJSONFileName);


        File userCSVFile = new File(userCSVFileName);
        Files.write(userCSV.getBytes(), userCSVFile);

        File vehicleCSVFile = new File(vehicleCSVFileName);
        Files.write(vehicleCSV.getBytes(), vehicleCSVFile);

        File vehicleJSONFile = new File(vehicleJSONFileName);
        Files.write(vehicleJSON.getBytes(), vehicleJSONFile);

        File userJSONFile = new File(userJSONFileName);
        Files.write(userJSON.getBytes(), userJSONFile);

        List<File> files = new ArrayList<>();
        files.add(userJSONFile);
        files.add(userCSVFile);
        files.add(vehicleJSONFile);
        files.add(vehicleCSVFile);

        logger.info("\uD83C\uDF3C\uD83C\uDF3C we have files to work with: " + files.size());
        try {
            List<ExampleFile> cr = associationService.upLoadExampleFiles(files);
            return ResponseEntity.ok(cr);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "Failed to upload example files: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @GetMapping("/addCountriesStatesCitiesToDB")
    public ResponseEntity<Object> addCountriesStatesCitiesToDB() {

        try {
            return ResponseEntity.ok(mongoService.addCountriesStatesCitiesToDB());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "addCountriesStatesCitiesToDB failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @GetMapping("/addSouthAfricanCitiesToDB")
    public ResponseEntity<Object> addSouthAfricanCitiesToDB() {

        try {
            return ResponseEntity.ok(mongoService.addSouthAfricanCitiesToDB());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "addSouthAfricanCitiesToDB failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @GetMapping("/checkDatabaseTotals")
    public ResponseEntity<Object> checkDatabaseTotals() {

        try {
            return ResponseEntity.ok(mongoService.checkDatabaseTotals());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "checkDatabaseTotals failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @GetMapping("/generateTranslations")
    public ResponseEntity<Object> generateTranslations() {

        try {
            String m = textTranslationService.generateTranslations(true);
            return ResponseEntity.ok(m);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "generateTranslations failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @PostMapping("/generateInputStrings")
    public ResponseEntity<Object> generateInputStrings(@RequestBody List<TranslationInput> inputStrings) {

        try {
            String m = textTranslationService.generateInputStrings(inputStrings);
            return ResponseEntity.ok(m);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "generateInputStrings failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @GetMapping("/createDartFile")
    public ResponseEntity<Object> createDartFile() {

        try {
            String m = textTranslationService.createDartFile(true);
            return ResponseEntity.ok(m);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "createDartFile failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @PostMapping("uploadFile")
    public ResponseEntity<Object> uploadFile(
            @RequestParam String objectName,
            @RequestPart MultipartFile document) throws IOException {

        /* todo - research sending media files zipped from phone */

        try {
            String doc = document.getOriginalFilename();
            assert doc != null;
            File file = new File(doc);
            byte[] bytes = document.getBytes();

            Files.write(bytes, file);
            String url = cloudStorageUploaderService.uploadFile(doc, file);
            boolean ok = file.delete();
            if (ok) {
                logger.info(E.RED_APPLE + E.RED_APPLE +
                        " cloud storage upload file deleted: " + true);
            }
            return ResponseEntity.ok(url);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "uploadFile failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @PostMapping("addVehiclePhoto")
    public ResponseEntity<Object> addVehiclePhoto(
            @RequestBody VehiclePhoto vehiclePhoto) {
        try {
            VehiclePhoto v = mediaService.addVehiclePhoto(vehiclePhoto);
            return ResponseEntity.ok(v);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "addVehiclePhoto failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @PostMapping("addVehicleVideo")
    public ResponseEntity<Object> addVehicleVideo(
            @RequestBody VehicleVideo vehiclePhoto) {
        try {
            VehicleVideo v = mediaService.addVehicleVideo(vehiclePhoto);
            return ResponseEntity.ok(v);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "addVehicleVideo failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @PostMapping("addAmbassadorCheckIn")
    public ResponseEntity<Object> addAmbassadorCheckIn(
            @RequestBody AmbassadorCheckIn checkIn) {
        try {
            AmbassadorCheckIn v = ambassadorService.addAmbassadorCheckIn(checkIn);
            return ResponseEntity.ok(v);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "addAmbassadorCheckIn failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @PostMapping("addAmbassadorPassengerCount")
    public ResponseEntity<Object> addAmbassadorPassengerCount(
            @RequestBody AmbassadorPassengerCount count) {
        try {
            AmbassadorPassengerCount v = ambassadorService.addAmbassadorPassengerCount(count);
            return ResponseEntity.ok(v);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "addAmbassadorPassengerCount failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @GetMapping("/changeFakeVehicleOwner")
    public ResponseEntity<Object> changeFakeVehicleOwner(@RequestParam String userId) {

        try {
            int m = vehicleService.changeFakeVehicleOwner(userId);
            return ResponseEntity.ok(m);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "changeFakeVehicleOwner failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @GetMapping("/generateHeartbeats")
    public ResponseEntity<Object> generateHeartbeats(@RequestParam String associationId,
                                                     @RequestParam int numberOfCars,
                                                     @RequestParam int intervalInSeconds) {
        try {

            List<VehicleHeartbeat> m = vehicleService.generateHeartbeats(
                    associationId, numberOfCars, intervalInSeconds);
            return ResponseEntity.ok(m);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "generateHeartbeats failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @GetMapping("/addAssociationToken")
    public ResponseEntity<Object> addAssociationToken(@RequestParam String associationId,
                                                      @RequestParam String userId,
                                                      @RequestParam String token) {
        try {
            AssociationToken m = messagingService.addAssociationToken(
                    associationId, userId, token);
            return ResponseEntity.ok(m);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "addAssociationToken failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }


    @PostMapping("/generateRouteHeartbeats")
    public ResponseEntity<Object> generateRouteHeartbeats(@RequestBody GenerationRequest request) {
        try {
            heartbeatService
                    .generateRouteHeartbeats(request);
            CustomResponse r = new CustomResponse(200,
                    "Heartbeat Generation started", DateTime.now().toDateTimeISO().toString());
            return ResponseEntity.ok(r);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "generateRouteHeartbeats failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/generateVehicleRouteHeartbeats")
    public ResponseEntity<Object> generateVehicleRouteHeartbeats(String vehicleId,
                                                                 String routeId,
                                                                 String startDate, int intervalInSeconds) {
        try {
            heartbeatService
                    .generateVehicleRouteHeartbeats(vehicleId, routeId, startDate, intervalInSeconds);
            CustomResponse r = new CustomResponse(200,
                    "Vehicle Heartbeat Generation started", DateTime.now().toDateTimeISO().toString());
            return ResponseEntity.ok(r);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "generateVehicleRouteHeartbeats failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }


    @GetMapping("/generateCommuterRequests")
    public ResponseEntity<Object> generateCommuterRequests(
            @RequestParam String associationId) {
        try {

            List<CommuterRequest> m = dispatchAsyncHelperService.generateCommuterRequestsInParallel(
                    associationId);
            return ResponseEntity.ok(m);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "generateCommuterRequest failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @GetMapping("/generateRouteCommuterRequests")
    public ResponseEntity<Object> generateRouteCommuterRequests(
            @RequestParam String routeId) {
        try {
            commuterService.generateRouteCommuterRequests(
                    routeId);
            CustomResponse c = new CustomResponse(200,
                    "Commuter request generation",
                    DateTime.now().toDateTimeISO().toString());
            return ResponseEntity.ok(c);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "generateRouteCommuterRequests failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/generateAmbassadorPassengerCounts")
    public ResponseEntity<Object> generateAmbassadorPassengerCounts(
            @RequestParam String associationId,
            @RequestParam int numberOfCars,
            @RequestParam int intervalInSeconds) {
        try {
            List<AmbassadorPassengerCount> m = ambassadorService.generateAmbassadorPassengerCounts(
                    associationId, numberOfCars, intervalInSeconds);
            return ResponseEntity.ok(m);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "generateAmbassadorPassengerCounts failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @GetMapping("/buildTimeSeries")
    public ResponseEntity<Object> buildTimeSeries(
            @RequestParam String collectionName,
            @RequestParam String metaField,
            @RequestParam String timeField) {
        try {
            CustomResponse m = timeSeriesService.buildTimeSeries(
                    collectionName, timeField, metaField);
            return ResponseEntity.ok(m);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "buildTimeSeries failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @GetMapping("/addHeartbeatTimeSeries")
    public ResponseEntity<Object> addHeartbeatTimeSeries(@RequestParam String associationId,
                                                         @RequestParam String vehicleId,
                                                         @RequestParam String vehicleReg) {
        try {
            CustomResponse m = timeSeriesService.addHeartbeatTimeSeries(associationId, vehicleId, vehicleReg);
            return ResponseEntity.ok(m);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "addHeartbeatTimeSeries failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @GetMapping("/generateRoutePassengerCounts")
    public ResponseEntity<Object> generateRoutePassengerCounts(
            @RequestParam String routeId,
            @RequestParam int numberOfCars,
            @RequestParam int intervalInSeconds) {
        try {
            List<AmbassadorPassengerCount> m = ambassadorService.generateRoutePassengerCounts(
                    routeId, numberOfCars, intervalInSeconds);
            return ResponseEntity.ok(m);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "generateRoutePassengerCounts failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }


    @PostMapping("/generateRouteDispatchRecords")
    public ResponseEntity<Object> generateRouteDispatchRecords(
            @RequestBody GenerationRequest request) {
        try {
            dispatchAsyncHelperService
                    .generateRouteDispatchRecordsInParallel(request);
            CustomResponse c = new CustomResponse(200, "Dispatch record generation",
                    DateTime.now().toDateTimeISO().toString());
            return ResponseEntity.ok(c);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "generateRouteDispatchRecordsInParallel failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @PostMapping("/addRouteAssignments")
    public ResponseEntity<Object> addRouteAssignments(
            @RequestBody RouteAssignmentList assignments) {
        try {
            List<RouteAssignment> m = vehicleService.addRouteAssignments(assignments);
            return ResponseEntity.ok(m);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "addRouteAssignments failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }


    @GetMapping("/fixOwnerToPassengerCounts")
    public ResponseEntity<Object> fixOwnerToPassengerCounts(@RequestParam String userId,
                                                            @RequestParam String ownerId,
                                                            @RequestParam String ownerName) {

        try {
            String m = dispatchService.fixOwnerToPassengerCounts(userId, ownerId, ownerName);
            return ResponseEntity.ok(m);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "fixOwnerToPassengerCounts failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }


    @GetMapping("/recreateAllQRCodes")
    public ResponseEntity<Object> recreateAllQRCodes(@RequestParam String associationId) {

        try {
            return ResponseEntity.ok(vehicleService.recreateAllQRCodes(associationId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "recreateAllQRCodes failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }

    }

    @GetMapping("/")

    public String ping() {
        return "Ola BRICS!! - KasieTransie waiting for you at the Rank! \uD83C\uDF50\uD83C\uDF50\uD83C\uDF50\uD83C\uDF50";
    }

}
