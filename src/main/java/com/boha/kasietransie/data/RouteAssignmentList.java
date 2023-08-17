package com.boha.kasietransie.data;

import com.boha.kasietransie.data.dto.RouteAssignment;
import lombok.Data;

import java.util.List;

@Data
public class RouteAssignmentList {
    List<RouteAssignment> assignments;
}
