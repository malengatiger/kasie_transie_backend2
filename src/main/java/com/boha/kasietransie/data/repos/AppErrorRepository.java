package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.AppError;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AppErrorRepository extends MongoRepository<AppError, String> {
    List<AppError> findByAssociationId(String associationId);
    List<AppError> findByUserId(String userId);
    GeoResults<AppError> findByErrorPositionNear(Point location, Distance distance);
}
