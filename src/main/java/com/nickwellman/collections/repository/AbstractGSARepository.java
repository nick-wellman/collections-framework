package com.nickwellman.collections.repository;

import com.nickwellman.collections.jdbc.DataSource;
import com.nickwellman.collections.models.config.repository.ItemDescriptor;
import com.nickwellman.collections.models.config.repository.Property;
import com.nickwellman.collections.models.config.repository.Table;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@Slf4j
public abstract class AbstractGSARepository implements MutableRepository {

    private DataSource mDatasource;
    private Map<String, ItemDescriptor> mItemDescriptors;

    public AbstractGSARepository() {
        mItemDescriptors = new HashMap<>();
    }

    @Override
    public List<RepositoryItem> getAllRepositoryItems(final String itemDescriptor) throws SQLException {
        // currently, only going to support single tables for item descriptors
        final ItemDescriptor id = mItemDescriptors.get(itemDescriptor);
        final String query = String.format("select * from %s", id.getTables().get(0).getName());
        final List<RepositoryItem> items = mDatasource.query(query, id, this::hydrateItem);

        return items;
    }

    @Override
    public RepositoryItem getRepositoryItem(final String id, final String itemDescriptor) throws SQLException {
        final ItemDescriptor itemDesc = mItemDescriptors.get(itemDescriptor);
        final Table table = itemDesc.getTables().get(0);
        final String query = String.format("select * from %s where %s=\"%s\"", table.getName(), table.getIdColumn(), id);
        final List<RepositoryItem> items = mDatasource.query(query, itemDesc, this::hydrateItem);

        return items.get(0);
    }

    @Override
    public List<RepositoryItem> getRepositoryItems(final String value, final String propertyName, final String itemDescriptor) throws SQLException {
        final ItemDescriptor itemDesc = mItemDescriptors.get(itemDescriptor);
        final Table table = itemDesc.getTables().get(0);
        final String columnName = table.getProperties()
                                       .stream()
                                       .filter(prop -> prop.getName().equals(propertyName))
                                       .map(Property::getColumn)
                                       .findFirst()
                                       .orElseThrow();
        final String query = String.format("select * from %s where %s=\"%s\"", table.getName(), columnName, value);
        final List<RepositoryItem> items = mDatasource.query(query, itemDesc, this::hydrateItem);

        return items;
    }

    @Override
    public List<RepositoryItem> getRepositoryItems(final List<String> ids, final String itemDescriptor) throws SQLException {
        final ItemDescriptor itemDesc = mItemDescriptors.get(itemDescriptor);
        final Table table = itemDesc.getTables().get(0);
        final String query = String.format("select * from %s where id in (%s)", table.getName(), String.join(",", ids));
        return mDatasource.query(query, itemDesc, this::hydrateItem);
    }

    @Override
    public MutableRepositoryItem createItem(final String itemDescriptor) {
        final MutableRepositoryItem item = new RepositoryItemImpl();
        item.setItemDescriptorName(itemDescriptor);
        return item;
    }

    @Override
    public RepositoryItem updateItem(final RepositoryItem repositoryItem, final String... fieldsToUpdate) {
        if (fieldsToUpdate.length == 0) {
            throw new IllegalArgumentException("fieldsToUpdate is a required field");
        }

        final Table table = mItemDescriptors.get(repositoryItem.getItemDescriptorName()).getTables().get(0);
        final List<Property> properties = table.getProperties();
        final StringBuilder update = new StringBuilder();
        final List<Property> updateProperties = new ArrayList<>();
        int index = 0;
        for (final String field : fieldsToUpdate) {
            final Optional<Property> optional = properties.stream().filter(p -> p.getName().equals(field)).findFirst();
            if (optional.isEmpty()) {
                throw new IllegalStateException("Unable to find field: " + field);
            }

            if (index != 0) {
                update.append(" and ");
            }

            update.append(optional.get().getColumn()).append("=?");
            updateProperties.add(optional.get());

            ++index;
        }
        final String insert = String.format("update %s set %s where %s='%s'",
                                            table.getName(),
                                            update,
                                            table.getIdColumn(),
                                            repositoryItem.getPropertyValue(table.getIdColumnName()));
        log.info("update sql: {}", update);
        log.info("insert sql: {}", insert);
        mDatasource.update(insert, repositoryItem, updateProperties, this::createPreparedStatement);
        return repositoryItem;
    }

    @Override
    public RepositoryItem addItem(final MutableRepositoryItem mutableRepositoryItem) throws IllegalStateException, SQLException {
        final Table table = mItemDescriptors.get(mutableRepositoryItem.getItemDescriptorName()).getTables().get(0);
        final List<Property> properties = table.getProperties();
        final StringBuilder fields = new StringBuilder();
        final StringBuilder values = new StringBuilder();
        int index = 0;
        for (final Property property : properties) {
            if (mutableRepositoryItem.getPropertyValue(property.getName()) == null) {
                continue;
            }

            if (index != 0) {
                fields.append(",");
                values.append(",");
            }
            fields.append(property.getColumn());
            values.append("?");
            index++;
        }
        final String insert = String.format("insert into %s (%s) values (%s)", table.getName(), fields, values);
        final String id = mDatasource.insert(insert, table, mutableRepositoryItem, this::createPreparedStatement);
        log.info("last insert id " + id);
        return getRepositoryItem(id, mutableRepositoryItem.getItemDescriptorName());
    }

    private List<RepositoryItem> hydrateItem(final ItemDescriptor id, final ResultSet rs) {
        final List<RepositoryItem> items = new ArrayList<>();
        try {
            while (rs.next()) {
                final RepositoryItemImpl item = new RepositoryItemImpl();
                item.setItemDescriptorName(id.getName());
                for (final Property property : id.getTables().get(0).getProperties()) {
                    switch (property.getDataType()) {
                        case INT -> item.setProperty(property.getName(), rs.getInt(property.getColumn()));
                        case DOUBLE -> item.setProperty(property.getName(), rs.getDouble(property.getColumn()));
                        case STRING -> item.setProperty(property.getName(), rs.getString(property.getColumn()));
                        case BINARY -> item.setProperty(property.getName(), rs.getBytes(property.getColumn()));
                        default -> throw new IllegalArgumentException("cant find data type");
                    }
                }
                items.add(item);
            }
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
        return items;
    }

    private void createPreparedStatement(final Table table, final RepositoryItem item, final PreparedStatement stmt) throws SQLException {
        final List<Property> properties = table.getProperties();
        createPreparedStatement(stmt, item, properties);
    }

    private void createPreparedStatement(final PreparedStatement stmt,
                                         final RepositoryItem item,
                                         final List<Property> properties) throws SQLException {
        int stmtIndex = 0;
        for (final Property property : properties) {
            final String propertyName = property.getName();

            if (item.getPropertyValue(propertyName) == null) {
                continue;
            }
            stmtIndex++;
            switch (property.getDataType()) {
                case INT -> stmt.setInt(stmtIndex, (Integer) item.getPropertyValue(propertyName));
                case DOUBLE -> stmt.setDouble(stmtIndex, (Double) item.getPropertyValue(propertyName));
                case STRING -> stmt.setString(stmtIndex, (String) item.getPropertyValue(propertyName));
                case BINARY -> stmt.setBytes(stmtIndex, (byte[]) item.getPropertyValue(propertyName));
                default -> throw new IllegalArgumentException("cant find data type");
            }
        }
    }
}
