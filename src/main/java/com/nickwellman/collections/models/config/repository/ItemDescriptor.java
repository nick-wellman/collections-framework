package com.nickwellman.collections.models.config.repository;

import lombok.Data;

import java.util.List;

@Data
public class ItemDescriptor {
    private String name;
    private List<Table> tables;
}
