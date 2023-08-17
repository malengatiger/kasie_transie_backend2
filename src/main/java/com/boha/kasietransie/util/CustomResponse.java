package com.boha.kasietransie.util;

import lombok.Data;

@Data
public class CustomResponse {
    int statusCode;
    String message;
    String date;

    public CustomResponse(int statusCode, String message, String date) {
        this.statusCode = statusCode;
        this.message = message;
        this.date = date;
    }

}
