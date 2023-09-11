package com.boha.kasietransie.helpermodels;

import com.boha.kasietransie.data.dto.Route;
import com.boha.kasietransie.data.dto.RouteLandmark;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RouteSearchInfo {
    double latitude;
    double longitude;

    List<Route> routes = new ArrayList<>();
    List<RouteLandmark> routeLandmarks = new ArrayList<>();
    List<RouteDistanceFromSearch> distanceFromStartOfRoute;
    List<RouteDistanceFromSearch> distanceFromEndOfRoute;

}

