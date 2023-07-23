package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.AmbassadorCheckIn;
import com.boha.kasietransie.data.dto.Commuter;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommuterRepository extends MongoRepository<Commuter, String> {
    List<Commuter> findByCommuterId(String commuterId);

}
