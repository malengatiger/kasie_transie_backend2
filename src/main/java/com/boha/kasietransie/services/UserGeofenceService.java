package com.boha.kasietransie.services;

import com.boha.kasietransie.data.dto.UserGeofenceEvent;
import com.boha.kasietransie.data.repos.UserGeofenceEventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class UserGeofenceService {
    private final UserGeofenceEventRepository userGeofenceEventRepository;
    private final MessagingService messagingService;

    public UserGeofenceService(UserGeofenceEventRepository userGeofenceEventRepository, MessagingService messagingService) {
        this.userGeofenceEventRepository = userGeofenceEventRepository;
        this.messagingService = messagingService;
    }

    public UserGeofenceEvent addUserGeofenceEvent(UserGeofenceEvent event) {
        //String geoHash = GeoHash.encodeHash(event.getPosition().getLatitude(),

        UserGeofenceEvent e = userGeofenceEventRepository.insert(event);
        messagingService.sendMessage(e);
        return e;
    }
    public List<UserGeofenceEvent> getUserGeofenceEventsForUser(String userId) {
        return userGeofenceEventRepository.findByUserId(userId);
    }
    public List<UserGeofenceEvent> getVehicleGeofenceEventsForLandmark(String landmarkId) {
        return userGeofenceEventRepository.findByLandmarkId(landmarkId);
    }
    public List<UserGeofenceEvent> getVehicleGeofenceEventsForAssociation(String associationId) {
        return userGeofenceEventRepository.findByAssociationId(associationId);
    }
}
