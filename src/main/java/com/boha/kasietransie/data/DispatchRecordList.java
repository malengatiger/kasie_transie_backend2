package com.boha.kasietransie.data;

import com.boha.kasietransie.data.dto.DispatchRecord;
import lombok.Data;

import java.util.List;

@Data
public class DispatchRecordList {
    private List<DispatchRecord> dispatchRecords;
}
