package com.boha.kasietransie.services;

import com.boha.kasietransie.data.dto.Association;
import com.boha.kasietransie.data.dto.User;
import com.boha.kasietransie.data.dto.Vehicle;
import com.boha.kasietransie.data.repos.AssociationRepository;
import com.boha.kasietransie.data.repos.UserRepository;
import com.boha.kasietransie.data.repos.VehicleHeartbeatRepository;
import com.boha.kasietransie.data.repos.VehicleRepository;
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
import org.joda.time.DateTime;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleHeartbeatRepository vehicleHeartbeatRepository;
    private final AssociationRepository associationRepository;
    private final ResourceLoader resourceLoader;
    final UserRepository userRepository;
    final UserService userService;
    final CloudStorageUploaderService cloudStorageUploaderService;

    final MongoTemplate mongoTemplate;
    final MessagingService messagingService;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger logger = Logger.getLogger(VehicleService.class.getSimpleName());

    private static final String XX = E.PRESCRIPTION + E.PRESCRIPTION + E.PRESCRIPTION;

    private static final String MM = "\uD83D\uDC26\uD83D\uDC26\uD83D\uDC26\uD83D\uDC26\uD83D\uDC26\uD83D\uDC26\uD83D\uDC26";

    public VehicleService(VehicleRepository vehicleRepository,
                          VehicleHeartbeatRepository vehicleHeartbeatRepository,
                          AssociationRepository associationRepository,
                          ResourceLoader resourceLoader, UserRepository userRepository, UserService userService, CloudStorageUploaderService cloudStorageUploaderService, MongoTemplate mongoTemplate, MessagingService messagingService) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleHeartbeatRepository = vehicleHeartbeatRepository;
        this.associationRepository = associationRepository;
        this.resourceLoader = resourceLoader;
        this.userRepository = userRepository;
        this.userService = userService;
        this.cloudStorageUploaderService = cloudStorageUploaderService;
        this.mongoTemplate = mongoTemplate;
        this.messagingService = messagingService;

        logger.info(MM + " VehicleService constructed and shit injected! ");

    }

    public Vehicle addVehicle(Vehicle vehicle) throws Exception {
        createVehicleQRCode(vehicle);
        Vehicle v = vehicleRepository.insert(vehicle);
        logger.info("Vehicle has been added to database");
        messagingService.sendVehicleUpdateMessage(v.getAssociationId(), v.getVehicleId());
        return v;
    }

    public List<Vehicle> getAssociationVehicles(String associationId) {
        return vehicleRepository.findByAssociationId(associationId);
    }

    public List<Vehicle> getOwnerVehicles(String userId) {
        return vehicleRepository.findByOwnerId(userId);
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

            Path path = Path.of("qrcodes/qrcode_" + reg
                    + "_" + System.currentTimeMillis() + ".png");

            String p = "qrcodes/qrcode_" + reg
                    + "_" + System.currentTimeMillis() + ".png";
            File file = new File(p);
            ImageIO.write(img, "png", file);
            logger.info(E.COFFEE + "File created and qrCode ready for uploading");
            String url = cloudStorageUploaderService.uploadFile(file.getName(), file);
            car.setQrCodeUrl(url);

            boolean delete = Files.deleteIfExists(path);
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
        logger.info(E.BLUE_DOT+E.BLUE_DOT+" importVehiclesFromJSON :" + associationId);

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
            logger.info(E.BLUE_DOT+" owner names :" + names.size());

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
                logger.info(E.BLUE_DOT+" owner cars :" + vehicles.size());

                try {
                    List<VehicleUploadResponse> uploadResponses = createUserAndVehicles(asses.get(0), resultVehicles,
                            lastName, firstName, vehicles);
                    responses.addAll(uploadResponses);
                    logger.info(E.LEAF+E.LEAF+E.LEAF
                            +" uploadResponses has been created for owner: " + name + " responses: " +
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
                        user.getName(),false, user.getCellphone());
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
                                vehicle.getOwnerName(),true, user.getCellphone());
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
                            vehicle.getOwnerName(),false, user.getCellphone());
                    responses.add(rep);

                }
            }
            logger.info("Vehicles for user imported from file will be added: " + vehicles.size() +
                    " owner: " + user.getName());
        } catch (Exception e) {
            VehicleUploadResponse rep = new VehicleUploadResponse(null,
                    user.getName(),false, user.getCellphone());
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
            logger.info("importVehiclesFromCSV: vehiclesFromCSVFile: " +  vehiclesFromCSVFile.size());

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
        query.addCriteria(Criteria.where("ownerName").is(oldOwner));

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

    Random random = new Random(System.currentTimeMillis());

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
