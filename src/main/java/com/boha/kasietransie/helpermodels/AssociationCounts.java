package com.boha.kasietransie.helpermodels;

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
