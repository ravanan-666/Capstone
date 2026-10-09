package com.djmart;

import com.djmart.config.DatabaseConfig;
import com.djmart.util.DatabaseUtil;
import org.junit.jupiter.api.Test;

import java.sql.*;

public class InspectDatabaseLive {

    @Test
    public void inspectLiveDatabase() throws Exception {
        DatabaseConfig config = new DatabaseConfig();
        System.out.println("Connecting to DB URL: " + config.getJdbcUrl());
        DatabaseUtil.initDataSource(config);

        try (Connection conn = DatabaseUtil.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("=== DATABASE METADATA ===");
            System.out.println("Product Name: " + meta.getDatabaseProductName());
            System.out.println("Product Version: " + meta.getDatabaseProductVersion());
            System.out.println("Driver Name: " + meta.getDriverName());

            System.out.println("\n=== TABLES & ROW COUNTS ===");
            try (ResultSet tables = meta.getTables(null, "PUBLIC", "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    int count = 0;
                    try (Statement stmt = conn.createStatement();
                         ResultSet countRs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
                        if (countRs.next()) count = countRs.getInt(1);
                    } catch (Exception ignored) {}
                    System.out.println("TABLE: " + tableName + " | ROWS: " + count);
                }
            }

            System.out.println("\n=== COLUMNS PER TABLE ===");
            try (ResultSet tables = meta.getTables(null, "PUBLIC", "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    System.out.println("\nColumns for [" + tableName + "]:");
                    try (ResultSet cols = meta.getColumns(null, "PUBLIC", tableName, "%")) {
                        while (cols.next()) {
                            String colName = cols.getString("COLUMN_NAME");
                            String colType = cols.getString("TYPE_NAME");
                            int colSize = cols.getInt("COLUMN_SIZE");
                            String isNullable = cols.getString("IS_NULLABLE");
                            System.out.printf("  - %-25s %-15s (size: %d, nullable: %s)%n", colName, colType, colSize, isNullable);
                        }
                    }

                    // Primary Keys
                    try (ResultSet pks = meta.getPrimaryKeys(null, "PUBLIC", tableName)) {
                        while (pks.next()) {
                            System.out.println("    [PRIMARY KEY]: " + pks.getString("COLUMN_NAME"));
                        }
                    }

                    // Foreign Keys
                    try (ResultSet fks = meta.getImportedKeys(null, "PUBLIC", tableName)) {
                        while (fks.next()) {
                            System.out.println("    [FOREIGN KEY]: " + fks.getString("FKCOLUMN_NAME") +
                                    " -> REFERENCES " + fks.getString("PKTABLE_NAME") + "(" + fks.getString("PKCOLUMN_NAME") + ")");
                        }
                    }
                }
            }
        } finally {
            DatabaseUtil.closeDataSource();
        }
    }
}
