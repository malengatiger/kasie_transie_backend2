package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserRepository extends MongoRepository<User, String> {

    List<User> findByUserId(String userId);
    List<User> findByEmail(String email);

    List<User> findByAssociationId(String associationId);

}
