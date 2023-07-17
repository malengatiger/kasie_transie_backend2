package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.Association;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AssociationRepository extends MongoRepository<Association, String> {
    List<Association> findByAssociationId(String associationId);
}
