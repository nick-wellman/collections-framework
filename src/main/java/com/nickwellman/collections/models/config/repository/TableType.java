package com.nickwellman.collections.models.config.repository;

import com.google.gson.annotations.SerializedName;

public enum TableType {
    @SerializedName("primary") PRIMARY("primary"),
    @SerializedName("multi") MULTI("multi");

    private final String value;

    TableType(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
