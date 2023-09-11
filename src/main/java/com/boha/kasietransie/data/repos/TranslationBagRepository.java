package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.TranslationBag;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TranslationBagRepository extends MongoRepository<TranslationBag, String> {
    List<TranslationBag> findByTarget(String locale);
    List<TranslationBag> findBySource(String locale);

}
