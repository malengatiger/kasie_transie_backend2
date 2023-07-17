package com.boha.kasietransie.data;

import lombok.Data;

@Data
public class RouteDistanceFromSearch {

    String routeName, startCityName;
    double distanceInMetres;
}
