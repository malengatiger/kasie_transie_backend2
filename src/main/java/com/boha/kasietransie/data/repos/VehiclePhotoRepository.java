package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.VehiclePhoto;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface VehiclePhotoRepository extends MongoRepository<VehiclePhoto, String> {
    List<VehiclePhoto> findByVehicleId(String vehicleId);

}
