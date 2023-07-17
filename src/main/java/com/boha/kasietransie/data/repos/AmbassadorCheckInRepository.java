package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.AmbassadorCheckIn;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AmbassadorCheckInRepository extends MongoRepository<AmbassadorCheckIn, String> {
    List<AmbassadorCheckIn> findByAssociationId(String associationId);
    List<AmbassadorCheckIn> findByUserId(String userId);

    List<AmbassadorCheckIn> findByVehicleId(String associationId);



}
