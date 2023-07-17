package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.VehicleHeartbeat;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface HeartbeatRepository extends MongoRepository<VehicleHeartbeat, String> {
    List<VehicleHeartbeat> findByVehicleId(String vehicleId);

    List<VehicleHeartbeat> findByAssociationId(String associationId);

    List<VehicleHeartbeat> findByOwnerId(String userId);

    GeoResults<VehicleHeartbeat> findByPositionNear(Point location, Distance distance);

}
