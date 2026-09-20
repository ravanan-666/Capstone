package com.djmart.dao;

import com.djmart.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Base abstract DAO class providing connection management for all DAO implementations.
 * Strict rule: All queries must use PreparedStatement and try-with-resources.
 */
public abstract class BaseDAO {

    /**
     * Obtains a connection from the configured HikariCP connection pool.
     *
     * @return active connection
     * @throws SQLException on pool or connection error
     */
    protected Connection getConnection() throws SQLException {
        return DatabaseUtil.getConnection();
    }
}
