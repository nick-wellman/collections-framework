package com.nickwellman.collections.models.config.repository;

import com.google.gson.annotations.SerializedName;

public enum DataType {
    @SerializedName("string") STRING("string"),
    @SerializedName("int") INT("int"),
    @SerializedName("double") DOUBLE("double"),
    @SerializedName("binary") BINARY("binary");

    private final String value;

    private DataType(final String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }
}
