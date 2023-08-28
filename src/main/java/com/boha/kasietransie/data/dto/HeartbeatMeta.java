package com.boha.kasietransie.data.dto;

import lombok.Data;

@Data
public class HeartbeatMeta {
    String vehicleId;
    String associationId;
    String ownerId;
    String vehicleReg;
    double latitude;
    double longitude;
}
