package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.Landmark;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface LandmarkRepository extends MongoRepository<Landmark, String> {
    List<Landmark> findByLandmarkId(String landmarkId);
    List<Landmark> findByAssociationId(String associationId);

    int deleteByLandmarkId(String landmarkId);
    GeoResults<Landmark> findByPositionNear(Point location, Distance distance);


}
