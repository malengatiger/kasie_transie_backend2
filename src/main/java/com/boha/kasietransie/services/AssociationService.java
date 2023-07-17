package com.boha.kasietransie.services;

import com.boha.kasietransie.data.dto.*;
import com.boha.kasietransie.data.repos.*;
import com.boha.kasietransie.util.Constants;
import com.boha.kasietransie.util.E;
import com.boha.kasietransie.util.VehicleUploadResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.joda.time.DateTime;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class AssociationService {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger logger = Logger.getLogger(AssociationService.class.getSimpleName());

    private static final String MM = "\uD83C\uDF4E\uD83C\uDF4E\uD83C\uDF4E\uD83C\uDF4E\uD83C\uDF4E\uD83C\uDF4E\uD83C\uDF4E\uD83C\uDF4E\uD83C\uDF4E ";

    private final AppErrorRepository appErrorRepository;
    private final AssociationRepository associationRepository;
    private final UserService userService;

    private final VehicleService vehicleService;
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final SettingsModelRepository settingsModelRepository;
    private final ResourceLoader resourceLoader;
    private final MongoTemplate mongoTemplate;


    public AssociationService(AppErrorRepository appErrorRepository,
                              AssociationRepository associationRepository,
                              UserService userService,
                              VehicleService vehicleService,
                              CountryRepository countryRepository,
                              CityRepository cityRepository,
                              SettingsModelRepository settingsModelRepository,
                              ResourceLoader resourceLoader,
                              MongoTemplate mongoTemplate) {
        this.appErrorRepository = appErrorRepository;
        this.associationRepository = associationRepository;
        this.userService = userService;
        this.vehicleService = vehicleService;
        this.countryRepository = countryRepository;
        this.cityRepository = cityRepository;
        this.settingsModelRepository = settingsModelRepository;
        this.resourceLoader = resourceLoader;
        this.mongoTemplate = mongoTemplate;
        logger.info(MM + " AssociationService constructed ");

    }

    //    @Transactional
    public RegistrationBag registerAssociation(Association association) throws Exception {
        logger.info(E.LEAF + E.LEAF + " registerAssociation starting ........... ");
        association.setDateRegistered(DateTime.now().toDateTimeISO().toString());

        User u = new User();
        if (association.getAssociationId() == null) {
            association.setAssociationId(UUID.randomUUID().toString());
            u.setAssociationId(association.getAssociationId());
        } else {
            u.setAssociationId(association.getAssociationId());
        }
        u.setFirstName(association.getAdminUserFirstName());
        u.setLastName(association.getAdminUserLastName());
        u.setCellphone(association.getAdminCellphone());
        u.setPassword(UUID.randomUUID().toString());
        u.setEmail(association.getAdminEmail());
        u.setAssociationName(association.getAssociationName());
        if (association.getCountryId() == null) {
            List<Country> countries = countryRepository.findByName(association.getCountryName());
            if (!countries.isEmpty()) {
                u.setCountryId(countries.get(0).getCountryId());
                association.setCountryId(countries.get(0).getCountryId());
                association.setCountryName(countries.get(0).getName());
            }
        } else {
            u.setCountryId(association.getCountryId());
            u.setCountryName(association.getCountryName());

        }
        u.setDateRegistered(DateTime.now().toDateTimeISO().toString());
        u.setUserType(Constants.ASSOCIATION_OFFICIAL);

        Association ass = null;
        try {
            User user = userService.createUser(u);
            association.setUserId(user.getUserId());
            u.setUserId(user.getUserId());
            ass = associationRepository.insert(association);
            SettingsModel settingsModel = new SettingsModel();
            settingsModel.setCreated(DateTime.now().toDateTimeISO().toString());
            settingsModel.setLocale("en");
            settingsModel.setThemeIndex(0);
            settingsModel.setAssociationId(association.getAssociationId());
            settingsModel.setDistanceFilter(100);
            settingsModel.setCommuterGeoQueryRadius(50);
            settingsModel.setGeofenceRadius(200);
            settingsModel.setHeartbeatIntervalSeconds(300);
            settingsModel.setLoiteringDelay(60);
            settingsModel.setNumberOfLandmarksToScan(100);
            settingsModel.setRefreshRateInSeconds(300);
            settingsModel.setVehicleGeoQueryRadius(100);
            settingsModel.setVehicleSearchMinutes(30);
            settingsModel.setCommuterSearchMinutes(30);

            settingsModelRepository.insert(settingsModel);
            logger.info(E.LEAF + E.LEAF + " Association: " + ass.getAssociationName() + " added to MongoDB database");

            List<Country> countries = countryRepository.findByCountryId(association.getCountryId());
            Country country = null;
            if (!countries.isEmpty()) {
                country = countries.get(0);
            }
            RegistrationBag bag = new RegistrationBag(ass, user, settingsModel, country);
            logger.info(E.LEAF + E.LEAF + " Association Admin Official: " + u.getName() + " registered OK");
            return bag;

        } catch (Exception e) {
            try {
                if (u.getUserId() != null) {
                    FirebaseAuth.getInstance().deleteUser(u.getUserId());
                    logger.info(E.RED_DOT + "Successfully deleted user.");
                }
                if (ass != null) {
                    associationRepository.delete(ass);
                }
            } catch (Exception ex) {
                throw new Exception("Firebase or MongoDB failed to create user or " +
                        "association; registration broke down! : " + ex.getMessage());
            }

        }

        throw new Exception("Firebase or MongoDB failed to create user or " +
                "association; registration broke down; like, crashed and burned!!");
    }

    public SettingsModel addSettingsModel(SettingsModel model) {
        return settingsModelRepository.insert(model);
    }

    public AppError addAppError(AppError error) {
        return appErrorRepository.insert(error);
    }

    public List<AppError> getAssociationAppErrors(String associationId) {
        return appErrorRepository.findByAssociationId(associationId);
    }

    public List<SettingsModel> getAssociationSettingsModels(String associationId) {
        return settingsModelRepository.findByAssociationId(associationId);
    }

    public List<Association> getAssociations() {
        return associationRepository.findAll();
    }

    public Association getAssociationById(String associationId) {
        List<Association> list = associationRepository.findByAssociationId(associationId);
        if (list.isEmpty()) {
            throw new NoSuchElementException();
        }
        return list.get(0);
    }

    public File downloadExampleUsersFile() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:users.csv");

        return resource.getFile();
    }

    public File downloadExampleVehiclesFile() throws IOException {
        Resource resource1 = resourceLoader.getResource("classpath:vehicles.csv");
        return resource1.getFile();
    }

    public RegistrationBag generateFakeAssociation(String associationName,
                                                   String email,
                                                   String testCellphoneNumber,
                                                   String firstName,
                                                   String lastName) throws Exception {
        Association ass = new Association();
        List<Country> cs = countryRepository.findByName("South Africa");
        List<City> cities = cityRepository.findByName("Johannesburg");

        RegistrationBag bag = null;
        if (!cs.isEmpty()) {
            ass.setAssociationName(associationName);
            ass.setAssociationId(UUID.randomUUID().toString());
            ass.setCountryId(cs.get(0).getCountryId());
            ass.setCountryName(cs.get(0).getName());
            if (!cities.isEmpty()) {
                ass.setCityId(cities.get(0).getCityId());
                ass.setCityName(cities.get(0).getName());
            }
            ass.setAdminEmail(email);
            ass.setAdminCellphone("+" + testCellphoneNumber);
            ass.setActive(0);
            ass.setAdminUserFirstName(firstName);
            ass.setAdminUserLastName(lastName);
            ass.setDateRegistered(DateTime.now().toDateTimeISO().toString());
            final double lat = -26.195246;
            final double lng = 28.034088;
            List<Double> coords = new ArrayList<>();
            coords.add(lng);
            coords.add(lat);
            ass.setPosition(new Position(
                    "Point", coords
            ));
        }
        bag = registerAssociation(ass);
        logger.info(XX + " Getting users and vehicles from files ... ");
        Resource resource = resourceLoader.getResource("classpath:users.csv");
        Resource resource1 = resourceLoader.getResource("classpath:vehicles.csv");

        File userFile = resource.getFile();
        File carFile = resource1.getFile();
        List<User> users = userService.importUsersFromCSV(userFile, ass.getAssociationId());
        List<VehicleUploadResponse> cars = vehicleService.importVehiclesFromCSV(carFile, ass.getAssociationId());

        logger.info(XX + " Fake association on the books! " + E.LEAF +
                " users: " + users.size() +
                " cars: " + cars.size() + " ass: " + associationName);
        logger.info(XX + " Fake association and admin user: " + gson.toJson(bag));


        return bag;
    }

    static final String XX = E.RED_APPLE + E.RED_APPLE;
}
