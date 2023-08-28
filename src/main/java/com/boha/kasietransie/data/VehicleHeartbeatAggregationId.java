package com.boha.kasietransie.data;

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
        return "HeartbeatAggregationId{" +
                "year=" + year +
                ", month=" + month +
                ", day=" + day +
                '}';
    }
}
