package com.boha.kasietransie.data;

import lombok.Data;

@Data
public class AssociationHeartbeatAggregationResult implements Comparable<AssociationHeartbeatAggregationResult>{
    private AssociationHeartbeatAggregationId id;
    private int total;

    @Override
    public int compareTo(AssociationHeartbeatAggregationResult o) {
        String dt1 = parseDate(getId().getYear(),getId().getMonth(),getId().getDay(),getId().getHour());
        String dt2 = parseDate(o.getId().getYear(),o.getId().getMonth(),o.getId().getDay(),o.getId().getHour());

        return dt1.compareTo(dt2);
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
