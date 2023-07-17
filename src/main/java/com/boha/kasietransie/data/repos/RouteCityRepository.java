package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.RouteCity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RouteCityRepository extends MongoRepository<RouteCity, String> {
    List<RouteCity> findByRouteId(String routeId);
    List<RouteCity> findByCityId(String cityId);

    List<RouteCity> findByAssociationId(String associationId);

}
