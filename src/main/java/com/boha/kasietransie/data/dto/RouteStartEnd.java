package com.boha.kasietransie.data.dto;

import lombok.Data;

@Data
public class RouteStartEnd {
    Position startCityPosition;
    Position endCityPosition;
    String startCityId;
    String startCityName;
    String endCityId;
    String endCityName;
}
