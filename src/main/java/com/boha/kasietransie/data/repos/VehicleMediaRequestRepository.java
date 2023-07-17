package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.VehicleMediaRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface VehicleMediaRequestRepository extends MongoRepository<VehicleMediaRequest, String> {
    List<VehicleMediaRequest> findByAssociationId(String associationId);
    List<VehicleMediaRequest> findByVehicleId(String vehicleId);

}
