package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.VehicleVideo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface VehicleVideoRepository extends MongoRepository<VehicleVideo, String> {
    List<VehicleVideo> findByVehicleId(String vehicleId);

}
