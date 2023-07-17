package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.VehicleArrival;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface VehicleArrivalRepository extends MongoRepository<VehicleArrival, String> {
    List<VehicleArrival> findByAssociationId(String associationId);
    List<VehicleArrival> findByLandmarkId(String landmarkId);

    List<VehicleArrival> findByVehicleId(String vehicleId);

    List<VehicleArrival> findByOwnerId(String userId);

    GeoResults<VehicleArrival> findByPositionNear(Point location, Distance distance);


}
