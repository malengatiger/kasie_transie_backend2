package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.Vehicle;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface VehicleRepository extends MongoRepository<Vehicle, String> {
    List<Vehicle> findByAssociationId(String associationId);
    List<Vehicle> findByOwnerId(String userId);

}
