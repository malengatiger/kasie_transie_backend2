package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.Commuter;
import com.boha.kasietransie.data.dto.CommuterRequest;
import com.boha.kasietransie.data.dto.CommuterResponse;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommuterRequestRepository extends MongoRepository<CommuterRequest, String> {
    List<CommuterRequest> findByRouteId(String routeId);
    List<CommuterRequest> findByAssociationId(String associationId);

    List<CommuterRequest> findByRouteLandmarkId(String routeLandmarkId);


}
