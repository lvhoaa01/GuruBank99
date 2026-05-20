package com.example.bankingapp.repository;

import com.example.bankingapp.BuildConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Minimal JDBC connection factory. Each call opens a fresh {@link Connection};
 * callers are expected to use try-with-resources so the connection is closed
 * promptly. No pooling — fine for emulator-local PostgreSQL.
 *
 * URL / user / password come from {@code BuildConfig} so the values are
 * defined in one place ({@code app/build.gradle}).
 */
public final class JdbcConnectionProvider {

    static {
        try {
            // Force-load the PostgreSQL driver. Service-loader registration
            // is finicky on Android, so we do it explicitly.
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("PostgreSQL JDBC driver not on classpath", e);
        }
    }

    private JdbcConnectionProvider() { }

    public static Connection get() {
        try {
            return DriverManager.getConnection(
                    BuildConfig.DB_URL, BuildConfig.DB_USER, BuildConfig.DB_PASS);
        } catch (SQLException e) {
            throw new RuntimeException("Could not open connection to " + BuildConfig.DB_URL, e);
        }
    }
}
