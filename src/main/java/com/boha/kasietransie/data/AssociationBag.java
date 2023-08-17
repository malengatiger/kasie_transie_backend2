package com.boha.kasietransie.data;

import com.boha.kasietransie.data.dto.*;
import lombok.Data;

import java.util.List;

@Data
public class AssociationBag {
    List<AmbassadorPassengerCount> passengerCounts;
    List<VehicleHeartbeat> heartbeats;
    List<VehicleArrival> arrivals;
    List<VehicleDeparture> departures;
    List<DispatchRecord> dispatchRecords;
}
