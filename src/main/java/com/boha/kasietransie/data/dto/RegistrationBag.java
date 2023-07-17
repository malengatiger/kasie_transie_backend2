package com.boha.kasietransie.data.dto;

import lombok.Data;

@Data
public class RegistrationBag {
    private Association association;
    private User user;
    private SettingsModel settings;
    private Country country;

    public RegistrationBag(Association association, User user, SettingsModel settings, Country country) {
        this.association = association;
        this.user = user;
        this.settings = settings;
        this.country = country;
    }
}
