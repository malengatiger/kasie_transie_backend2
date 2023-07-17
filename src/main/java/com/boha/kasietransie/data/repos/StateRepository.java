package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.State;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface StateRepository extends MongoRepository<State, String> {
    List<State> findByCountryId(String countryID);
}
