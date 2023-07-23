package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.RoutePoint;
import com.boha.kasietransie.data.dto.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface VehicleRepository extends MongoRepository<Vehicle, String> {
    List<Vehicle> findByAssociationId(String associationId);

    List<Vehicle> findByOwnerId(String userId);

    Page<Vehicle> findByAssociationId(String associationId, Pageable pageable);

    Page<Vehicle> findByOwnerId(String userId, Pageable pageable);


}
