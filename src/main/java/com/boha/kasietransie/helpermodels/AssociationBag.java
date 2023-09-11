package com.boha.kasietransie.helpermodels;

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
    List<CommuterRequest> commuterRequests;
}
