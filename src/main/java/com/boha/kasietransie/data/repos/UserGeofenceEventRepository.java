package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.UserGeofenceEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserGeofenceEventRepository extends MongoRepository<UserGeofenceEvent, String> {
    List<UserGeofenceEvent> findByLandmarkId(String landmarkId);
    List<UserGeofenceEvent> findByUserId(String userId);

    List<UserGeofenceEvent> findByAssociationId(String associationId);


}
