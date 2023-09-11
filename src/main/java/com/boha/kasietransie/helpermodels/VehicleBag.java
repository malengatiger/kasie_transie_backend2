package com.boha.kasietransie.helpermodels;

import com.boha.kasietransie.data.dto.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data

public class VehicleBag {
    String vehicleId;
    String created;
    List<DispatchRecord> dispatchRecords = new ArrayList<>();
    List<VehicleHeartbeat> heartbeats = new ArrayList<>();
    List<AmbassadorPassengerCount> passengerCounts = new ArrayList<>();
    List<VehicleArrival> arrivals = new ArrayList<>();
    List<VehicleDeparture> departures = new ArrayList<>();
}
