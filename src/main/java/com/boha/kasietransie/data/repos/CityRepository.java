package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.City;
import com.boha.kasietransie.data.dto.RoutePoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CityRepository extends MongoRepository<City, String> {
//    List<City> findByCountryId(String countryId);
    Page<City> findByCountryId(String countryId, Pageable pageable);

    List<City> findByCountryId(String countryId);

    List<City> findByCountryName(String countryName);

    List<City> findByName(String name);

    GeoResults<City> findByPositionNear(Point location, Distance distance);
}
