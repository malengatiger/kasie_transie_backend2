package com.boha.kasietransie.data.dto;

import lombok.Data;

import java.util.List;

@Data
public class Position {
    String type;
    List<Double> coordinates;

    public Position() {
    }

    public Position(String point, List<Double> coords) {

    }
//    double latitude;
//    double longitude;
//    //String geoHash;


}
