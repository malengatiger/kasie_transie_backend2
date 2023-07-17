package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.SettingsModel;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SettingsModelRepository extends MongoRepository<SettingsModel, String> {
    List<SettingsModel> findByAssociationId(String associationId);
}
