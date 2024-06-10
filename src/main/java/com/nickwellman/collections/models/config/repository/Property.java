package com.nickwellman.collections.models.config.repository;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Property {
    private String name;
    private String column;
    @SerializedName(value = "data-type")
    private DataType dataType;
}
