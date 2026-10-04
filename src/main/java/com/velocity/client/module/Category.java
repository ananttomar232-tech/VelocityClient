package com.velocity.client.module;

public enum Category {
    HUD("HUD"),
    PVP("PVP"),
    UTILITY("UTILITY"),
    PERFORMANCE("PERFORMANCE");

    public final String label;

    Category(String label) {
        this.label = label;
    }
}
