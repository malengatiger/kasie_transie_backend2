package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.VehicleHeartbeat;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface  VehicleHeartbeatRepository extends MongoRepository< VehicleHeartbeat, String> {
    List< VehicleHeartbeat> findByAssociationId(String associationId);
    List< VehicleHeartbeat> findByOwnerId(String userId);
    List< VehicleHeartbeat> findByVehicleId(String vehicleId);

}
