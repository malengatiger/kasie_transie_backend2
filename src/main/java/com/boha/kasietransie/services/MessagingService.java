package com.boha.kasietransie.services;

import com.boha.kasietransie.data.dto.*;
import com.boha.kasietransie.data.repos.VehicleRepository;
import com.boha.kasietransie.util.Constants;
import com.boha.kasietransie.util.E;
import com.google.firebase.messaging.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class MessagingService {

    VehicleRepository vehicleRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(MessagingService.class.getSimpleName());
    private static final Gson G = new GsonBuilder().setPrettyPrinting().create();
    private static final String MM = E.GLOBE + E.GLOBE + E.GLOBE +  E.GLOBE + E.GLOBE + E.GLOBE +
            " MessagingService " + E.RED_APPLE;

    public void sendMessage(VehicleArrival vehicleArrival) {
        try {
            String topic = Constants.vehicleArrival + vehicleArrival.getAssociationId();
            Notification notification = Notification.builder()
                    .setBody("A vehicle has arrived at a landmark")
                    .setTitle("Vehicle Arrival")
                    .build();

            Message message = buildMessage(Constants.vehicleArrival, topic,
                    G.toJson(vehicleArrival), notification);
            FirebaseMessaging.getInstance().send(message);
            LOGGER.info(MM + "VehicleArrival message sent via FCM " + vehicleArrival.getVehicleReg()
                    + " at " + vehicleArrival.getLandmarkName());
        } catch (Exception e) {
            LOGGER.error("Failed to send vehicleArrival FCM message");
            e.printStackTrace();
        }
    }

    public void sendMessage(VehicleDeparture vehicleDeparture) {
        try {
            String topic = Constants.vehicleDeparture + vehicleDeparture.getAssociationId();
            Notification notification = Notification.builder()
                    .setBody("A vehicle has departed from landmark: " + vehicleDeparture.getLandmarkName())
                    .setTitle("Vehicle Departure")
                    .build();

            Message message = buildMessage(Constants.vehicleDeparture, topic,
                    G.toJson(vehicleDeparture), notification);
            FirebaseMessaging.getInstance().send(message);
//            LOGGER.info(MM + "VehicleDeparture message sent via FCM " + G.toJson(vehicleDeparture));

        } catch (Exception e) {
            LOGGER.error("Failed to send vehicleDeparture FCM message");
            e.printStackTrace();
        }
    }

    public void sendMessage(LocationRequest locationRequest) {
        try {
            String topic = Constants.locationRequest + locationRequest.getAssociationId();
            Notification notification = Notification.builder()
                    .setBody("A vehicle location has been requested ")
                    .setTitle("Vehicle Location Request")
                    .build();

            Message message = buildMessage(Constants.locationRequest, topic,
                    G.toJson(locationRequest), notification);
            FirebaseMessaging.getInstance().send(message);
            LOGGER.info(MM + "LocationRequest message sent via FCM: " +  " topic: " + topic + " "
                    + G.toJson(locationRequest));

        } catch (Exception e) {
            LOGGER.error("Failed to send locationRequest FCM message");
            e.printStackTrace();
        }
    }

    public void sendMessage(VehicleHeartbeat heartbeat) {
        try {
            String topic = Constants.heartbeat + heartbeat.getAssociationId();

            Message message = buildMessage(Constants.heartbeat, topic,
                    G.toJson(heartbeat));
            FirebaseMessaging.getInstance().send(message);
            LOGGER.info(MM + "VehicleHeartbeat message sent via FCM: " + heartbeat.getVehicleReg()
            + " topic: " + topic);

        } catch (Exception e) {
            LOGGER.error("Failed to send VehicleHeartbeat FCM message");
            e.printStackTrace();
        }
    }

    public void sendMessage(LocationResponse locationResponse) {
        try {
            String topic = Constants.locationResponse + locationResponse.getAssociationId();
            Notification notification = Notification.builder()
                    .setBody("A vehicle location response has been sent to you ")
                    .setTitle("Vehicle Location Response")
                    .build();

            Message message = buildMessage(Constants.locationResponse, topic,
                    G.toJson(locationResponse), notification);
            FirebaseMessaging.getInstance().send(message);
            LOGGER.info(MM + "LocationResponse message sent via FCM: " + " topic: " + topic + " "
                    + G.toJson(locationResponse));

        } catch (Exception e) {
            LOGGER.error("Failed to send locationResponse FCM message");
            e.printStackTrace();
        }
    }

    public void sendMessage(UserGeofenceEvent userGeofenceEvent) {
        try {
            String topic = Constants.userGeofenceEvent + userGeofenceEvent.getAssociationId();
            Notification notification = Notification.builder()
                    .setBody("A user has entered or exited a landmark: " + userGeofenceEvent.getLandmarkName())
                    .setTitle("User Geofence Event")
                    .build();

            Message message = buildMessage(Constants.userGeofenceEvent, topic,
                    G.toJson(userGeofenceEvent), notification);
            FirebaseMessaging.getInstance().send(message);
//            LOGGER.info(MM + "UserGeofenceEvent message sent via FCM: " + G.toJson(userGeofenceEvent));

        } catch (Exception e) {
            LOGGER.error("Failed to send userGeofenceEvent FCM message");
            e.printStackTrace();
        }
    }

    public void sendRouteUpdateMessage(RouteUpdateRequest routeUpdateRequest) throws Exception {
        try {
            String topic = Constants.routeUpdateRequest + routeUpdateRequest.getAssociationId();
            Notification notification = Notification.builder()
                    .setBody("A Route has changed and you should get the route automatically. Route: "
                            + routeUpdateRequest.getRouteName())
                    .setTitle("Route Change Notice")
                    .build();
            Message message = buildMessage(Constants.routeUpdateRequest, topic,
                    G.toJson(routeUpdateRequest), notification);
            FirebaseMessaging.getInstance().send(message);
//            LOGGER.info(E.RED_APPLE + "Route Update Message has been sent: \n associationId: "
//                    + routeUpdateRequest.getAssociationId() + " routeId: " + routeUpdateRequest.getRouteName());
        } catch (Exception e) {
            LOGGER.error("Failed to send RouteUpdateMessage FCM message, routeId: " + routeUpdateRequest.getRouteId());
            throw new Exception(e.getMessage());
        }
    }

    public int sendVehicleUpdateMessage(String associationId, String vehicleId) {
        try {
            List<Vehicle> vehicleList = vehicleRepository.findByVehicleId(vehicleId);
            Vehicle vehicle;
            if (!vehicleList.isEmpty()) {
                vehicle = vehicleList.get(0);
                String topic = Constants.vehicleChanges + associationId;

                Message message = buildMessage(Constants.vehicleChanges, topic,
                        G.toJson(vehicle));
                FirebaseMessaging.getInstance().send(message);
            }


        } catch (Exception e) {
            LOGGER.error("Failed to send VehicleUpdateMessage FCM message");
            e.printStackTrace();
        }
        return 0;
    }

    public int sendVehicleMediaRequestMessage(VehicleMediaRequest request) throws Exception {
        try {
            String topic = Constants.vehicleMediaRequest + request.getAssociationId();
            Notification notification = Notification.builder()
                    .setBody("A Request for Vehicle Photos or Video for: " + request.getVehicleReg())
                    .setTitle("Vehicle Media Request")
                    .build();
            String data = G.toJson(request);
            Message message = buildMessage(Constants.vehicleMediaRequest, topic,
                    data, notification);

            FirebaseMessaging.getInstance().send(message);

        } catch (Exception e) {
            LOGGER.error("Failed to send VehicleMediaMessage FCM message");
            throw new Exception(e.getMessage());
        }
        return 0;
    }

    public void sendMessage(DispatchRecord dispatchRecord) {
        try {
            String topic = Constants.dispatchRecord + dispatchRecord.getAssociationId();
            Message message = buildMessage(Constants.dispatchRecord, topic,
                    G.toJson(dispatchRecord));
            FirebaseMessaging.getInstance().send(message);
//            LOGGER.info(MM + "DispatchRecord message sent via FCM: " + dispatchRecord.getLandmarkName());

        } catch (Exception e) {
            LOGGER.error("Failed to send dispatchRecord FCM message");
            e.printStackTrace();
        }
    }

    public void sendMessage(CommuterRequest commuterRequest) {
        try {
            String topic = Constants.commuterRequest + commuterRequest.getAssociationId();
            Notification notification = Notification.builder()
                    .setBody("A request has arrived from Commuter for Route: "
                            + commuterRequest.getRouteName())
                    .setTitle("Commuter Request")
                    .build();

            Message message = buildMessage(Constants.commuterRequest, topic,
                    G.toJson(commuterRequest), notification);

            FirebaseMessaging.getInstance().send(message);
//            LOGGER.info(MM + "CommuterRequest message sent via FCM: " + commuterRequest.getRouteLandmarkName());

        } catch (Exception e) {
            LOGGER.error("Failed to send commuterRequest FCM message");
            e.printStackTrace();
        }
    }

    public void sendMessage(AmbassadorPassengerCount passengerCount) {
        try {
            String topic = Constants.passengerCount + passengerCount.getAssociationId();
            Message message = buildMessage(Constants.passengerCount, topic,
                    G.toJson(passengerCount));
            FirebaseMessaging.getInstance().send(message);
//            LOGGER.info(MM + "AmbassadorPassengerCount message sent via FCM: " + passengerCount.getRouteLandmarkName());

        } catch (Exception e) {
            LOGGER.error("Failed to send passengerCount FCM message");
            e.printStackTrace();
        }
    }

    private Message buildMessage(String dataName, String topic, String payload) {
        return Message.builder()
                .putData(dataName, payload)
                .setFcmOptions(FcmOptions.builder()
                        .setAnalyticsLabel("KasieTransieFCM").build())
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .build())
                .setTopic(topic)
                .build();
    }

    private Message buildMessage(String dataName, String topic, String payload, Notification notification) {
        return Message.builder()
                .setNotification(notification)
                .putData(dataName, payload)
                .setFcmOptions(FcmOptions.builder()
                        .setAnalyticsLabel("KasieTransieFCM").build())
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .build())
                .setTopic(topic)
                .build();
    }

}
