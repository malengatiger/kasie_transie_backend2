package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.Route;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RouteRepository extends MongoRepository<Route, String> {

    List<Route> findByRouteId(String routeId);
    List<Route> findByAssociationId(String associationId);




}
