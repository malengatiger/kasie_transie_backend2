package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.CalculatedDistance;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CalculatedDistanceRepository extends MongoRepository<CalculatedDistance, String> {
    List<CalculatedDistance> findByRouteId(String routeId);

    void deleteByRouteId(String routeId);
}
