package com.boha.kasietransie.services;

import com.boha.kasietransie.data.dto.*;
import com.boha.kasietransie.data.repos.*;
import com.boha.kasietransie.util.E;
import com.google.api.core.ApiFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.joda.time.DateTime;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Logger;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
public class CommuterService {
    final CommuterRepository commuterRepository;
    final CommuterRequestRepository commuterRequestRepository;
    final CommuterResponseRepository commuterResponseRepository;
    final CloudStorageUploaderService cloudStorageUploaderService;
    private final MailService mailService;
    final MessagingService messagingService;
    final UserRepository userRepository;
    final RouteRepository routeRepository;
    final RouteService routeService;
    final MongoTemplate mongoTemplate;


    public CommuterService(CommuterRepository commuterRepository, CommuterRequestRepository commuterRequestRepository, CommuterResponseRepository commuterResponseRepository, CloudStorageUploaderService cloudStorageUploaderService, MailService mailService, MessagingService messagingService, UserRepository userRepository, RouteRepository routeRepository, RouteService routeService, MongoTemplate mongoTemplate) {
        this.commuterRepository = commuterRepository;
        this.commuterRequestRepository = commuterRequestRepository;
        this.commuterResponseRepository = commuterResponseRepository;
        this.cloudStorageUploaderService = cloudStorageUploaderService;
        this.mailService = mailService;
        this.messagingService = messagingService;
        this.userRepository = userRepository;
        this.routeRepository = routeRepository;
        this.routeService = routeService;
        this.mongoTemplate = mongoTemplate;
    }

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger logger = Logger.getLogger(CommuterService.class.getSimpleName());

    public List<CommuterRequest> getAssociationCommuterRequests(String associationId, String startDate) {
        Criteria c = Criteria.where("associationId").is(associationId)
                .and("dateRequested").gte(startDate);

        Query query = new Query(c).with(Sort.by("dateRequested").descending());
        return mongoTemplate.find(query, CommuterRequest.class);
    }

    public List<CommuterRequest> getRouteCommuterRequests(String routeId, String startDate) {
        Criteria c = Criteria.where("routeId").is(routeId)
                .and("dateRequested").gte(startDate);

        Query query = new Query(c).with(Sort.by("dateRequested").descending());
        return mongoTemplate.find(query, CommuterRequest.class);
    }

