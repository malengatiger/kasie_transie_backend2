package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.RouteAssignment;
import com.boha.kasietransie.data.dto.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RouteAssignmentRepository extends MongoRepository<RouteAssignment, String> {
    List<RouteAssignment> findByAssociationId(String associationId);

    List<RouteAssignment> findByRouteId(String routeId);

    List<RouteAssignment> findByVehicleId(String vehicleId);

    long deleteByRouteId(String routeId);

    Page<RouteAssignment> findByAssociationId(String associationId, Pageable pageable);


}
