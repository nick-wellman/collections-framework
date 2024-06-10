package com.nickwellman.collections.models.config.repository;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class Table {
    private String name;
    @SerializedName(value = "id-column")
    private String idColumn;
    @SerializedName(value = "id-column-name")
    private String idColumnName;
    private TableType type;
    private List<Property> properties;
}
