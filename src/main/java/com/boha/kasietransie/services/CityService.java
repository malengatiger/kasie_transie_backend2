package com.boha.kasietransie.services;

import com.boha.kasietransie.data.RouteBag;
import com.boha.kasietransie.data.RouteBagList;
import com.boha.kasietransie.data.dto.*;
import com.boha.kasietransie.data.repos.CityRepository;
import com.boha.kasietransie.data.repos.CountryRepository;
import com.boha.kasietransie.data.repos.StateRepository;
import com.boha.kasietransie.data.repos.UserRepository;
import com.boha.kasietransie.util.E;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.mongodb.client.model.Filters.near;

@Service
public class CityService {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger logger = Logger.getLogger(MongoService.class.getSimpleName());
    private static final String XX = E.COFFEE + E.COFFEE + E.COFFEE;

    final UserRepository userRepository;
    final MongoClient mongoClient;
    final CityRepository cityRepository;
    final CountryRepository countryRepository;

    final StateRepository stateRepository;
    final MongoTemplate mongoTemplate;

    @Value("${databaseName}")
    private String databaseName;

    public CityService(UserRepository userRepository,
                       MongoClient mongoClient,
                       CityRepository cityRepository,
                       CountryRepository countryRepository, StateRepository stateRepository, MongoTemplate mongoTemplate) {

        this.userRepository = userRepository;
        this.mongoClient = mongoClient;
        this.cityRepository = cityRepository;
        this.countryRepository = countryRepository;
        this.stateRepository = stateRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public City addCity(City city) {
        return cityRepository.insert(city);
    }

    public List<City> getCountryCities(String countryId, int page) {
        Instant start = Instant.now();

        PageRequest request = PageRequest.of(page,1000, Sort.by("name"));

        Page<City> routePointPage = cityRepository.findByCountryId(countryId, request);
        int pages = routePointPage.getTotalPages();
        logger.info(E.RED_DOT + "number of pages: " + pages);
        if (pages == 0) {
            return new ArrayList<>();
        }
        if (page > pages) {
            return new ArrayList<>();
        }
        Iterator<City> ite = routePointPage.iterator();
        List<City> cities = new ArrayList<>();
        while (ite.hasNext()) {
            City p = ite.next();
            cities.add(p);
        }
        //
        logger.info(E.RED_DOT + "number of cities: " + cities.size() + " page: " + page + " of size: 300");
        logger.info(E.LEAF+E.LEAF+" cities delivered. elapsed time: "
                + Duration.between(start, Instant.now()).toSeconds() + " seconds");
        return cities;
    }

    public File getCountryCitiesZippedFile(String countryId) throws Exception {
        logger.info(E.PANDA + E.PANDA +E.PANDA +E.PANDA +
                " getCountryCitiesZippedFile starting countryId: " + countryId);

        long start = System.currentTimeMillis();
        List<City> cities = cityRepository.findByCountryId(countryId);


        String json = gson.toJson(cities);

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        decimalFormat.setGroupingUsed(true);
        decimalFormat.setGroupingSize(3);

        logger.info(E.RED_DOT + E.RED_DOT + " Before zip: " + decimalFormat.format(json.length()) + " bytes in json");

        File dir = new File("zipDirectory");
        if (!dir.exists()) {
            boolean ok = dir.mkdir();
            logger.info( " Zip directory created: path: " + dir.getAbsolutePath() + " created: " + ok);
        }
        File zippedFile = new File(dir, DateTime.now().getMillis() + ".zip");
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zippedFile));
        ZipEntry e = new ZipEntry("cities");
        out.putNextEntry(e);

        byte[] data = json.getBytes();
        out.write(data, 0, data.length);
        out.closeEntry();

        out.close();
        long end = System.currentTimeMillis();
        long ms = (end - start);
        double elapsed = Double.parseDouble("" + ms) / Double.parseDouble("1000");

        logger.info(E.RED_DOT + E.RED_DOT + " After zip: "
                + decimalFormat.format(zippedFile.length()) + " bytes in file, elapsed: "
                + E.RED_APPLE + " " + elapsed + " seconds");

        return zippedFile;
    }


    public List<Country> getCountries() {
        return countryRepository.findAll();
    }
    public List<City> findCitiesByLocation(double latitude, double longitude, double radiusInKM, int limit) {

        logger.info(E.LEAF + E.LEAF + " lat: " + latitude
                + " lng: " + longitude + " radius: " + radiusInKM);
        List<City> list = new ArrayList<>();


        int mLimit;
        if (limit == 0) {
            mLimit = 10;
        } else {
            mLimit = limit;
        }

        logger.info(E.COOL_MAN+E.COOL_MAN
                +" findCitiesByLocation: radius: " + radiusInKM);
        logger.info(E.COOL_MAN+E.COOL_MAN
                +" findCitiesByLocation: lat: " + latitude + " lng: " + longitude);
        org.springframework.data.geo.Point point =
                new org.springframework.data.geo.Point(longitude, latitude);

        Distance distance = new Distance(radiusInKM, Metrics.KILOMETERS);
        GeoResults<City> citiesGeo = cityRepository.findByPositionNear(point, distance);

        logger.info(E.COOL_MAN+E.COOL_MAN
                +" findCitiesByLocation: " + citiesGeo.getContent().size());
        int count = 0;
        for (GeoResult<City> result : citiesGeo) {
            list.add(result.getContent());
            count++;
            if (count > limit) {
                break;
            }
        }

        logger.info(E.COOL_MAN+E.COOL_MAN
                +" findCitiesByLocation: filtered by limit: " + list.size()+ " cities");

        logger.info(E.LEAF + E.LEAF + E.LEAF + E.LEAF + E.LEAF + " Cities found around location with radius: "
                + radiusInKM + " km; found " + list.size() + " cities");

        return list;
    }
    public List<City> getCitiesNear(double latitude, double longitude,
                                    double minDistanceInMetres,
                                    double maxDistanceInMetres) {

        MongoDatabase mongoDb = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> cityCollection = mongoDb.getCollection(City.class.getSimpleName());
        Point myPoint = new Point(new Position(longitude, latitude));
        Bson query = near("position", myPoint,
                maxDistanceInMetres, minDistanceInMetres);
        final List<City> cities = new ArrayList<>();

        cityCollection.find(query)
                .forEach(doc -> {
                    String json = doc.toJson();
                    City city = gson.fromJson(json, City.class);
                    cities.add(city);
                });

        logger.info(E.PINK+E.PINK+E.PINK+"" + cities.size()
                + " cities found with min: " + minDistanceInMetres
                + " max: " + maxDistanceInMetres);

        HashMap<String, City> map = filter(cities);
        List<City> filteredCities = map.values().stream().toList();
        int count = 0;
        for (City place : filteredCities) {
            count++;
            logger.info(E.LEAF+E.LEAF+" City: #" + count + " " + E.RED_APPLE + " " + place.getName()
                    + ", " + place.getProvince());
        }


        return filteredCities;

    }
    private static HashMap<String, City> filter(List<City> cities) {
        HashMap<String,City> map = new HashMap<>();
        for (City city : cities) {
            if (!map.containsKey(city.getName())) {
                map.put(city.getName(), city);
            }
        }
        return map;
    }

    public List<State> getCountryStates(String countryId) {
        return stateRepository.findByCountryId(countryId);
    }

}
