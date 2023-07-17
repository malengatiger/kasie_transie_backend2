package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.LocationResponse;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface LocationResponseRepository extends MongoRepository<LocationResponse, String> {
    List<LocationResponse> findByAssociationId(String associationId);
    List<LocationResponse> findByVehicleId(String vehicleId);



}