    public Commuter createCommuter(Commuter commuter) throws Exception {
        logger.info("\uD83E\uDDE1\uD83E\uDDE1 create commuter : " + gson.toJson(commuter));
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        logger.info("\uD83E\uDDE1\uD83E\uDDE1 createRequest  .... ");
        String storedPassword = UUID.randomUUID().toString();
        boolean validEmail = false;
        try {
            UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest();
            if (commuter.getCellphone() != null) {
                createRequest.setPhoneNumber(commuter.getCellphone());
            }
            if (commuter.getName() != null) {
                createRequest.setDisplayName(commuter.getName());
            }
            createRequest.setPassword(storedPassword);
            if (commuter.getEmail() == null) {
                String name = commuter.getName();
                String mName = name.replace(" ", "").toLowerCase(Locale.getDefault());
                String email = mName + System.currentTimeMillis() + "@kasietransie.com";
                commuter.setEmail(email);
                createRequest.setEmail(email);
                logger.info("\uD83E\uDDE1\uD83E\uDDE1 createUserAsync  .... email: " + email);

            } else {
                validEmail = true;
                createRequest.setEmail(commuter.getEmail());
            }

            ApiFuture<UserRecord> userRecordFuture = firebaseAuth.createUserAsync(createRequest);
            UserRecord userRecord = userRecordFuture.get();
            logger.info("\uD83E\uDDE1\uD83E\uDDE1 userRecord from Firebase : "
                    + userRecord.getEmail() + " uid: " + userRecord.getUid());
            if (userRecord.getUid() != null) {
                String uid = userRecord.getUid();
                commuter.setCommuterId(uid);
                createCommuterQRCode(commuter);
                commuterRepository.insert(commuter);
                logger.info(E.CROISSANT + E.CROISSANT + E.CROISSANT +
                        E.CROISSANT + " commuter should be in database");
                commuter.setPassword(storedPassword);
                //
                if (validEmail) {
                    String message = "Dear " + commuter.getName() +
                            "      ,\n\nYou have been registered with KasieTransie and the team is happy to send you the first time login password. '\n" +
                            "      \nPlease login on the web with your email and the attached password but use your cellphone number to sign in on the phone.\n" +
                            "      \n\nThank you for working with KasieTransie. \nWelcome aboard!!\nBest Regards,\nThe KasieTransie Team\ninfo@geomonitorapp.io\n\n";

                    logger.info("\uD83E\uDDE1\uD83E\uDDE1 sending email  .... ");
                    mailService.sendHtmlEmail(commuter.getEmail(), message, "Welcome to KasieTransie");
                }
                logger.info("\uD83E\uDDE1\uD83E\uDDE1 KasieTransie commuter created. "
                        + gson.toJson(commuter));
            } else {
                throw new Exception("userRecord.getUid() == null. We have a problem with Firebase, Jack!");
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            throw e;
        }

        return commuter;
    }

    public void createCommuterQRCode(Commuter commuter) {
        try {
            String barcodeText = gson.toJson(commuter);
            QRCodeWriter barcodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix =
                    barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, 800, 800);

            BufferedImage img = MatrixToImageWriter.toBufferedImage(bitMatrix);

            String reg = commuter.getCommuterId().replace(" ", "");

            File file = getQRCodeFile(reg);
            ImageIO.write(img, "png", file);
            logger.info(E.COFFEE + "File created and qrCode ready for uploading");
            String url = cloudStorageUploaderService.uploadFile(file.getName(), file);
            commuter.setQrCodeUrl(url);

            boolean delete = Files.deleteIfExists(file.toPath());
            logger.info(E.LEAF + E.LEAF + E.LEAF +
                    " QRCode generated, url: " + url + " for commuter: "
                    + E.RED_APPLE + " - temp file deleted: " + delete);
        } catch (WriterException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static File getQRCodeFile(String identifier) {
        File tmpDir = new File("qrcodes");
        if (!tmpDir.isDirectory()) {
            try {
                Files.createDirectory(tmpDir.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String p = "qrcodes/qrcode_" + identifier
                + "_" + System.currentTimeMillis() + ".png";
        return new File(p);
    }

    public Commuter addCommuter(Commuter commuter) throws Exception {
        Commuter comm = createCommuter(commuter);
        createCommuterQRCode(comm);
        return comm;
    }

    public CommuterRequest addCommuterRequest(CommuterRequest commuterRequest) {
        CommuterRequest c = commuterRequestRepository.insert(commuterRequest);
        messagingService.sendMessage(commuterRequest);
        return c;
    }

    public CommuterResponse addCommuterResponse(CommuterResponse commuterResponse) {
        return commuterResponseRepository.insert(commuterResponse);
    }

    Random random = new Random(System.currentTimeMillis());

    private List<Commuter> generateCommuters(int count) {
        List<Commuter> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Commuter c = new Commuter();
            c.setCommuterId(UUID.randomUUID().toString());
            c.setEmail("commuter_" + System.currentTimeMillis() + "@.kasietransie.com");
            c.setDateRegistered(DateTime.now().toDateTimeISO().toString());
            c.setName("FakeCommuter_" + System.currentTimeMillis());
            Commuter cx = commuterRepository.insert(c);
            list.add(cx);
        }
        logger.info(E.BLUE_DOT + E.BLUE_BIRD + " commuters generated: " + list.size());
        return list;
    }

    @Async
    public void generateRouteCommuterRequests(
            String routeId, int intervalInSeconds, int numberOfCommuters) {
        logger.info(E.HAND2 + E.HAND2 + E.HAND2 + " generateRouteCommuterRequests  ...");

        Route route = null;
        List<Route> routes = routeRepository.findByRouteId(routeId);
        if (!routes.isEmpty()) {
            route = routes.get(0);
        }
        if (route == null) {
            return;
        }

        List<Commuter> commuters = commuterRepository.findAll();
        List<CommuterRequest> commuterRequests = new ArrayList<>();
        logger.info(E.BLUE_DOT + " commuters in play: " + commuters.size() + " commuters ...");

        DateTime minutesAgo = DateTime.now().toDateTimeISO().minusHours(1);
        Criteria c = Criteria.where("routeId").is(route.getRouteId());
        Query query = new Query(c).with(Sort.by("index"));
        List<RouteLandmark> routeLandmarks = mongoTemplate.find(query, RouteLandmark.class);

        for (int i = 0; i < numberOfCommuters; i++) {
            for (Commuter commuter : commuters) {
                int k = random.nextInt(100);
                if (k > 70) {
                    continue;
                }
                int landmarkIndex = random.nextInt(routeLandmarks.size() - 1);
                RouteLandmark mark = routeLandmarks.get(landmarkIndex);
                int passengers = random.nextInt(16);
                if (passengers == 0) passengers = 1;
                CommuterRequest cr = new CommuterRequest();
                cr.setCommuterRequestId(UUID.randomUUID().toString());
                cr.setCommuterId(commuter.getCommuterId());
                cr.setDateRequested(minutesAgo.toString());
                cr.setAssociationId(mark.getAssociationId());
                cr.setCurrentPosition(getRandomPosition(mark.getPosition()));
                cr.setRouteId(mark.getRouteId());
                cr.setRouteName(mark.getRouteName());
                cr.setNumberOfPassengers(passengers);
                cr.setRouteLandmarkId(mark.getLandmarkId());
                cr.setRouteLandmarkName(mark.getLandmarkName());
                cr.setDestinationCityId(route.getRouteStartEnd().getEndCityId());
                cr.setDestinationCityName(route.getRouteStartEnd().getEndCityName());
                cr.setOriginCityId(route.getRouteStartEnd().getStartCityId());
                cr.setOriginCityName(route.getRouteStartEnd().getStartCityName());
                cr.setNumberOfPassengers(passengers);
                //
                CommuterRequest request = addCommuterRequest(cr);
                commuterRequests.add(request);
                //
                minutesAgo = getDateTime(intervalInSeconds, minutesAgo);
            }
        }

        logger.info(E.LEAF + E.LEAF + " commuter requests added: "
                + " route: " + route.getName() + " at: " + commuterRequests.size() + " requests generated"
                + " " + E.FLOWER_RED);
    }

    private DateTime getDateTime(int intervalInSeconds, DateTime minutesAgo) {
        int addMin = random.nextInt(5);
        if (addMin == 0) {
            addMin = 1;
        }
        minutesAgo = minutesAgo.plusMinutes(addMin);
        try {
            Thread.sleep(intervalInSeconds * 1000L);
        } catch (InterruptedException e) {
            //ignore
        }
        return minutesAgo;
    }

    public Position getRandomPosition(Position pos) {
        int latDistance = random.nextInt(5000);
        if (latDistance < 500) latDistance = 2000;
        int lngDistance = random.nextInt(5000);
        if (lngDistance < 500) lngDistance = 1500;

        double lat = getCoordinateWithOffset(pos.getCoordinates().get(1),latDistance);
        double lng = getCoordinateWithOffset(pos.getCoordinates().get(0),lngDistance);

        List<Double> coords = new ArrayList<>();
        coords.add(lng);
        coords.add(lat);

        Position p = new Position();
        p.setCoordinates(coords);
        p.setType("Point");

        return p;
    }
    private double getCoordinateWithOffset(double coordinate, double offsetInMeters) {
        double earthRadius = 6371000.0; // Earth's radius in meters
        double coordRad = toRadians(coordinate);
        double distanceRad = offsetInMeters / earthRadius;

        double newCoordinate = coordRad + distanceRad;
        return toDegrees(newCoordinate);
    }

    private double toRadians(double degree) {
        return degree * Math.PI / 180.0;
    }

    // Helper method to convert radians to degrees
    private double toDegrees(double radian) {
        return radian * 180.0 / Math.PI;
    }
}
