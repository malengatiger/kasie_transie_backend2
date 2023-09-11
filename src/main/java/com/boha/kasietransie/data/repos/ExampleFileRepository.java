package com.boha.kasietransie.data.repos;

import com.boha.kasietransie.data.dto.ExampleFile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ExampleFileRepository extends MongoRepository<ExampleFile, String> {
    List<ExampleFile> findByType(String type);
}
