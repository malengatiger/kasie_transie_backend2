package com.boha.kasietransie.helpermodels;

import lombok.Data;

@Data
public class VehicleHeartbeatAggregationId {
    private int year;
    private int month;
    private int day;
    private int hour;
    private String vehicleId;


    public VehicleHeartbeatAggregationId() {
    }

    public VehicleHeartbeatAggregationId(int year, int month, int day, int hour, String vehicleId) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.vehicleId = vehicleId;
    }

    // Getters and setters

    @Override
    public String toString() {
        return parseDate(year,month,day,hour);
    }



    String parseDate(int year,
                     int month,
                     int day,
                     int hour) {
        //2023-08-30T18:04:20.278+02:00
        StringBuilder sb = new StringBuilder();
        sb.append(year).append("-");
        if (month < 10) {
            sb.append("0");
        }
        sb.append(month).append("-");
        if (day < 10) {
            sb.append("0");
        }
        sb.append(day).append("-");
        if (hour < 10) {
            sb.append("0");
        }
        sb.append(hour);

        return sb.toString();
    }
}
