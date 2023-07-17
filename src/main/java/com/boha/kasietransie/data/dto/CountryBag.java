package com.boha.kasietransie.data.dto;

import lombok.Data;

import java.util.List;

@Data
public class CountryBag {
    private List<Country> countries;
    private List<State> states;
    private List<City> cities;

    @Override
    public String toString() {
        return "\uD83E\uDD66\uD83E\uDD66 CountryBag {" +
                " \n \uD83C\uDF4E countries=" + countries.size() +
                ", \n \uD83C\uDF4E states=" + states.size() +
                ", \n \uD83C\uDF4E cities=" + cities.size() +
                '}';
    }
}
