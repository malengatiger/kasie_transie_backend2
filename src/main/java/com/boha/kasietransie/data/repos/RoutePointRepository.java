package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.RoutePoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface RoutePointRepository extends PagingAndSortingRepository<RoutePoint, String> {
    List<RoutePoint> findByRouteId(String routeId);
    Page<RoutePoint> findByRouteId(String routeId, Pageable pageable);
    List<RoutePoint> findByAssociationId(String associationId);

    List<RoutePoint> findByRouteIdOrderByCreatedAsc(String routeId);

    GeoResults<RoutePoint> findByPositionNear(Point location, Distance distance);

    void deleteByRoutePointId(String routePointId);
}
