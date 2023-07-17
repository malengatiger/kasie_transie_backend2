package com.boha.kasietransie.data;

import lombok.Data;

@Data
public class CounterBag {
    long count;
    String description;

    public CounterBag(long count, String description) {
        this.count = count;
        this.description = description;
    }
}
