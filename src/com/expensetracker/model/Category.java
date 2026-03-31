package com.expensetracker.model;

public enum Category {
    FOOD("Food & Dining"),
    TRANSPORT("Transport & Travel"),
    ENTERTAINMENT("Entertainment"),
    STUDY("Books & Study Material"),
    RENT("Hostel Rent"),
    OTHER("Miscellaneous");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
