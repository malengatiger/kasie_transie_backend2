package com.boha.kasietransie.data;

import lombok.Data;

@Data
public class AssociationCounts {
   long passengerCounts;
    long heartbeats;
    long arrivals;
    long departures;
    long dispatchRecords;
    long commuterRequests;
    String created;
}
