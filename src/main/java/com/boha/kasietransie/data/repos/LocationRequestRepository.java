package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.LocationRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface LocationRequestRepository extends MongoRepository<LocationRequest, String> {
    List<LocationRequest> findByAssociationId(String associationId);



}
