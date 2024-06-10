package com.nickwellman.collections.jdbc;

import com.nickwellman.collections.GenericService;
import com.nickwellman.collections.models.config.repository.ItemDescriptor;
import com.nickwellman.collections.models.config.repository.Property;
import com.nickwellman.collections.models.config.repository.Table;
import com.nickwellman.collections.models.config.repository.TriConsumer;
import com.nickwellman.collections.repository.RepositoryItem;
import lombok.Getter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.function.BiFunction;

@Getter
public abstract class DataSource implements GenericService {

    protected String host;
    protected String database;
    protected String username;
    protected String password;

    public DataSource(final String host, final String database, final String username, final String password) {
        this.host = host;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public List<RepositoryItem> query(final String sql,
                                      final ItemDescriptor itemDescriptor,
                                      final BiFunction<ItemDescriptor, ResultSet, List<RepositoryItem>> function) throws SQLException {
        final String url = connectionString();
        try (final Connection conn = DriverManager.getConnection(url)) {

            final PreparedStatement stmt = conn.prepareStatement(sql);
            final ResultSet rs = stmt.executeQuery();

            return function.apply(itemDescriptor, rs);
        }
    }

    public String insert(final String sql,
                         final Table table,
                         final RepositoryItem item,
                         final TriConsumer<Table, RepositoryItem, PreparedStatement> consumer) throws SQLException {
        try (final Connection conn = DriverManager.getConnection(connectionString())) {
            final PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            consumer.apply(table, item, stmt);
            stmt.executeUpdate();
            final ResultSet rs = stmt.getGeneratedKeys();
            rs.next();
            return rs.getString("GENERATED_KEY");
        }
    }

    public void update(final String sql,
                       final RepositoryItem item,
                       final List<Property> properties,
                       final TriConsumer<PreparedStatement, RepositoryItem, List<Property>> consumer) {
        try (final Connection conn = DriverManager.getConnection(connectionString())) {
            final PreparedStatement stmt = conn.prepareStatement(sql);
            consumer.apply(stmt, item, properties);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract String connectionString();
}
