package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.Association;
import com.boha.kasietransie.data.dto.AssociationToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AssociationTokenRepository extends MongoRepository<AssociationToken, String> {
    List<AssociationToken> findByAssociationId(String associationId);
}
