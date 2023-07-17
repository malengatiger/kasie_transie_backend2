package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.VehicleDeparture;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface VehicleDepartureRepository extends MongoRepository<VehicleDeparture, String> {
    List<VehicleDeparture> findByAssociationId(String associationId);
    List<VehicleDeparture> findByLandmarkId(String landmarkId);

    List<VehicleDeparture> findByOwnerId(String userId);


    List<VehicleDeparture> findByVehicleId(String vehicleId);

    GeoResults<VehicleDeparture> findByPositionNear(Point location, Distance distance);


}
