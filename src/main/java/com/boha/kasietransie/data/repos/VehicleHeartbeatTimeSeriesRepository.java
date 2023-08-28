package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.VehicleHeartbeatTimeSeries;
import org.bson.BsonDateTime;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface VehicleHeartbeatTimeSeriesRepository extends MongoRepository<VehicleHeartbeatTimeSeries, String> {
//    List<City> findByCountryId(String countryId);
//    Page<HeartbeatTimeSeries> findByCreated(String countryId, Pageable pageable);

    List<VehicleHeartbeatTimeSeries> findByTimestamp(BsonDateTime timestamp);
}
