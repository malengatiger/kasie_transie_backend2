package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.DispatchRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DispatchRecordRepository extends MongoRepository<DispatchRecord, String> {
    List<DispatchRecord> findByRouteLandmarkId(String landmarkId);
    List<DispatchRecord> findByVehicleId(String vehicleId);

    List<DispatchRecord> findByOwnerId(String userId);


    List<DispatchRecord> findByMarshalId(String userId);

    List<DispatchRecord> findByAssociationId(String associationId);



}
