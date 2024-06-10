package com.nickwellman.collections.repository;

import com.nickwellman.collections.GenericService;

import java.sql.SQLException;
import java.util.List;

public interface Repository extends GenericService {

    List<RepositoryItem> getAllRepositoryItems(String itemDescriptor) throws SQLException;

    RepositoryItem getRepositoryItem(final String id, String itemDescriptor) throws SQLException;

    List<RepositoryItem> getRepositoryItems(List<String> ids, String itemDescriptor) throws SQLException;

    List<RepositoryItem> getRepositoryItems(final String value, final String propertyName, final String itemDescriptor) throws SQLException;
}
