package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.RouteLandmark;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RouteLandmarkRepository extends MongoRepository<RouteLandmark, String> {
    List<RouteLandmark> findByRouteId(String routeId);
    List<RouteLandmark> findByAssociationId(String associationId);

    GeoResults<RouteLandmark> findByPositionNear(Point location, Distance distance);

    List<RouteLandmark> findByAssociationIdOrderByCreatedAsc(String associationId);

    List<RouteLandmark> findByRouteIdOrderByCreatedAsc(String routeId);

}
