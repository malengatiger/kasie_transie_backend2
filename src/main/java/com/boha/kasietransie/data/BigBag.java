package com.boha.kasietransie.data;

import com.boha.kasietransie.data.dto.*;
import lombok.Data;

import java.util.List;

@Data
public class BigBag {
    List<VehicleArrival> vehicleArrivals;
    List<DispatchRecord> dispatchRecords;
    List<VehicleHeartbeat> vehicleHeartbeats;
    List<VehicleDeparture> vehicleDepartures;
    List<AmbassadorPassengerCount> passengerCounts;
}
