package com.boha.kasietransie.data.dto;

import lombok.Data;

@Data
public class Timezone {
    private String zoneName;
    private int gmtOffset;
    private String gmtOffsetName;
    private String abbreviation;
    private String tzName;
}

