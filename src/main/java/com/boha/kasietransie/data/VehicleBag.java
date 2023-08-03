package com.boha.kasietransie.data;

import com.boha.kasietransie.data.dto.*;
import lombok.Data;

import java.util.List;
@Data

public class VehicleBag {
    String vehicleId;
    String created;
    List<DispatchRecord> dispatchRecords;
    List<VehicleHeartbeat> heartbeats;
    List<AmbassadorPassengerCount> passengerCounts;
    List<VehicleArrival> arrivals;
    List<VehicleDeparture> departures;
}
