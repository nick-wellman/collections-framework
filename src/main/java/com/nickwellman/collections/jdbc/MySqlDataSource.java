package com.nickwellman.collections.jdbc;

public class MySqlDataSource extends DataSource {

    public MySqlDataSource(final String host, final String database, final String username, final String password) {
        super(host, database, username, password);
    }

    @Override
    protected String connectionString() {
        return String.format("%s/%s?user=%s&password=%s", host, database, username, password);
    }
}
