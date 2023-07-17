package com.boha.kasietransie.util;

import lombok.Data;

@Data
public class CustomErrorResponse {
    int statusCode;
    String message;
    String date;

    public CustomErrorResponse(int statusCode, String message, String date) {
        this.statusCode = statusCode;
        this.message = message;
        this.date = date;
    }

}
