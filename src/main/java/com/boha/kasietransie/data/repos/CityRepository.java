package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.City;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CityRepository extends MongoRepository<City, String> {
    List<City> findByCountryId(String countryId);
    List<City> findByCountryName(String countryName);

    List<City> findByName(String name);

    GeoResults<City> findByPositionNear(Point location, Distance distance);
}
