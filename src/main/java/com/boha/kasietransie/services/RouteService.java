package com.boha.kasietransie.services;

import com.boha.kasietransie.data.CalculatedDistanceList;
import com.boha.kasietransie.data.RouteBag;
import com.boha.kasietransie.data.dto.*;
import com.boha.kasietransie.data.repos.*;
import com.boha.kasietransie.util.E;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.mongodb.WriteConcern;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.DeleteResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class RouteService {
    private final RouteRepository routeRepository;
    final RoutePointRepository routePointRepository;
    final RouteLandmarkRepository routeLandmarkRepository;
    final RouteCityRepository routeCityRepository;

    final AssociationRepository associationRepository;

    final CalculatedDistanceRepository calculatedDistanceRepository;

    final VehicleMediaRequestRepository vehicleMediaRequestRepository;
    final RouteUpdateRequestRepository routeUpdateRequestRepository;
    final MessagingService messagingService;
    final MongoTemplate mongoTemplate;
    final MongoOperations mongoOperations;
    final CloudStorageUploaderService cloudStorageUploaderService;

    public RouteService(RouteRepository routeRepository,
                        RoutePointRepository routePointRepository,
                        RouteLandmarkRepository routeLandmarkRepository,
                        RouteCityRepository routeCityRepository,
                        AssociationRepository associationRepository,
                        CalculatedDistanceRepository calculatedDistanceRepository,
                        VehicleMediaRequestRepository vehicleMediaRequestRepository,
                        RouteUpdateRequestRepository routeUpdateRequestRepository,
                        MessagingService messagingService,
                        MongoTemplate mongoTemplate, MongoOperations mongoOperations, CloudStorageUploaderService cloudStorageUploaderService) {
        this.routeRepository = routeRepository;
        this.routePointRepository = routePointRepository;
        this.routeLandmarkRepository = routeLandmarkRepository;
        this.routeCityRepository = routeCityRepository;
        this.associationRepository = associationRepository;
        this.calculatedDistanceRepository = calculatedDistanceRepository;
        this.vehicleMediaRequestRepository = vehicleMediaRequestRepository;
        this.routeUpdateRequestRepository = routeUpdateRequestRepository;
        this.messagingService = messagingService;
        this.mongoTemplate = mongoTemplate;
        this.mongoOperations = mongoOperations;
        this.cloudStorageUploaderService = cloudStorageUploaderService;
    }

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger logger = Logger.getLogger(RouteService.class.getSimpleName());
    private static final String XX = E.PRESCRIPTION + E.PRESCRIPTION + E.PRESCRIPTION;

    public void createRouteQRCode(Route route) throws Exception {
        try {
            String barcodeText = gson.toJson(route);
            QRCodeWriter barcodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix =
                    barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, 800, 800);

            BufferedImage img = MatrixToImageWriter.toBufferedImage(bitMatrix);

            String id = route.getRouteId().replace(" ", "");

            Path path = Path.of("qrcodes/qrcode_" + id
                    + "_" + System.currentTimeMillis() + ".png");

            String p = "qrcodes/qrcode_" + id
                    + "_" + System.currentTimeMillis() + ".png";
            File file = new File(p);
            ImageIO.write(img, "png", file);
            logger.info(E.COFFEE + "File created and qrCode ready for uploading");
            String url = cloudStorageUploaderService.uploadFile(file.getName(), file);
            route.setQrCodeUrl(url);

            boolean delete = Files.deleteIfExists(path);
            logger.info(E.LEAF + E.LEAF + E.LEAF +
                    " QRCode generated, url: " + url + " for route: " + gson.toJson(route)
                    + E.RED_APPLE + " - temp file deleted: " + delete);
        } catch (WriterException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Route addRoute(Route route) throws Exception {
        createRouteQRCode(route);
        return routeRepository.insert(route);
    }

    public List<RouteUpdateRequest> getRouteUpdateRequests(String routeId) {
        return routeUpdateRequestRepository.findByRouteId(routeId);
    }

    public RouteBag refreshRoute(String routeId) throws Exception {
        List<Route> list = routeRepository.findByRouteId(routeId);
        if (!list.isEmpty()) {
            Route route = list.get(0);

            List<RouteLandmark> routeLandmarks = routeLandmarkRepository.findByRouteId(routeId);
            List<RoutePoint> routePoints = routePointRepository.findByRouteId(routeId);
            List<RouteCity> routeCities = routeCityRepository.findByRouteId(routeId);
            RouteBag bag = new RouteBag();
            bag.setRoute(route);
            bag.setRoutePoints(routePoints);
            bag.setRouteLandmarks(routeLandmarks);
            bag.setRouteCities(routeCities);

            logger.info("Route has been refreshed for user. " + E.LEAF + E.LEAF + E.LEAF);
            return bag;
        }

        throw new Exception("Route not found");
    }

    public int deleteRoutePoint(String routePointId) {
        routePointRepository.deleteByRoutePointId(routePointId);
        return 0;
    }

    public Route updateRouteColor(String routeId, String color) throws Exception {
        List<Route> list = routeRepository.findByRouteId(routeId);
        if (!list.isEmpty()) {
            Route r = list.get(0);
            r.setColor(color);
            routeRepository.save(r);
            return r;
        }
        throw new Exception("color update failed");
    }

    public int addRoutePoints(List<RoutePoint> routePoints) {
        logger.info(XX + " Bulk insert of " + routePoints.size() + " routePoints");

        int inserted = 0;
        try {
            Instant start = Instant.now();
            mongoTemplate.setWriteConcern(WriteConcern.W1.withJournal(true));

            BulkOperations bulkInsertion = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED,
                    RoutePoint.class);

            for (RoutePoint routePoint : routePoints) bulkInsertion.insert(routePoint);

            BulkWriteResult bulkWriteResult = bulkInsertion.execute();

            inserted = bulkWriteResult.getInsertedCount();
            int delta = routePoints.size() - inserted;
            logger.info(XX + " Number of failed inserts : " + E.RED_DOT + delta);
            logger.info(XX + " Bulk insert of " + inserted
                    + " routePoints completed in " + Duration.between(start, Instant.now()).toMillis()
                    + " milliseconds");
        } catch (Exception e) {
            logger.info(E.RED_DOT + E.RED_DOT + E.RED_DOT + " Probable dup key insert");
            logger.severe(e.getMessage());
        }
        return inserted;
    }

    public List<RoutePoint> deleteRoutePointsFromIndex(String routeId, int index) {
        Criteria c = Criteria.where("routeId").is(routeId)
                .and("index").gte(index);

        org.springframework.data.mongodb.core.query.Query query = new Query(c);
        DeleteResult res = mongoTemplate.remove(query, RoutePoint.class);
        logger.info(E.BUTTERFLY+" delete result: " + res.getDeletedCount());

        List<RoutePoint> list = routePointRepository.findByRouteId(routeId);
        logger.info(E.BUTTERFLY + " return points left after delete: " + list.size());
        return list;
    }

    public List<CalculatedDistance> addCalculatedDistances(CalculatedDistanceList list) {

        List<CalculatedDistance> calculatedDistances = list.getCalculatedDistances();
        logger.info(XX + " Bulk insert of " + calculatedDistances.size() + " calculatedDistances");
        if (!calculatedDistances.isEmpty()) {
            String routeId = calculatedDistances.get(0).getRouteId();
            calculatedDistanceRepository.deleteByRouteId(routeId);
            logger.info(E.BLUE_DOT + " calculatedDistances deleted: " + calculatedDistances.size());
        }
        int inserted = 0;
        try {
            Instant start = Instant.now();
            mongoTemplate.setWriteConcern(WriteConcern.W1.withJournal(true));

            BulkOperations bulkInsertion = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED,
                    CalculatedDistance.class);

            for (CalculatedDistance routePoint : calculatedDistances) bulkInsertion.insert(routePoint);

            BulkWriteResult bulkWriteResult = bulkInsertion.execute();

            inserted = bulkWriteResult.getInsertedCount();
            int delta = calculatedDistances.size() - inserted;
            logger.info(XX + " Number of failed inserts : " + E.RED_DOT + delta);
            logger.info(XX + " Bulk insert of " + inserted
                    + " calculatedDistances completed in " + Duration.between(start, Instant.now()).toMillis()
                    + " milliseconds");
        } catch (Exception e) {
            logger.info(E.RED_DOT + E.RED_DOT + E.RED_DOT + " Probable dup key insert");
            logger.severe(e.getMessage());
        }
        return calculatedDistances;
    }

    public RouteLandmark addRouteLandmark(RouteLandmark routeLandmark) {
        return routeLandmarkRepository.insert(routeLandmark);
    }

    public VehicleMediaRequest addVehicleMediaRequest(VehicleMediaRequest vehicleMediaRequest) throws Exception {
        messagingService.sendVehicleMediaRequestMessage(vehicleMediaRequest);
        return vehicleMediaRequestRepository.insert(vehicleMediaRequest);
    }

    public RouteUpdateRequest addRouteUpdateRequest(RouteUpdateRequest routeUpdateRequest) throws Exception {
        messagingService.sendRouteUpdateMessage(routeUpdateRequest);
        return routeUpdateRequestRepository.insert(routeUpdateRequest);
    }


    public RouteLandmark updateRouteLandmark(RouteLandmark routeLandmark) {
        return routeLandmarkRepository.save(routeLandmark);
    }

    public RouteCity addRouteCity(RouteCity routeCity) {
        return routeCityRepository.insert(routeCity);
    }

    public List<Route> getAssociationRoutes(String associationId) {
        return routeRepository.findByAssociationId(associationId);
    }

    public List<Object> getRoutePointAggregate() {

        Aggregation agg = newAggregation(
                group("routeId","name").count().as("count"),
                project("count", "routeId").and("count").previousOperation()
        );

        AggregationResults<Object> groupResults
                = mongoTemplate.aggregate(agg, RoutePoint.class, Object.class);
        List<Object> result = groupResults.getMappedResults();
        logger.info(E.BELL+"results: " + result.size());
        for (Object obj : groupResults.getMappedResults()) {
            String m = gson.toJson(obj);
            logger.info(E.BELL+ " " + m);
        }
        return result;
    }
    public List<RoutePoint> getRoutePoints(String routeId, int page) {
        Instant start = Instant.now();
        PageRequest request = PageRequest.of(page,300, Sort.by("created"));

        Page<RoutePoint> routePointPage = routePointRepository.findByRouteId(routeId, request);
        int pages = routePointPage.getTotalPages();
        logger.info(E.RED_DOT + "number of pages: " + pages);
        if (pages == 0) {
            return new ArrayList<>();
        }
        if (page > pages) {
            return new ArrayList<>();
        }
        Iterator<RoutePoint> ite = routePointPage.iterator();
        List<RoutePoint> points = new ArrayList<>();
        while (ite.hasNext()) {
            RoutePoint p = ite.next();
            points.add(p);
        }
        //
        logger.info(E.RED_DOT + "number of points: " + points.size() + " page: " + page + " of size: 300");
        logger.info(E.LEAF+E.LEAF+" RoutePoints delivered. elapsed time: "
                + Duration.between(start, Instant.now()).toSeconds() + " seconds");
        return points;
    }

    public List<RouteCity> getRouteCities(String routeId) {
        return routeCityRepository.findByRouteId(routeId);
    }

    public List<CalculatedDistance> getCalculatedDistances(String routeId) {
        return calculatedDistanceRepository.findByRouteId(routeId);
    }

    public List<RouteLandmark> getRouteLandmarks(String routeId) {
        return routeLandmarkRepository.findByRouteId(routeId);
    }

    public List<Route> findRoutesByLocation(double latitude, double longitude, double radiusInKM) {

        org.springframework.data.geo.Point point =
                new org.springframework.data.geo.Point(longitude, latitude);
        Distance distance = new Distance(radiusInKM, Metrics.KILOMETERS);
        GeoResults<RoutePoint> routePointGeoResults = routePointRepository.findByPositionNear(point, distance);

        return getRoutesFromRoutePoints(routePointGeoResults);
    }

    public List<RoutePoint> findRoutePointsByLocation(double latitude, double longitude, double radiusInKM) {

        org.springframework.data.geo.Point point =
                new org.springframework.data.geo.Point(longitude, latitude);
        Distance distance = new Distance(radiusInKM, Metrics.KILOMETERS);
        GeoResults<RoutePoint> routePointGeoResults = routePointRepository.findByPositionNear(point, distance);

        List<RoutePoint> points = new ArrayList<>();
        for (GeoResult<RoutePoint> routePointGeoResult : routePointGeoResults) {
            points.add(routePointGeoResult.getContent());
        }
        return points;
    }


    public List<RouteLandmark> findRouteLandmarksByLocation(String associationId,
                                                            double latitude,
                                                            double longitude,
                                                            double radiusInKM) {


        logger.info(E.COOL_MAN + E.COOL_MAN
                + " findRouteLandmarksByLocation: radius: " + radiusInKM);
        logger.info(E.COOL_MAN + E.COOL_MAN
                + " findRouteLandmarksByLocation: lat: " + latitude + " lng: " + longitude);
        org.springframework.data.geo.Point point =
                new org.springframework.data.geo.Point(longitude, latitude);

        Distance distance = new Distance(radiusInKM, Metrics.KILOMETERS);
        GeoResults<RouteLandmark> geoResults = routeLandmarkRepository.findByPositionNear(point, distance);

        logger.info(E.COOL_MAN + E.COOL_MAN
                + " findRouteLandmarksByLocation, points: " + geoResults.getContent().size());

        List<RouteLandmark> routeLandmarks = new ArrayList<>();

        for (GeoResult<RouteLandmark> geoResult : geoResults) {
            routeLandmarks.add(geoResult.getContent());
        }

        logger.info(E.COOL_MAN + E.COOL_MAN
                + " findRouteLandmarksByLocation, routes: " + routeLandmarks.size());

        List<RouteLandmark> filteredList = new ArrayList<>();
        for (RouteLandmark routeLandmark : routeLandmarks) {
            if (routeLandmark.getAssociationId().equalsIgnoreCase(associationId)) {
                filteredList.add(routeLandmark);
            }
        }

        logger.info(E.COOL_MAN + E.COOL_MAN
                + " findRouteLandmarksByLocation, filteredRouteLandmarks: " + filteredList.size());

        for (RouteLandmark filteredRoute : filteredList) {
            logger.info(E.BASKET_BALL + "Nearest RouteLandmark: " + filteredRoute.getRouteName()
                    + E.HAND1 + " landmark: " + filteredRoute.getLandmarkName());
        }

        return filteredList;
    }

    public List<Route> findAssociationRoutesByLocation(String associationId,
                                                       double latitude,
                                                       double longitude,
                                                       double radiusInKM) {


        logger.info(E.COOL_MAN + E.COOL_MAN
                + " findAssociationRoutesByLocation: radius: " + radiusInKM + " associationId: " + associationId);
        logger.info(E.COOL_MAN + E.COOL_MAN
                + " findAssociationRoutesByLocation: lat: " + latitude + " lng: " + longitude);
        Instant start = Instant.now();
        org.springframework.data.geo.Point point =
                new org.springframework.data.geo.Point(longitude, latitude);

        final NearQuery nearQuery = NearQuery.near(point).maxDistance(radiusInKM * 1000).spherical(true);
        GeoResults<RoutePoint> results = mongoTemplate.geoNear(nearQuery, RoutePoint.class);
//        Query mongoQuery1 = new Query();
//        mongoQuery1.addCriteria(Criteria.where("position.coordinates").near(point));
//        List<RoutePoint> rPoints = mongoTemplate.find(mongoQuery1, RoutePoint.class,"RoutePoint");
//        Instant end0 = Instant.now();
        Instant end0 = Instant.now();
        logger.info(E.COOL_MAN + E.COOL_MAN
                + " findAssociationRoutesByLocation, mongoTemplate points: "
                + results.getContent().size() + E.RED_APPLE + " Elapsed: " +  Duration.between(start, end0).toSeconds() + " seconds");

//        Distance distance = new Distance(radiusInKM, Metrics.KILOMETERS);
//        GeoResults<RoutePoint> geoResults = routePointRepository.findByPositionNear(point, distance);
//
//        Instant end1 = Instant.now();
//        logger.info(E.COOL_MAN + E.COOL_MAN
//                + " findAssociationRoutesByLocation, points: "
//                + geoResults.getContent().size() + " Elapsed: " +  Duration.between(start, end1).toSeconds() + " seconds");
//
        List<RoutePoint> rPoints = new ArrayList<>();
        for (GeoResult<RoutePoint> result : results) {
            rPoints.add(result.getContent());
        }
        HashMap<String, RoutePoint> map = new HashMap<>();
        for (RoutePoint routePoint : rPoints) {
            map.put(routePoint.getRouteId(), routePoint);
        }

        List<Route> routes = new ArrayList<>();
        List<RoutePoint> mPoints = map.values().stream().toList();
        logger.info(E.COOL_MAN + E.COOL_MAN
                + " route points after filter: " + E.LEAF
                + " " +  mPoints.size());

        for (RoutePoint mPoint : mPoints) {
            List<Route> rs = routeRepository.findByRouteId(mPoint.getRouteId());
            routes.addAll(rs);
        }
        Instant end2 = Instant.now();

        logger.info(E.COOL_MAN + E.COOL_MAN
                + " data retrieval complete: "
                + " Elapsed: " +  Duration.between(start, end2).toSeconds() + " seconds");

        logger.info(E.COOL_MAN + E.COOL_MAN
                + " findAssociationRoutesByLocation, routes: " + routes.size());

        List<Route> filteredRoutes = new ArrayList<>();
        for (Route route : routes) {
            logger.info(E.COFFEE+" associationId : " + associationId);
            logger.info(E.COFFEE+" filter associationId : " + route.getAssociationId());
            if (route.getAssociationId().equalsIgnoreCase(associationId)) {
                filteredRoutes.add(route);
            }
        }
        logger.info(E.COOL_MAN + E.COOL_MAN
                + " findAssociationRoutesByLocation, filteredRoutes: " + filteredRoutes.size());
        for (Route filteredRoute : filteredRoutes) {
            logger.info(E.BASKET_BALL + "Nearest Route: " + filteredRoute.getName()
                    + E.HAND1 + " color: " + filteredRoute.getColor());
        }
        logger.info(E.COOL_MAN + E.COOL_MAN
                + " search complete: " + E.FERN
                + " Elapsed: " +  Duration.between(start, Instant.now()).toSeconds() + " seconds\n\n");

        return filteredRoutes;
    }

    public List<RouteLandmark> getAssociationRouteLandmarks(String associationId) {
        return routeLandmarkRepository.findByAssociationId(associationId);
    }

    public List<RoutePoint> getAssociationRoutePoints(String associationId) {
        return routePointRepository.findByAssociationId(associationId);
    }

    public List<RouteCity> getAssociationRouteCities(String associationId) {
        return routeCityRepository.findByAssociationId(associationId);
    }

    private List<Route> getRoutesFromRoutePoints(GeoResults<RoutePoint> routePointGeoResults) {
        List<Route> routes = new ArrayList<>();
        List<RoutePoint> list = new ArrayList<>();
        for (GeoResult<RoutePoint> departureGeoResult : routePointGeoResults) {
            list.add(departureGeoResult.getContent());
        }
        HashMap<String, String> map = new HashMap<>();
        for (RoutePoint routePoint : list) {
            if (!map.containsKey(routePoint.getRouteId())) {
                map.put(routePoint.getRouteId(), routePoint.getRouteId());
            }
        }
        for (String value : map.values()) {
            List<Route> routeList = routeRepository.findByRouteId(value);
            routes.addAll(routeList);
        }
        return routes;
    }


    public List<RouteLandmark> putRouteLandmarksInOrder(String routeId) {
        List<RouteLandmark> routeLandmarks = routeLandmarkRepository.findByRouteIdOrderByCreatedAsc(routeId);
        List<RouteLandmark> routeFilteredLandmarks = new ArrayList<>();
        int index = 0;
        for (RouteLandmark routeLandmark : routeLandmarks) {
            routeLandmark.setIndex(index);
            RouteLandmark routeLandmark1 = routeLandmarkRepository.save(routeLandmark);
            routeFilteredLandmarks.add(routeLandmark1);
            logger.info(E.BLUE_DOT + " Ordered by date: index: " + index + E.RED_APPLE
                    + " landmark: " + routeLandmark1.getLandmarkName());
            index++;

        }
        return routeFilteredLandmarks;
    }

}
