package com.boha.kasietransie.services;

import com.boha.kasietransie.data.dto.VehicleMediaRequest;
import com.boha.kasietransie.data.dto.VehiclePhoto;
import com.boha.kasietransie.data.dto.VehicleVideo;
import com.boha.kasietransie.data.repos.VehicleMediaRequestRepository;
import com.boha.kasietransie.data.repos.VehiclePhotoRepository;
import com.boha.kasietransie.data.repos.VehicleVideoRepository;
import com.boha.kasietransie.util.E;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class MediaService {
    final VehiclePhotoRepository vehiclePhotoRepository;
    final VehicleVideoRepository vehicleVideoRepository;

    final  VehicleMediaRequestRepository vehicleMediaRequestRepository;
    final MessagingService messagingService;
    final MongoTemplate mongoTemplate;

    private static final Logger logger = LoggerFactory.getLogger(MediaService.class);

    public MediaService(VehiclePhotoRepository vehiclePhotoRepository,
                        VehicleVideoRepository vehicleVideoRepository,
                        VehicleMediaRequestRepository vehicleMediaRequestRepository,
                        MessagingService messagingService, MongoTemplate mongoTemplate) {
        this.vehiclePhotoRepository = vehiclePhotoRepository;
        this.vehicleVideoRepository = vehicleVideoRepository;
        this.vehicleMediaRequestRepository = vehicleMediaRequestRepository;
        this.messagingService = messagingService;
        this.mongoTemplate = mongoTemplate;
    }
    public VehiclePhoto addVehiclePhoto(VehiclePhoto vehiclePhoto) {
        return vehiclePhotoRepository.insert(vehiclePhoto);
    }
    public List<VehicleMediaRequest> getVehicleMediaRequests(String vehicleId) {
        return vehicleMediaRequestRepository.findByVehicleId(vehicleId);
    }
    public List<VehicleMediaRequest> getAssociationVehicleMediaRequests(String associationId, String startDate) {
        Query query = new Query();
        query.addCriteria(Criteria.where("associationId").is(associationId)
                .andOperator(Criteria.where("created").gte(startDate))).limit(1500);

        HashMap<String, VehicleMediaRequest> map = new HashMap<>();

        List<VehicleMediaRequest> list = mongoTemplate.find(query,VehicleMediaRequest.class);
        List<VehicleMediaRequest> filtered;

        for (VehicleMediaRequest vehicleMediaRequest : list) {
            map.put(vehicleMediaRequest.getVehicleId(),vehicleMediaRequest);
        }

        filtered = map.values().stream().toList();
        logger.info(E.LEAF + " found " + filtered.size() + " Association Vehicle MediaRequests ");
        return filtered;
    }

    public VehicleVideo addVehicleVideo(VehicleVideo vehicleVideo) {
        return vehicleVideoRepository.insert(vehicleVideo);
    }
    public List<VehiclePhoto> getVehiclePhotos(String vehicleId) {
        return vehiclePhotoRepository.findByVehicleId(vehicleId);
    }
    public List<VehicleVideo> getVehicleVideos(String vehicleId) {
        return vehicleVideoRepository.findByVehicleId(vehicleId);
    }
}
