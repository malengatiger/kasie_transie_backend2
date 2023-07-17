package com.boha.kasietransie.util;

import lombok.Data;

@Data
public class VehicleUploadResponse {
    String vehicleReg;
    String ownerName;
    String cellphone;
    boolean ok;

    public VehicleUploadResponse(String vehicleReg, String ownerName, boolean ok, String cellphone) {
        this.vehicleReg = vehicleReg;
        this.ownerName = ownerName;
        this.ok = ok;
        this.cellphone = cellphone;
    }
}
