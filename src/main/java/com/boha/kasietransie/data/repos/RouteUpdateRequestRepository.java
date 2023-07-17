package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.RouteUpdateRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RouteUpdateRequestRepository extends MongoRepository<RouteUpdateRequest, String> {
    List<RouteUpdateRequest> findByAssociationId(String associationId);

    List<RouteUpdateRequest> findByRouteId(String routeId);




}
