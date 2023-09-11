package com.boha.kasietransie.helpermodels;

import com.boha.kasietransie.data.dto.Route;
import com.boha.kasietransie.data.dto.RouteCity;
import com.boha.kasietransie.data.dto.RouteLandmark;
import com.boha.kasietransie.data.dto.RoutePoint;
import lombok.Data;

import java.util.List;
@Data
public class RouteBag {
    Route route;
    List<RouteLandmark> routeLandmarks;
    List<RoutePoint> routePoints;
    List<RouteCity> routeCities;

    public RouteBag() {
    }

    public RouteBag(Route route, List<RouteLandmark> routeLandmarks, List<RoutePoint> routePoints, List<RouteCity> routeCities) {
        this.route = route;
        this.routeLandmarks = routeLandmarks;
        this.routePoints = routePoints;
        this.routeCities = routeCities;
    }
}
