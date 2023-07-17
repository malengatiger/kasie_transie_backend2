package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.Country;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CountryRepository extends MongoRepository<Country, String> {
    List<Country> findByName(String name);
    List<Country> findByCountryId(String countryId);
}
