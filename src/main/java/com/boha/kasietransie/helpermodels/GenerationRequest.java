package com.boha.kasietransie.helpermodels;

import lombok.Data;

import java.util.List;
@Data
public class GenerationRequest {
    List<String> vehicleIds;
    String routeId;
    String startDate;
    int intervalInSeconds;
}
