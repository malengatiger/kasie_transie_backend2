package com.boha.kasietransie.services;

import com.boha.kasietransie.data.repos.CityRepository;
import com.boha.kasietransie.data.repos.CountryRepository;
import com.boha.kasietransie.data.repos.StateRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class GeoHashFixer {

    final CountryRepository countryRepository;
    final StateRepository stateRepository;
    final CityRepository cityRepository;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger logger = Logger.getLogger(GeoHashFixer.class.getSimpleName());

    private static final String MM = "\uD83C\uDF4E\uD83C\uDF4E\uD83C\uDF4E\uD83C\uDF4E\uD83C\uDF4E\uD83C\uDF4E\uD83C\uDF4E\uD83C\uDF4E\uD83C\uDF4E ";


    public GeoHashFixer(CountryRepository countryRepository, StateRepository stateRepository, CityRepository cityRepository) {
        this.countryRepository = countryRepository;
        this.stateRepository = stateRepository;
        this.cityRepository = cityRepository;
    }
}


