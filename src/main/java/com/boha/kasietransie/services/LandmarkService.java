package com.boha.kasietransie.services;

import com.boha.kasietransie.data.dto.Landmark;
import com.boha.kasietransie.data.repos.LandmarkRepository;
import com.boha.kasietransie.data.repos.RouteRepository;
import com.boha.kasietransie.util.E;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class LandmarkService {

    private final LandmarkRepository landmarkRepository;
    private final RouteRepository routeRepository;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger logger = Logger.getLogger(LandmarkService.class.getSimpleName());

    public LandmarkService(LandmarkRepository landmarkRepository, RouteRepository routeRepository) {
        this.landmarkRepository = landmarkRepository;
        this.routeRepository = routeRepository;
    }

    public Landmark addBasicLandmark(Landmark landmark) {
        logger.info(".... about to insert landmark ...");
        Landmark m = landmarkRepository.insert(landmark);
        logger.info(".... inserted landmark ..." + gson.toJson(m));
        return m;
    }

    public int deleteLandmark(String landmarkId) {

        return landmarkRepository.deleteByLandmarkId(landmarkId);
    }



    public List<Landmark> findLandmarksByLocation(double latitude,
                                                  double longitude, double radiusInKM) {
        logger.info(E.COOL_MAN+E.COOL_MAN
                +" findLandmarksByLocation: radius: " + radiusInKM);
        logger.info(E.COOL_MAN+E.COOL_MAN
                +" findLandmarksByLocation: lat: " + latitude + " lng: " + longitude);
        org.springframework.data.geo.Point point =
                new org.springframework.data.geo.Point(longitude, latitude);

        Distance distance = new Distance(radiusInKM, Metrics.KILOMETERS);
        GeoResults<Landmark> landmarks = landmarkRepository.findByPositionNear(point, distance);

        List<Landmark> list = new ArrayList<>();
        for (GeoResult<Landmark> landmark : landmarks) {
            list.add(landmark.getContent());
        }

        logger.info(E.COOL_MAN+E.COOL_MAN
                +" findLandmarksByLocation: " + list.size()+ " landmarks");
        return list;
    }
}
