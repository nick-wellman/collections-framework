package com.nickwellman.collections.repository;

public interface RepositoryItem {

    Object getPropertyValue(String propertyName);

    String getItemDescriptorName();
}
