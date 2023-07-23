package com.boha.kasietransie.data.dto;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collation = "CommuterResponse")

public class CommuterResponse {
    String commuterResponseId;
    String commuterRequestId;
    String responseDate;
    String message;
    String routeId;
    String routeName;
    int numberOfVehiclesOnRoute;
    String routeLandmarkId;
    String routeLandmarkName;
    String associationId;
    boolean vehicleDispatched;
}
