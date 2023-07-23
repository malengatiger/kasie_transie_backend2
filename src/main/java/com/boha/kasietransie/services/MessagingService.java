package com.boha.kasietransie.services;

import com.boha.kasietransie.data.dto.*;
import com.boha.kasietransie.util.E;
import com.google.firebase.messaging.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MessagingService {

   private static final Logger LOGGER = LoggerFactory.getLogger(MessagingService.class.getSimpleName());
    private static final Gson G = new GsonBuilder().setPrettyPrinting().create();
    private static String MM = E.PANDA+E.PANDA+E.PANDA+" MessagingService " + E.RED_APPLE;
    public void sendMessage(VehicleArrival vehicleArrival) {
        try {
            String topic = "vehicleArrival_" + vehicleArrival.getAssociationId();
            Notification notification = Notification.builder()
                    .setBody("A vehicle has arrived at a landmark")
                    .setTitle("Vehicle Arrival")
                    .build();

            Message message = buildMessage("vehicleArrival", topic,
                    G.toJson(vehicleArrival), notification);
            FirebaseMessaging.getInstance().send(message);
            LOGGER.info(MM + "VehicleArrival message sent via FCM " + G.toJson(vehicleArrival));
        } catch (Exception e) {
            LOGGER.error("Failed to send vehicleArrival FCM message");
            e.printStackTrace();
        }
    }
    public void sendMessage(VehicleDeparture vehicleDeparture) {
        try {
            String topic = "vehicleDeparture_" + vehicleDeparture.getAssociationId();
            Notification notification = Notification.builder()
                    .setBody("A vehicle has departed from landmark: " + vehicleDeparture.getLandmarkName())
                    .setTitle("Vehicle Departure")
                    .build();

            Message message = buildMessage("vehicleDeparture", topic,
                    G.toJson(vehicleDeparture), notification);
            FirebaseMessaging.getInstance().send(message);
            LOGGER.info(MM + "VehicleDeparture message sent via FCM " +  G.toJson(vehicleDeparture));

        } catch (Exception e) {
            LOGGER.error("Failed to send vehicleDeparture FCM message");
            e.printStackTrace();
        }
    }
    public void sendMessage(LocationRequest locationRequest) {
        try {
            String topic = "locationRequest_" + locationRequest.getAssociationId();
            Notification notification = Notification.builder()
                    .setBody("A vehicle location has been requested ")
                    .setTitle("Vehicle Location Request")
                    .build();

            Message message = buildMessage("locationRequest", topic,
                    G.toJson(locationRequest), notification);
            FirebaseMessaging.getInstance().send(message);
            LOGGER.info(MM + "LocationRequest message sent via FCM: " + G.toJson(locationRequest));

        } catch (Exception e) {
            LOGGER.error("Failed to send locationRequest FCM message");
            e.printStackTrace();
        }
    }
    public void sendMessage(LocationResponse locationResponse) {
        try {
            String topic = "locationResponse_" + locationResponse.getAssociationId();
            Notification notification = Notification.builder()
                    .setBody("A vehicle location response has been sent to you ")
                    .setTitle("Vehicle Location Response")
                    .build();

            Message message = buildMessage("locationResponse", topic,
                    G.toJson(locationResponse), notification);
            FirebaseMessaging.getInstance().send(message);
            LOGGER.info(MM + "LocationResponse message sent via FCM: " + G.toJson(locationResponse));

        } catch (Exception e) {
            LOGGER.error("Failed to send locationResponse FCM message");
            e.printStackTrace();
        }
    }
    public void sendMessage(UserGeofenceEvent userGeofenceEvent) {
        try {
            String topic = "userGeofenceEvent_" + userGeofenceEvent.getAssociationId();
            Notification notification = Notification.builder()
                    .setBody("A user has entered or exited a landmark: " + userGeofenceEvent.getLandmarkName())
                    .setTitle("User Geofence Event")
                    .build();

            Message message = buildMessage("userGeofenceEvent", topic,
                    G.toJson(userGeofenceEvent), notification);
            FirebaseMessaging.getInstance().send(message);
            LOGGER.info(MM + "UserGeofenceEvent message sent via FCM: " + G.toJson(userGeofenceEvent));

        } catch (Exception e) {
            LOGGER.error("Failed to send userGeofenceEvent FCM message");
            e.printStackTrace();
        }
    }
    public void sendRouteUpdateMessage(RouteUpdateRequest routeUpdateRequest) throws Exception {
        try {
            String topic = "route_changes_" + routeUpdateRequest.getAssociationId();
            Notification notification = Notification.builder()
                    .setBody("A Route has changed and you should get the route automatically. Route: "
                            + routeUpdateRequest.getRouteName())
                    .setTitle("Route Change Notice")
                    .build();
            Message message = buildMessage("routeChanges", topic,
                    routeUpdateRequest.getRouteId(), notification);
            FirebaseMessaging.getInstance().send(message);
            LOGGER.info(E.RED_APPLE + "Route Update Message has been sent: \n associationId: "
                    + routeUpdateRequest.getAssociationId() + " routeId: " + routeUpdateRequest.getRouteName());
        } catch (Exception e) {
            LOGGER.error("Failed to send RouteUpdateMessage FCM message, routeId: " + routeUpdateRequest.getRouteId());
            throw new Exception(e.getMessage());
        }
    }
    public int sendVehicleUpdateMessage(String associationId, String vehicleId) {
        try {
            String topic = "vehicle_changes_" + associationId;

            Message message = buildMessage("vehicleChanges", topic,
                    vehicleId);
            FirebaseMessaging.getInstance().send(message);

        } catch (Exception e) {
            LOGGER.error("Failed to send VehicleUpdateMessage FCM message");
            e.printStackTrace();
        }
        return 0;
    }

    public int sendVehicleMediaRequestMessage(VehicleMediaRequest request) throws Exception {
        try {
            String topic = "vehicle_media_request_" + request.getAssociationId();
            Notification notification = Notification.builder()
                    .setBody("A Request for Vehicle Photos or Video for: " + request.getVehicleReg())
                    .setTitle("Vehicle Media Request")
                    .build();
            String data = G.toJson(request);
            Message message = buildMessage("vehicleMediaRequest", topic,
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
            String topic = "dispatchRecord_" + dispatchRecord.getAssociationId();
            Message message = buildMessage("dispatchRecord", topic,
                    G.toJson(dispatchRecord));
            FirebaseMessaging.getInstance().send(message);
            LOGGER.info(MM + "DispatchRecord message sent via FCM: " + G.toJson(dispatchRecord));

        } catch (Exception e) {
            LOGGER.error("Failed to send dispatchRecord FCM message");
            e.printStackTrace();
        }
    }

    public void sendMessage(CommuterRequest commuterRequest) {
        try {
            String topic = "commuterRequest_" + commuterRequest.getRouteId();
            Notification notification = Notification.builder()
                    .setBody("A request has arrived from Commuter for Route: "
                            + commuterRequest.getRouteName())
                    .setTitle("Commuter Request")
                    .build();

            Message message = buildMessage("commuterRequest", topic,
                    commuterRequest.getRouteId(), notification);

            FirebaseMessaging.getInstance().send(message);
            LOGGER.info(MM + "CommuterRequest message sent via FCM: " + G.toJson(commuterRequest));

        } catch (Exception e) {
            LOGGER.error("Failed to send commuterRequest FCM message");
            e.printStackTrace();
        }
    }

    public void sendMessage(AmbassadorPassengerCount passengerCount) {
        try {
            String topic = "passengerCount_" + passengerCount.getAssociationId();
            Message message = buildMessage("passengerCount", topic,
                    G.toJson(passengerCount));
            FirebaseMessaging.getInstance().send(message);
            LOGGER.info(MM + "AmbassadorPassengerCount message sent via FCM: " + G.toJson(passengerCount));

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
