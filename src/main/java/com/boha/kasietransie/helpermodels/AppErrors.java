package com.boha.kasietransie.helpermodels;

import com.boha.kasietransie.data.dto.AppError;
import lombok.Data;

import java.util.List;

@Data
public class AppErrors {
    List<AppError> appErrorList;
}
