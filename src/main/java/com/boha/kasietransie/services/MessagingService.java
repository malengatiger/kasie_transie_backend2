package com.boha.kasietransie.services;

import com.boha.kasietransie.data.dto.*;
import com.boha.kasietransie.data.repos.AppErrorRepository;
import com.boha.kasietransie.data.repos.AssociationRepository;
import com.boha.kasietransie.data.repos.AssociationTokenRepository;
import com.boha.kasietransie.data.repos.VehicleRepository;
import com.boha.kasietransie.util.Constants;
import com.boha.kasietransie.util.CustomResponse;
import com.boha.kasietransie.util.E;
import com.google.firebase.messaging.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MessagingService {

    final VehicleRepository vehicleRepository;
    final AppErrorRepository appErrorRepository;
    final AssociationRepository associationRepository;
    final AssociationTokenRepository associationTokenRepository;
    final MongoTemplate mongoTemplate;
    private static final Logger logger = LoggerFactory.getLogger(MessagingService.class.getSimpleName());
    private static final Gson G = new GsonBuilder().setPrettyPrinting().create();
    private static final String MM = E.GLOBE + E.GLOBE + E.GLOBE + E.GLOBE + E.GLOBE + E.GLOBE +
            " MessagingService " + E.RED_APPLE;

    private boolean sleeping = false;
    public List<AssociationToken> getTokens(String associationId) {
        Query q = new Query().addCriteria(Criteria.where("associationId")
                .is(associationId)).with(Sort.by(Sort.Direction.DESC, "created"));

        List<AssociationToken> list = mongoTemplate.find(q, AssociationToken.class);
        //logger.info(E.HAND1 + E.HAND2 + " Association tokens found: " + list.size());
        return list;
    }

    public AssociationToken addAssociationToken(String associationId, String userId, String token) throws Exception {
        logger.info("AssociationToken receiveToken .... " + associationId);
        Query q = new Query(Criteria.where("userId").is(userId));
        mongoTemplate.findAndRemove(q,AssociationToken.class);

        AssociationToken t = new AssociationToken();
        List<Association> associations = associationRepository.findByAssociationId(associationId);
        Association ass = null;
        if (!associations.isEmpty()) {
            ass = associations.get(0);
        }
        if (ass == null) {
            throw new Exception("Association not found");
        }
        t.setUserId(userId);
        t.setToken(token);
        t.setAssociationId(associationId);
        t.setCreated(DateTime.now().toDateTimeISO().toString());
        t.setAssociationName(ass.getAssociationName());

        AssociationToken associationToken = associationTokenRepository.insert(t);
        logger.info("AssociationToken added to database");

        return associationToken;
    }

    public CustomResponse addSubscriptions(List<String> registrationTokens, List<String> topics) throws FirebaseMessagingException {

        for (String topic : topics) {
            TopicManagementResponse response = FirebaseMessaging.getInstance().subscribeToTopic(
                    registrationTokens, topic);
            logger.info(response.getSuccessCount() + " tokens were subscribed successfully for topic: " + E.RED_APPLE
                    + " " + topic);
        }
        logger.info(topics.size() + " topics were processed for subscriptions " + E.RED_APPLE);
        return new CustomResponse(200, "Subscriptions completed for "
                + topics.size(), DateTime.now().toDateTimeISO().toString());
    }

    public void sendMessage(VehicleArrival vehicleArrival) {
        if (checkSleeping()) return;
        try {
            String topic = Constants.vehicleArrival + vehicleArrival.getAssociationId();
            Notification notification = Notification.builder()
                    .setBody("A vehicle has arrived at a landmark")
                    .setTitle("Vehicle Arrival")
                    .build();

            Message message = buildMessage(Constants.vehicleArrival, topic,
                    G.toJson(vehicleArrival), notification);
            FirebaseMessaging.getInstance().send(message);
            logger.info(MM + "VehicleArrival message sent via FCM TOPIC: " + vehicleArrival.getVehicleReg());

            //get tokens for association (FOR devices running on Web - Kasie Web
            List<AssociationToken> assTokens = getTokens(vehicleArrival.getAssociationId());
            for (AssociationToken token : assTokens) {
                sendAssociationMessage(vehicleArrival, Constants.vehicleArrival, notification, token.getToken());
            }
            sleeping = false;

        } catch (Exception e) {
            logger.error("Failed to send vehicleArrival FCM message");
            sleepToCatchUp(e);
        }
    }

    private boolean checkSleeping() {
        if (sleeping) {
            logger.info(E.CANCEL+" ... attempt to send ignored. " +
                    "We sleeping, Boss!");
            return true;
        }
        return false;
    }

    private static void sendAssociationMessage(Object object, String dataType,
                                               Notification notification, String token) throws FirebaseMessagingException {

        Message msg;
        if (notification != null) {
            msg = Message.builder()
                    .setNotification(notification)
                    .putData(dataType, G.toJson(object))
                    .setFcmOptions(FcmOptions.builder()
                            .setAnalyticsLabel("KasieTransieFCM").build())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .build())
                    .setToken(token)
                    .build();
        } else {
            msg = Message.builder()
                    .putData(dataType, G.toJson(object))
                    .setFcmOptions(FcmOptions.builder()
                            .setAnalyticsLabel("KasieTransieFCM").build())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .build())
                    .setToken(token)
                    .build();
        }
        FirebaseMessaging.getInstance().send(msg);
        logger.info(E.GLOBE + E.GLOBE + E.GLOBE + E.GLOBE +
                " " + dataType + " FCM message sent direct to device using token");
    }

    public void sendMessage(VehicleDeparture vehicleDeparture) {
        if (checkSleeping()) return;

        try {
            String topic = Constants.vehicleDeparture + vehicleDeparture.getAssociationId();
            Notification notification = Notification.builder()
                    .setBody("A vehicle has departed from landmark: " + vehicleDeparture.getLandmarkName())
                    .setTitle("Vehicle Departure")
                    .build();

            Message message = buildMessage(Constants.vehicleDeparture, topic,
                    G.toJson(vehicleDeparture), notification);
            FirebaseMessaging.getInstance().send(message);
            logger.info(MM + "VehicleDeparture message sent via FCM TOPIC: " + vehicleDeparture.getVehicleReg());

            List<AssociationToken> assTokens = getTokens(vehicleDeparture.getAssociationId());
            for (AssociationToken token : assTokens) {
                sendAssociationMessage(vehicleDeparture, Constants.vehicleDeparture, notification, token.getToken());
            }
            sleeping = false;


        } catch (Exception e) {
            logger.error("Failed to send vehicleDeparture FCM message");
            sleepToCatchUp(e);
        }
    }

    public void sendMessage(LocationRequest locationRequest) {
        if (checkSleeping()) return;

        try {
            String topic = Constants.locationRequest + locationRequest.getAssociationId();
            Notification notification = Notification.builder()
                    .setBody("A vehicle location has been requested ")
                    .setTitle("Vehicle Location Request")
                    .build();

            Message message = buildMessage(Constants.locationRequest, topic,
                    G.toJson(locationRequest), notification);
            FirebaseMessaging.getInstance().send(message);
            logger.info(MM + "locationRequest message sent via FCM TOPIC: " + locationRequest.getVehicleReg());

            List<AssociationToken> assTokens = getTokens(locationRequest.getAssociationId());
            for (AssociationToken token : assTokens) {
                sendAssociationMessage(locationRequest, Constants.locationRequest, notification, token.getToken());
            }

            sleeping = false;

        } catch (Exception e) {
            logger.error("Failed to send locationRequest FCM message");
            sleepToCatchUp(e);
        }
    }

    public void sendMessage(VehicleHeartbeat heartbeat) {
        if (checkSleeping()) return;

        try {
            String topic = Constants.heartbeat + heartbeat.getAssociationId();

            Message message = buildMessage(Constants.heartbeat, topic,
                    G.toJson(heartbeat));
            FirebaseMessaging.getInstance().send(message);
            logger.info(MM + "heartbeat message sent via FCM: " + heartbeat.getVehicleReg());
            List<AssociationToken> assTokens = getTokens(heartbeat.getAssociationId());
            for (AssociationToken token : assTokens) {
                sendAssociationMessage(heartbeat, Constants.heartbeat, null, token.getToken());
            }

            sleeping = false;


        } catch (Exception e) {
            logger.error("Failed to send VehicleHeartbeat FCM message");
            sleepToCatchUp(e);
        }
    }

    public void sendMessage(LocationResponse locationResponse) {
        if (checkSleeping()) return;

        try {
            String topic = Constants.locationResponse + locationResponse.getAssociationId();
            Notification notification = Notification.builder()
                    .setBody("A vehicle location response has been sent to you ")
                    .setTitle("Vehicle Location Response")
                    .build();

            Message message = buildMessage(Constants.locationResponse, topic,
                    G.toJson(locationResponse), notification);
            FirebaseMessaging.getInstance().send(message);
            logger.info(MM + "locationResponse message sent via FCM: " + locationResponse.getVehicleReg());
            List<AssociationToken> assTokens = getTokens(locationResponse.getAssociationId());
            for (AssociationToken token : assTokens) {
                sendAssociationMessage(locationResponse, Constants.locationResponse, notification, token.getToken());
            }

            sleeping = false;


        } catch (Exception e) {
            logger.error("Failed to send locationResponse FCM message");
            sleepToCatchUp(e);
        }
    }

    public void sendMessage(UserGeofenceEvent userGeofenceEvent) {
        if (checkSleeping()) return;

        try {
            String topic = Constants.userGeofenceEvent + userGeofenceEvent.getAssociationId();
            Notification notification = Notification.builder()
                    .setBody("A user has entered or exited a landmark: " + userGeofenceEvent.getLandmarkName())
                    .setTitle("User Geofence Event")
                    .build();

            Message message = buildMessage(Constants.userGeofenceEvent, topic,
                    G.toJson(userGeofenceEvent), notification);
            FirebaseMessaging.getInstance().send(message);
            logger.info(MM + "userGeofenceEvent message sent via FCM: " + userGeofenceEvent.getLandmarkName());
            List<AssociationToken> assTokens = getTokens(userGeofenceEvent.getAssociationId());
            for (AssociationToken token : assTokens) {
                sendAssociationMessage(userGeofenceEvent, Constants.userGeofenceEvent, notification, token.getToken());
            }

            sleeping = false;


        } catch (Exception e) {
            logger.error("Failed to send userGeofenceEvent FCM message");
            sleepToCatchUp(e);
        }
    }

    public void sendRouteUpdateMessage(RouteUpdateRequest routeUpdateRequest) throws Exception {
        if (checkSleeping()) return;
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
            logger.info(MM + "routeUpdateRequest message sent via FCM: " + routeUpdateRequest.getRouteName());

            List<AssociationToken> assTokens = getTokens(routeUpdateRequest.getAssociationId());
            for (AssociationToken token : assTokens) {
                sendAssociationMessage(routeUpdateRequest, Constants.routeUpdateRequest, notification, token.getToken());
            }
            sleeping = false;

        } catch (Exception e) {
            logger.error("Failed to send RouteUpdateMessage FCM message, routeId: " + routeUpdateRequest.getRouteId());
            sleepToCatchUp(e);
        }
    }

    public int sendVehicleUpdateMessage(String associationId, String vehicleId) {
        if (checkSleeping()) return 9;

        try {
            List<Vehicle> vehicleList = vehicleRepository.findByVehicleId(vehicleId);
            Vehicle vehicle;
            if (!vehicleList.isEmpty()) {
                vehicle = vehicleList.get(0);
                String topic = Constants.vehicleChanges + associationId;

                Message message = buildMessage(Constants.vehicleChanges, topic,
                        G.toJson(vehicle));
                FirebaseMessaging.getInstance().send(message);
                logger.info(MM + "vehicleChanges message sent via FCM: " + vehicle.getVehicleReg());
                List<AssociationToken> assTokens = getTokens(associationId);
                for (AssociationToken token : assTokens) {
                    sendAssociationMessage(vehicle, Constants.vehicleChanges, null, token.getToken());
                }


            }

            sleeping = false;

        } catch (Exception e) {
            logger.error("Failed to send VehicleUpdateMessage FCM message");
            sleepToCatchUp(e);
        }
        return 0;
    }

    public int sendVehicleMediaRequestMessage(VehicleMediaRequest request) throws Exception {
        if (checkSleeping()) return 9;

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
            logger.info(MM + "vehicleMediaRequest message sent via FCM: " + request.getVehicleReg());
            List<AssociationToken> assTokens = getTokens(request.getAssociationId());
            for (AssociationToken token : assTokens) {
                sendAssociationMessage(request, Constants.vehicleMediaRequest, notification, token.getToken());
            }

            sleeping = false;


        } catch (Exception e) {
            logger.error("Failed to send VehicleMediaMessage FCM message");
            sleepToCatchUp(e);
        }
        return 0;
    }

    public void sendMessage(DispatchRecord dispatchRecord) {
        if (checkSleeping()) return;

        try {
            String topic = Constants.dispatchRecord + dispatchRecord.getAssociationId();
            Notification notification = Notification.builder()
                    .setBody("Dispatch record arrived " + dispatchRecord.getVehicleReg())
                    .setTitle("Taxi Dispatch")
                    .build();
            Message message = buildMessage(Constants.dispatchRecord, topic,
                    G.toJson(dispatchRecord));
            FirebaseMessaging.getInstance().send(message);
            logger.info(MM + "dispatchRecord message sent via FCM TOPIC: " + dispatchRecord.getVehicleReg());

            List<AssociationToken> assTokens = getTokens(dispatchRecord.getAssociationId());
            for (AssociationToken token : assTokens) {
                sendAssociationMessage(dispatchRecord, Constants.dispatchRecord, notification, token.getToken());
            }

            sleeping = false;

        } catch (Exception e) {
            logger.error("Failed to send dispatchRecord FCM message");
            sleepToCatchUp(e);
        }
    }

    private void sleepToCatchUp(Exception e) {
        logger.error(e.getMessage());
        sleeping = true;
        try {
            AppError a = new AppError();
            a.setCreated(DateTime.now().toDateTimeISO().toString());
            a.setErrorMessage(E.RED_DOT + " FCM Error: " + e.getMessage());
            a.setAppErrorId(UUID.randomUUID().toString());
            a.setDeviceType("Backend MessagingService");
            appErrorRepository.insert(a);
            logger.info(".... zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz" +
                    " .... sleeping for 20 seconds to allow FCM " +
                    "to catch a breath after quota error. AppError added to Mongo");
            Thread.sleep(20000);
        } catch (InterruptedException ex) {
            //ignore
        }
    }

    public void sendMessage(CommuterRequest commuterRequest) {
        if (checkSleeping()) return;

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
            logger.info(MM + "commuterRequest message sent via FCM TOPIC: " + commuterRequest.getRouteName());

            List<AssociationToken> assTokens = getTokens(commuterRequest.getAssociationId());
            for (AssociationToken token : assTokens) {
                sendAssociationMessage(commuterRequest, Constants.commuterRequest, notification, token.getToken());
            }

            sleeping = false;

        } catch (Exception e) {
            logger.error("Failed to send commuterRequest FCM message");
            sleepToCatchUp(e);
        }
    }

    public void sendMessage(AmbassadorPassengerCount passengerCount) {
        if (checkSleeping()) return;

        try {
            String topic = Constants.passengerCount + passengerCount.getAssociationId();
            Notification notification = Notification.builder()
                    .setBody("Passenger Count arrives" + passengerCount.getVehicleReg())
                    .setTitle("Passenger Count")
                    .build();
            Message message = buildMessage(Constants.passengerCount, topic,
                    G.toJson(passengerCount));
            FirebaseMessaging.getInstance().send(message);
            logger.info(MM + "passengerCount message sent via FCM TOPIC: " + passengerCount.getVehicleReg());

            List<AssociationToken> assTokens = getTokens(passengerCount.getAssociationId());
            for (AssociationToken token : assTokens) {
                sendAssociationMessage(passengerCount, Constants.passengerCount, notification, token.getToken());
            }
            sleeping = false;


        } catch (Exception e) {
            logger.error("Failed to send passengerCount FCM message");
            sleepToCatchUp(e);
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
