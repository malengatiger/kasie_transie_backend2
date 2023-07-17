package com.boha.kasietransie.services;

import com.boha.kasietransie.data.dto.AmbassadorCheckIn;
import com.boha.kasietransie.data.dto.AmbassadorPassengerCount;
import com.boha.kasietransie.data.repos.AmbassadorCheckInRepository;
import com.boha.kasietransie.data.repos.AmbassadorPassengerCountRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AmbassadorService {

    final AmbassadorCheckInRepository ambassadorCheckInRepository;
    final AmbassadorPassengerCountRepository ambassadorPassengerCountRepository;
    final MongoTemplate mongoTemplate;
    final MessagingService messagingService;

    public AmbassadorService(AmbassadorCheckInRepository ambassadorCheckInRepository, AmbassadorPassengerCountRepository ambassadorPassengerCountRepository, MongoTemplate mongoTemplate, MessagingService messagingService) {
        this.ambassadorCheckInRepository = ambassadorCheckInRepository;
        this.ambassadorPassengerCountRepository = ambassadorPassengerCountRepository;
        this.mongoTemplate = mongoTemplate;
        this.messagingService = messagingService;
    }

    public List<AmbassadorCheckIn> getAssociationAmbassadorCheckIn(String associationId, String startDate) {
        Criteria c = Criteria.where("associationId").is(associationId)
                .and("created").gte(startDate);
        Query query = new Query(c);
        return mongoTemplate.find(query, AmbassadorCheckIn.class);
    }

    public List<AmbassadorCheckIn> getVehicleAmbassadorCheckIn(String vehicleId, String startDate) {
        Criteria c = Criteria.where("vehicleId").is(vehicleId)
                .and("created").gte(startDate);
        Query query = new Query(c);
        return mongoTemplate.find(query, AmbassadorCheckIn.class);
    }

    public List<AmbassadorCheckIn> getUserAmbassadorCheckIn(String userId, String startDate) {
        Criteria c = Criteria.where("userId").is(userId)
                .and("created").gte(startDate);
        Query query = new Query(c);
        return mongoTemplate.find(query, AmbassadorCheckIn.class);
    }

    public AmbassadorPassengerCount addAmbassadorCheckIn(AmbassadorPassengerCount count, String startDate) {
        ambassadorPassengerCountRepository.insert(count);
        return count;
    }

    public List<AmbassadorPassengerCount> getUserAmbassadorPassengerCounts(String userId, String startDate) {
        Criteria c = Criteria.where("userId").is(userId)
                .and("created").gte(startDate);
        Query query = new Query(c);
        return mongoTemplate.find(query, AmbassadorPassengerCount.class);
    }

    public List<AmbassadorPassengerCount> getAssociationAmbassadorPassengerCounts(String associationId, String startDate) {
        Criteria c = Criteria.where("associationId").is(associationId)
                .and("created").gte(startDate);
        Query query = new Query(c);
        return mongoTemplate.find(query, AmbassadorPassengerCount.class);
    }

    public List<AmbassadorPassengerCount> getVehicleAmbassadorPassengerCounts(String vehicleId, String startDate) {
        Criteria c = Criteria.where("vehicleId").is(vehicleId)
                .and("created").gte(startDate);
        Query query = new Query(c);
        return mongoTemplate.find(query, AmbassadorPassengerCount.class);
    }

    public AmbassadorCheckIn addAmbassadorCheckIn(AmbassadorCheckIn checkIn) {
        ambassadorCheckInRepository.insert(checkIn);
        return checkIn;
    }

    public AmbassadorPassengerCount addAmbassadorPassengerCount(AmbassadorPassengerCount count) {
        ambassadorPassengerCountRepository.insert(count);
        messagingService.sendMessage(count);
        return count;
    }
}
