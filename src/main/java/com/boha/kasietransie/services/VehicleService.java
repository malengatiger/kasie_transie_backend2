package com.boha.kasietransie.services;

import com.boha.kasietransie.data.dto.*;
import com.boha.kasietransie.data.repos.*;
import com.boha.kasietransie.util.Constants;
import com.boha.kasietransie.util.E;
import com.boha.kasietransie.util.FileToVehicles;
import com.boha.kasietransie.util.VehicleUploadResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
//
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;


@Service
@RequiredArgsConstructor

public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleHeartbeatRepository vehicleHeartbeatRepository;
    private final AssociationRepository associationRepository;
    private final ResourceLoader resourceLoader;
    final UserRepository userRepository;
    final UserService userService;
    final HeartbeatService heartbeatService;
    final CloudStorageUploaderService cloudStorageUploaderService;

    final MongoTemplate mongoTemplate;
    final MessagingService messagingService;
    final RouteService routeService;
    final RouteRepository routeRepository;

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger logger = Logger.getLogger(VehicleService.class.getSimpleName());

    private static final String XX = E.PRESCRIPTION + E.PRESCRIPTION + E.PRESCRIPTION;

    private static final String MM = "\uD83D\uDC26\uD83D\uDC26\uD83D\uDC26\uD83D\uDC26\uD83D\uDC26\uD83D\uDC26\uD83D\uDC26";





    Random random = new Random(System.currentTimeMillis());

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

    public List<VehicleHeartbeat> generateHeartbeats(String associationId, int numberOfCars,
                                                     int intervalInSeconds) {
        List<Vehicle> all = vehicleRepository.findByAssociationId(associationId);
        logger.info(E.BLUE_DOT + " found " + all.size()
                + " cars for association: " + associationId + " ----- " + E.RED_DOT + E.RED_DOT);

        List<VehicleHeartbeat> heartbeats = new ArrayList<>();
        List<Vehicle> vehicleList = getCars(all, numberOfCars);

        logger.info(E.BLUE_DOT + " processing " + vehicleList.size()
                + " cars for heartbeat generation ...");
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

        int heartbeatCount = 0;

        HashMap<String, List<RoutePoint>> hashMap = new HashMap<>();

        for (Vehicle vehicle : vehicleList) {
            //move car along the route - ascending by index
            int index = random.nextInt(filteredRoutes.size() - 1);
            Route route = filteredRoutes.get(index);

            List<RoutePoint> points;
            if (hashMap.get(route.getRouteId()) != null) {
                points = hashMap.get(route.getRouteId());
            } else {
                points = getPoints(route);
                hashMap.put(route.getRouteId(), points);
            }

            List<Integer> indices = getSortedIndices(points);
            DateTime minutesAgo = DateTime.now().toDateTimeISO().minusMinutes(30);

            logger.info("\n\n" + E.BLUE_DOT + E.BLUE_DOT + E.BLUE_DOT + E.BLUE_DOT
                    + route.getName() + " will be used for "
                    + vehicle.getVehicleReg() + E.RED_APPLE + " starting at: " + minutesAgo +
                    " number of heartbeats to generate on route: " + E.FERN + " " + indices.size());

            for (Integer routePointIndex : indices) {
                RoutePoint rp = points.get(routePointIndex);
                VehicleHeartbeat heartbeat = getVehicleHeartbeat(vehicle, minutesAgo, rp);
                VehicleHeartbeat addedVehicleHeartbeat = heartbeatService.addVehicleHeartbeat(heartbeat);
                heartbeats.add(addedVehicleHeartbeat);

                heartbeatCount++;
                logger.info(E.HEART_BLUE + E.HEART_BLUE + E.HEART_BLUE + E.HEART_BLUE
                        + " heartbeat added. "
                        + E.RED_CAR + " " + heartbeat.getVehicleReg()
                        + " " + heartbeat.getCreated() + E.RED_APPLE + " index: " + routePointIndex
                        + " owner: " + heartbeat.getOwnerName() + " " + E.RED_APPLE
                        + " heartbeatCount so far: " + heartbeatCount + " created: " + heartbeat.getCreated());
                //
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
            }

        }

        return heartbeats;
    }

    public List<VehicleHeartbeat> generateRouteHeartbeats(String routeId, int numberOfCars,
                                                          int intervalInSeconds) {
        logger.info(E.BLUE_DOT + " generateRouteHeartbeats starting for route " + routeId + " cars: " + numberOfCars+ E.FERN);

        Route route;
        List<Route> routes = routeRepository.findByRouteId(routeId);
        if (routes.isEmpty()) {
            return new ArrayList<>();
        }
        route = routes.get(0);
        List<Vehicle> all = vehicleRepository.findByAssociationId(route.getAssociationId());
        logger.info(E.BLUE_DOT + " found " + all.size()
                + " cars for association: " + route.getAssociationName() + " ----- " + E.RED_DOT + E.RED_DOT);

        List<VehicleHeartbeat> heartbeats = new ArrayList<>();
        List<Vehicle> vehicleList = getCars(all, numberOfCars);

        logger.info(E.BLUE_DOT + " processing " + vehicleList.size()
                + " cars for heartbeat generation ...");
        int heartbeatCount = 0;

        List<RoutePoint> points = new ArrayList<>();
        boolean stop = false;
        int index = 0;
        while (!stop) {
            List<RoutePoint> list = routeService.getRoutePoints(routeId, index);
            if (list.isEmpty()) {
                stop = true;
            } else {
                points.addAll(list);
            }
            index++;
        }

        logger.info("\n\n" + E.BLUE_DOT + E.BLUE_DOT + E.BLUE_DOT + E.BLUE_DOT
                + route.getName() + " will be used for "
                + E.RED_APPLE + E.FERN);

        for (Vehicle vehicle : vehicleList) {
            //move car along the route - ascending by index
            List<Integer> indices = getSortedIndices(points);
            DateTime minutesAgo = DateTime.now().toDateTimeISO().minusMinutes(30);

            logger.info(E.BLUE_DOT + E.BLUE_DOT + E.BLUE_DOT + E.BLUE_DOT
                    + route.getName() + " will be used for "
                    + vehicle.getVehicleReg() + E.RED_APPLE + " starting at: " + minutesAgo +
                    " number of heartbeats to generate on route: " + E.FERN + " " + indices.size());

            for (Integer routePointIndex : indices) {
                RoutePoint rp = points.get(routePointIndex);
                VehicleHeartbeat heartbeat = getVehicleHeartbeat(vehicle, minutesAgo, rp);
                VehicleHeartbeat addedVehicleHeartbeat = heartbeatService.addVehicleHeartbeat(heartbeat);
                heartbeats.add(addedVehicleHeartbeat);

                heartbeatCount++;
                logger.info(E.HEART_BLUE + E.HEART_BLUE + E.HEART_BLUE + E.HEART_BLUE
                        + " heartbeat added. "
                        + E.RED_CAR + " " + heartbeat.getVehicleReg()
                        + " " + heartbeat.getCreated() + E.RED_APPLE + " index: " + routePointIndex
                        + " owner: " + heartbeat.getOwnerName() + " " + E.RED_APPLE
                        + " heartbeatCount so far: " + heartbeatCount + " created: " + heartbeat.getCreated());
                //
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
            }

        }

        return heartbeats;
    }

    public static VehicleHeartbeat getVehicleHeartbeat(Vehicle vehicle, DateTime minutesAgo, RoutePoint rp) {
        VehicleHeartbeat vh = new VehicleHeartbeat();
        vh.setVehicleId(vehicle.getVehicleId());
        vh.setMake(vehicle.getMake());
        vh.setModel(vehicle.getModel());
        vh.setCreated(minutesAgo.toString());
        vh.setAssociationId(vehicle.getAssociationId());
        vh.setPosition(rp.getPosition());
        vh.setOwnerId(vehicle.getOwnerId());
        vh.setOwnerName(vehicle.getOwnerName());
        vh.setVehicleReg(vehicle.getVehicleReg());
        vh.setLongDate(minutesAgo.getMillis());
        vh.setVehicleHeartbeatId(UUID.randomUUID().toString());
        return vh;
    }

    List<Integer> getSortedIndices(List<RoutePoint> points) {
        int bound = 5;
        if (points.size() > 400) {
            bound = 10;
        } else {
            bound = 2;
        }
        int count = random.nextInt(bound);
        if (count == 0) {
            count = 2;
        }

        if (points.isEmpty()) {
            return new ArrayList<>();
        }
        HashMap<Integer, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < count; i++) {
            int index = random.nextInt(points.size() - 1);
            indexMap.put(index, index);
        }
        List<Integer> indices = new ArrayList<>(indexMap.values().stream().toList());
        Collections.sort(indices);
        return indices;
    }

    public List<RoutePoint> getPoints(Route route) {
        Criteria routeCriteria = where("routeId").is(route.getRouteId());
        Query query = new Query(routeCriteria).with(Sort.by("index"));

        List<RoutePoint> list = mongoTemplate.find(query, RoutePoint.class);
        logger.info(E.RED_APPLE + " .... Points found for " + route.getName()
                + " : " + list.size());
        return list;

    }

    public List<VehicleHeartbeat> findOwnerVehiclesByLocationAndTime(
            String userId,
            double latitude, double longitude, int minutes) {
        Point point = new Point(latitude, longitude);
        Distance distance = new Distance(100, Metrics.KILOMETERS);
        Circle circle = new Circle(point, distance);
//        Integer resultLimit = 20; //Limit to 20 records

        DateTime now = DateTime.now().toDateTimeISO().minusMinutes(minutes);
        String date = now.toString();
        Criteria geoCriteria = where("position").withinSphere(circle);
        Criteria ownerCriteria = where("ownerId").gte(userId);
        Criteria dateCriteria = where("created").gte(date);

        Query query = query(geoCriteria);
        query.addCriteria(ownerCriteria);
        query.addCriteria(dateCriteria);

        List<VehicleHeartbeat> cars = mongoTemplate.find(query, VehicleHeartbeat.class);

        logger.info(XX + " Number of cars: " + E.RED_DOT + cars.size());
        return cars;
    }

    public List<VehicleHeartbeat> findAssociationVehiclesByLocationAndTime(
            String associationId,
            double latitude, double longitude, int minutes) {
        Point point = new Point(latitude, longitude);
        Distance distance = new Distance(100, Metrics.KILOMETERS);
        Circle circle = new Circle(point, distance);
//        Integer resultLimit = 20; //Limit to 20 records

        DateTime now = DateTime.now().toDateTimeISO().minusMinutes(minutes);
        String date = now.toString();
        Criteria geoCriteria = where("position").withinSphere(circle);
        Criteria associationCriteria = where("associationId").gte(associationId);
        Criteria dateCriteria = where("created").gte(date);

        Query query = query(geoCriteria);
        query.addCriteria(associationCriteria);
        query.addCriteria(dateCriteria);

        List<VehicleHeartbeat> cars = mongoTemplate.find(query, VehicleHeartbeat.class);

        logger.info(XX + " Number of cars: " + E.RED_DOT + cars.size());
        return cars;
    }

    public Vehicle addVehicle(Vehicle vehicle) throws Exception {
        createVehicleQRCode(vehicle);
        Vehicle v = vehicleRepository.insert(vehicle);
        logger.info("Vehicle has been added to database");
        messagingService.sendVehicleUpdateMessage(v.getAssociationId(), v.getVehicleId());
        return v;
    }

    public Vehicle updateVehicle(Vehicle vehicle) {
        Vehicle v = vehicleRepository.save(vehicle);
        logger.info("Vehicle has been updated on database");
        messagingService.sendVehicleUpdateMessage(v.getAssociationId(), v.getVehicleId());
        return v;
    }

    public List<Vehicle> getAssociationVehicles(String associationId, int page) {

        Instant start = Instant.now();
        PageRequest request = PageRequest.of(page, 300, Sort.by("vehicleReg"));

        Page<Vehicle> vehiclePage = vehicleRepository.findByAssociationId(associationId, request);
        int pages = vehiclePage.getTotalPages();
        logger.info(E.RED_DOT + "number of pages: " + pages);
        if (pages == 0) {
            return new ArrayList<>();
        }
        if (page > pages) {
            return new ArrayList<>();
        }
        Iterator<Vehicle> ite = vehiclePage.iterator();
        List<Vehicle> vehicles = new ArrayList<>();
        while (ite.hasNext()) {
            Vehicle p = ite.next();
            vehicles.add(p);
        }
        //
        logger.info(E.RED_DOT + "number of cars: " + vehicles.size() + " page: " + page + " of size: 300");
        logger.info(E.LEAF + E.LEAF + " Cars delivered. elapsed time: "
                + Duration.between(start, Instant.now()).toSeconds() + " seconds");
        return vehicles;

    }

    public List<Vehicle> getOwnerVehicles(String userId, int page) {

        Instant start = Instant.now();
        PageRequest request = PageRequest.of(page, 200, Sort.by("userId"));

        Page<Vehicle> vehiclePage = vehicleRepository.findByOwnerId(userId, request);
        int pages = vehiclePage.getTotalPages();
        logger.info(E.RED_DOT + "number of pages: " + pages);
        if (pages == 0) {
            return new ArrayList<>();
        }
        if (page > pages) {
            return new ArrayList<>();
        }
        Iterator<Vehicle> ite = vehiclePage.iterator();
        List<Vehicle> vehicles = new ArrayList<>();
        while (ite.hasNext()) {
            Vehicle p = ite.next();
            vehicles.add(p);
        }
        //
        logger.info(E.RED_DOT + "number of cars: " + vehicles.size() + " page: " + page + " of size: 300");
        logger.info(E.LEAF + E.LEAF + " Cars delivered. elapsed time: "
                + Duration.between(start, Instant.now()).toSeconds() + " seconds");

        return vehicles;
    }

    public int updateVehicleQRCode(Vehicle vehicle) throws Exception {
        try {
            int result = createVehicleQRCode(vehicle);
            vehicleRepository.save(vehicle);
            logger.info(E.OK + E.OK + " ... we cool with QRCode for "
                    + vehicle.getVehicleReg() + " result: " + result);

        } catch (Exception e) {
            logger.severe("Unable to create QRCode");
            return 9;
        }
        return 0;
    }

    public int createVehicleQRCode(Vehicle car) throws Exception {
        try {
            String barcodeText = gson.toJson(car);
            QRCodeWriter barcodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix =
                    barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, 800, 800);

            BufferedImage img = MatrixToImageWriter.toBufferedImage(bitMatrix);

            String reg = car.getVehicleReg().replace(" ", "");

            File file = CommuterService.getQRCodeFile(reg);
            ImageIO.write(img, "png", file);
            logger.info(E.COFFEE + "File created and qrCode ready for uploading");
            String url = cloudStorageUploaderService.uploadFile(file.getName(), file);
            car.setQrCodeUrl(url);

            boolean delete = Files.deleteIfExists(file.toPath());
            logger.info(E.LEAF + E.LEAF + E.LEAF +
                    " QRCode generated, url: " + url + " for car: " + gson.toJson(car)
                    + E.RED_APPLE + " - temp file deleted: " + delete);
        } catch (WriterException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return 0;
    }

    public int recreateAllQRCodes(String associationId) throws Exception {
        logger.info(E.LEAF + E.LEAF + E.LEAF + " ... recreateAllQRCodes starting ...");
        List<Vehicle> list = vehicleRepository.findByAssociationId(associationId);
        File tmpDir = new File("qrcodes");
        if (!tmpDir.isDirectory()) {
            try {
                Path p = Files.createDirectory(tmpDir.toPath());
                logger.info(E.LEAF + E.LEAF + E.LEAF + " ... recreateAllQRCodes dir for files: "
                        + p.toFile().getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            logger.info(E.LEAF + E.LEAF + E.LEAF + " ... recreateAllQRCodes dir already exists ");
        }
        int count = 0;
        for (Vehicle vehicle : list) {
            createVehicleQRCode(vehicle);
            vehicleRepository.save(vehicle);
            count++;
            logger.info(E.BLUE_DOT + E.BLUE_DOT +
                    " Changed qrCode for car#" + count + " " + E.RED_APPLE + " " + vehicle.getVehicleReg());
        }
        return count;
    }

    public List<VehicleUploadResponse> importVehiclesFromJSON(File file, String associationId) throws Exception {
        logger.info(E.BLUE_DOT + E.BLUE_DOT + " importVehiclesFromJSON :" + associationId);

        List<Association> asses = associationRepository.findByAssociationId(associationId);
        List<VehicleUploadResponse> vehicles = new ArrayList<>();

        if (!asses.isEmpty()) {
            List<Vehicle> vehiclesFromJSONFile = FileToVehicles.getVehiclesFromJSONFile(file);
            logger.info(E.BLUE_DOT + " getVehiclesFromJSONFile: "
                    + vehiclesFromJSONFile.size() + " will start processVehiclesFromFile");
            vehicles = processVehiclesFromFile(associationId, asses, vehiclesFromJSONFile);
            logger.info(E.BLUE_DOT + " processVehiclesFromFile: "
                    + vehicles.size() + " will start processVehiclesFromFile");

        }
        return vehicles;
    }

    private List<VehicleUploadResponse> processVehiclesFromFile(String associationId,
                                                                List<Association> asses,
                                                                List<Vehicle> vehiclesFromJSONFile) throws Exception {

        logger.info("processing vehiclesFromJSONFile :" + vehiclesFromJSONFile.size());
        List<VehicleUploadResponse> responses = new ArrayList<>();
        List<Vehicle> resultVehicles = new ArrayList<>();
        // get all owners and write them first

        try {
            HashMap<String, String> nameMap = new HashMap<>();
            for (Vehicle vehicle : vehiclesFromJSONFile) {
                nameMap.put(vehicle.getOwnerName(), vehicle.getOwnerName());
            }
            List<String> names = nameMap.values().stream().toList();
            logger.info(E.BLUE_DOT + " owner names :" + names.size());

            for (String name : names) {
                String[] strings = name.split(" ");
                String lastName = strings[strings.length - 1];
                StringBuilder firstName = new StringBuilder();
                for (int i = 0; i < strings.length - 1; i++) {
                    firstName.append(" ").append(strings[i]);
                }
                List<Vehicle> vehicles = new ArrayList<>();
                for (Vehicle vehicle : vehiclesFromJSONFile) {
                    if (vehicle.getOwnerName().equalsIgnoreCase(name)) {
                        vehicles.add(vehicle);
                    }
                }
                logger.info(E.BLUE_DOT + " owner cars :" + vehicles.size());

                try {
                    List<VehicleUploadResponse> uploadResponses = createUserAndVehicles(asses.get(0), resultVehicles,
                            lastName, firstName, vehicles);
                    responses.addAll(uploadResponses);
                    logger.info(E.LEAF + E.LEAF + E.LEAF
                            + " uploadResponses has been created for owner: " + name + " responses: " +
                            " " + uploadResponses.size());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return responses;
    }

    private List<VehicleUploadResponse> createUserAndVehicles(Association ass,
                                                              List<Vehicle> resultVehicles, String lastName,
                                                              StringBuilder firstName, List<Vehicle> vehicles) throws Exception {
        User user = new User();
        List<VehicleUploadResponse> responses = new ArrayList<>();
        try {
            user.setPassword(UUID.randomUUID().toString());
            user.setFirstName(firstName.toString());
            user.setLastName(lastName);
            user.setCellphone(vehicles.get(0).getCellphone());
            user.setUserType(Constants.OWNER);
            user.setAssociationId(ass.getAssociationId());
            user.setAssociationName(ass.getAssociationName());
            user.setCountryId(ass.getCountryId());
            user.setCountryName(ass.getCountryName());
            User mUser = null;
            try {
                mUser = userService.createUser(user);
            } catch (Exception e) {
                VehicleUploadResponse rep = new VehicleUploadResponse(null,
                        user.getName(), false, user.getCellphone());
                responses.add(rep);
            }
            for (Vehicle vehicle : vehicles) {
                vehicle.setAssociationId(ass.getAssociationId());
                vehicle.setAssociationName(ass.getAssociationName());
                vehicle.setCreated(DateTime.now().toDateTimeISO().toString());
                vehicle.setCountryId(ass.getCountryId());
                vehicle.setVehicleId(UUID.randomUUID().toString());
                vehicle.setOwnerName(user.getName());
                assert mUser != null;
                vehicle.setOwnerId(mUser.getUserId());
                vehicle.setActive(0);

                int result = createVehicleQRCode(vehicle);
                if (result == 0) {
                    try {
                        vehicleRepository.insert(vehicle);
                        resultVehicles.add(vehicle);
                        VehicleUploadResponse rep = new VehicleUploadResponse(vehicle.getVehicleReg(),
                                vehicle.getOwnerName(), true, user.getCellphone());
                        responses.add(rep);
                        logger.info(E.OK + E.OK + " ... we cool with QRCode for "
                                + gson.toJson(vehicle) + " result: " + result);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    logger.severe(E.NOT_OK + " Unable to create QRCode for "
                            + vehicle.getVehicleReg());
                    VehicleUploadResponse rep = new VehicleUploadResponse(vehicle.getVehicleReg(),
                            vehicle.getOwnerName(), false, user.getCellphone());
                    responses.add(rep);

                }
            }
            logger.info("Vehicles for user imported from file will be added: " + vehicles.size() +
                    " owner: " + user.getName());
        } catch (Exception e) {
            VehicleUploadResponse rep = new VehicleUploadResponse(null,
                    user.getName(), false, user.getCellphone());
            responses.add(rep);
        }
        return responses;
    }

    public List<VehicleUploadResponse> importVehiclesFromCSV(File file, String associationId) throws Exception {
        List<Association> asses = associationRepository.findByAssociationId(associationId);
        logger.info("importVehiclesFromCSV: associationId: " + associationId + " asses: " + asses.size());
        List<VehicleUploadResponse> uploadResponses = new ArrayList<>();
        try {
            List<Vehicle> vehiclesFromCSVFile = FileToVehicles.getVehiclesFromCSVFile(file);
            logger.info("importVehiclesFromCSV: vehiclesFromCSVFile: " + vehiclesFromCSVFile.size());

            if (!asses.isEmpty()) {
                uploadResponses = processVehiclesFromFile(associationId, asses, vehiclesFromCSVFile);

            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return uploadResponses;
    }

    public List<Vehicle> generateFakeVehicles(String associationId, int number) throws Exception {
        List<Vehicle> list = new ArrayList<>();
        List<Association> asses = associationRepository.findByAssociationId(associationId);
        if (asses.isEmpty()) {
            logger.info(E.RED_DOT + " Association not found!");
            return new ArrayList<>();
        }
        for (int i = 0; i < number; i++) {
            Vehicle mVehicle = getBaseVehicle(associationId,
                    asses.get(0).getAssociationName());
            createVehicleQRCode(mVehicle);
            list.add(mVehicle);
        }
        List<Vehicle> mList = vehicleRepository.insert(list);
        logger.info(E.FROG + E.FROG + E.FROG + " vehicles added to database: " + mList.size());
        for (Vehicle v : mList) {
            logger.info(E.BLUE_DOT + E.BLUE_DOT + " VEHICLE inside Mongo: " + gson.toJson(v));

        }
        return mList;
    }

    public List<Vehicle> generateFakeVehiclesFromFile(String associationId) throws Exception {
        logger.info(E.BLUE_DOT + " Getting fake vehicles from file ... ");
        Resource resource = resourceLoader.getResource("classpath:vehicles.json");
        File file = resource.getFile();

        List<Association> asses = associationRepository.findByAssociationId(associationId);
        if (asses.isEmpty()) {
            logger.info(E.RED_DOT + E.RED_DOT + " Association not found: "
                    + associationId + ", cannot generate fake vehicles");
            return new ArrayList<>();
        }
        //
        List<Vehicle> vehiclesFromJSONFile = FileToVehicles.getVehiclesFromJSONFile(file);
        //fill up objects
        for (Vehicle v : vehiclesFromJSONFile) {
            v.setAssociationId(asses.get(0).getAssociationId());
            v.setAssociationName(asses.get(0).getAssociationName());
            v.setCountryId(asses.get(0).getCountryId());
            v.setVehicleId(UUID.randomUUID().toString());
            v.setCountryId(asses.get(0).getCountryId());
            v.setVehicleId(UUID.randomUUID().toString());
            v.setPassengerCapacity(16);
            v.setCreated(DateTime.now().toString());
            createVehicleQRCode(v);
        }

        List<Vehicle> mList = vehicleRepository.insert(vehiclesFromJSONFile);
        logger.info(E.LEAF + E.LEAF + E.LEAF + " Fake vehicles added to database " + mList.size());
        for (Vehicle vehicle : mList) {
            logger.info(E.LEAF + E.LEAF + " VEHICLE inside MongoDB's p..y: " + gson.toJson(vehicle));
        }

        return mList;
    }

    public int changeFakeVehicleOwner(String userId) {

        List<User> users = userRepository.findByUserId(userId);
        logger.info(XX + " Number of users: " + E.RED_DOT + users.size());

        String oldOwner = "Mr. Transportation III";
        Query query = new Query();
        query.addCriteria(where("ownerName").is(oldOwner));

        List<Vehicle> cars = mongoTemplate.find(query, Vehicle.class);
        logger.info(XX + " Number of cars: " + E.RED_DOT + cars.size());

        for (Vehicle car : cars) {
            car.setOwnerId(userId);
            car.setOwnerName(users.get(0).getName());
        }
        vehicleRepository.saveAll(cars);
        logger.info(XX + " completed of cars: " + E.RED_DOT + cars.size());

        return cars.size();


    }

    private Vehicle getBaseVehicle(String associationId, String associationName) {
        Vehicle v = new Vehicle();

        v.setAssociationId(associationId);
        v.setActive(0);
        v.setMake("Toyota");
        v.setModel("Quantum");
        v.setOwnerId("Not a Real Id");
        v.setOwnerName(getOwnerName());
        v.setYear("2018");
        v.setCreated(DateTime.now().toDateTimeISO().toString());
        v.setAssociationName(associationName);
        v.setVehicleId(UUID.randomUUID().toString());
        v.setPassengerCapacity(16);
        v.setVehicleReg(getVehicleReg());

        return v;

    }

    private String getOwnerName() {
        String[] firstNames = new String[]{"John", "Nancy", "David", "Eric G", "Thomas A", "George", "Freddie", "Benjamin", "Thabo",
                "Thabiso", "Mmamothe", "Yvonne", "Brandy G", "Catherine", "Anthony", "Malenga", "Jimmy", "Donnie", "Samuel", "Karina"};
        String[] lastNames = new String[]{"Smith", "Baloyi", "Donaldson", "van der Merwe", "Battles", "Carpenter", "Moredi",
                "Benjamin", "Donald", "jackson", "Rostov", "Maringa", "van Wyk", "Damarin", "Phillips", "Hellenic",
                "Mofokeng", "Maluleke", "Henderson", "Marule", "Nkuna"};

        int index1 = random.nextInt(firstNames.length);
        int index2 = random.nextInt(lastNames.length);

        String name = firstNames[index1] + " " + lastNames[index2];
        int x = random.nextInt(100);
        if (x > 25) {
            return name;
        } else {
            return "Joburg South Taxi Collective Ltd.";
        }
    }

    private String getVehicleReg() {
        Random rand = new Random(System.currentTimeMillis());
        String[] alpha = {"V", "B", "F", "D", "K", "G", "H", "R", "P", "Y", "T", "W", "Q", "M", "N", "X", "F", "V", "B", "Z"};
        StringBuilder sb = new StringBuilder();
        sb.append(alpha[rand.nextInt(alpha.length - 1)]);
        sb.append(alpha[rand.nextInt(alpha.length - 1)]);
        sb.append(alpha[rand.nextInt(alpha.length - 1)]);
        sb.append(" ");
        sb.append(rand.nextInt(9));
        sb.append(rand.nextInt(9));
        sb.append(" ");
        sb.append("GP");

        return sb.toString();
    }
}
