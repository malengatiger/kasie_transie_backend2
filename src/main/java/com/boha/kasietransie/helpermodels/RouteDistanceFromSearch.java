package com.boha.kasietransie.helpermodels;

import lombok.Data;

@Data
public class RouteDistanceFromSearch {

    String routeName, startCityName;
    double distanceInMetres;
}
