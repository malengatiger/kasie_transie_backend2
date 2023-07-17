package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.AmbassadorPassengerCount;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AmbassadorPassengerCountRepository extends MongoRepository<AmbassadorPassengerCount, String> {
    List<AmbassadorPassengerCount> findByAssociationId(String associationId);
    List<AmbassadorPassengerCount> findByUserId(String userId);

    List<AmbassadorPassengerCount> findByVehicleId(String associationId);

    List<AmbassadorPassengerCount> findByRouteId(String routeId);


}
