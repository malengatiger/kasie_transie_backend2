package com.boha.kasietransie.data;

import lombok.Data;

@Data
public class AssociationHeartbeatAggregationId {
    private int year;
    private int month;
    private int day;
    private int hour;
    private String associationId;


    public AssociationHeartbeatAggregationId() {
    }

    public AssociationHeartbeatAggregationId(int year, int month, int day, int hour, String associationId) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.associationId = associationId;
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
