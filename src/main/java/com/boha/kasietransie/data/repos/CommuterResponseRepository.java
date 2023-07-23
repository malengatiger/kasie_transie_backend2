package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.CommuterRequest;
import com.boha.kasietransie.data.dto.CommuterResponse;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommuterResponseRepository extends MongoRepository<CommuterResponse, String> {
    List<CommuterResponse> findByAssociationId(String associationId);
    List<CommuterResponse> findByRouteId(String routeId);

    List<CommuterResponse> findByRouteLandmarkId(String routeLandmarkId);


}
