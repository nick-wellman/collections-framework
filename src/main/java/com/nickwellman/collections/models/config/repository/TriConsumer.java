package com.nickwellman.collections.models.config.repository;

import java.sql.SQLException;

@FunctionalInterface
public interface TriConsumer<A, B, C> {

    void apply(A a, B b, C c) throws SQLException;
}
