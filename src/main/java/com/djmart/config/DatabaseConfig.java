package com.djmart.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration holder for database connectivity and HikariCP connection pool parameters.
 * Loads configuration with precedence:
 * 1. Explicit properties passed in constructor / load
 * 2. Environment variables (DB_URL, DB_USER, etc.)
 * 3. File system config.properties (if exists)
 * 4. Classpath config.properties (if exists)
 * 5. Sensible development defaults
 */
public class DatabaseConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConfig.class);

    private String jdbcUrl;
    private String username;
    private String password;
    private String driverClassName;

    private int maximumPoolSize;
    private int minimumIdle;
    private long idleTimeoutMs;
    private long connectionTimeoutMs;
    private long maxLifetimeMs;
    private long leakDetectionThresholdMs;

    public DatabaseConfig() {
        loadDefaults();
        loadProperties(null);
        applyEnvironmentVariables();
    }

    public DatabaseConfig(String propertiesResourcePath) {
        loadDefaults();
        loadProperties(propertiesResourcePath);
        applyEnvironmentVariables();
    }

    private void loadDefaults() {
        // Automatically ensure data directory exists in runtime environment
        try {
            File dataDir = new File("data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            String catalinaBase = System.getProperty("catalina.base");
            if (catalinaBase != null && !catalinaBase.trim().isEmpty()) {
                File tomcatDataDir = new File(catalinaBase, "data");
                if (!tomcatDataDir.exists()) {
                    tomcatDataDir.mkdirs();
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Unable to pre-create data directory: {}", e.getMessage());
        }

        this.jdbcUrl = "jdbc:h2:./data/djmart;DB_CLOSE_DELAY=-1;MODE=REGULAR;AUTO_SERVER=TRUE";
        this.username = "sa";
        this.password = "";
        this.driverClassName = "org.h2.Driver";

        this.maximumPoolSize = 10;
        this.minimumIdle = 2;
        this.idleTimeoutMs = 30000L;
        this.connectionTimeoutMs = 20000L;
        this.maxLifetimeMs = 1800000L;
        this.leakDetectionThresholdMs = 15000L;
    }

    private void loadProperties(String resourcePath) {
        Properties props = new Properties();

        // 1. Check specified resource path
        if (resourcePath != null) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                if (in != null) {
                    props.load(in);
                    LOGGER.info("Loaded database configuration from classpath resource: {}", resourcePath);
                } else {
                    File file = new File(resourcePath);
                    if (file.exists()) {
                        try (FileInputStream fin = new FileInputStream(file)) {
                            props.load(fin);
                            LOGGER.info("Loaded database configuration from file: {}", file.getAbsolutePath());
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to load configuration from {}: {}", resourcePath, e.getMessage());
            }
        } else {
            // 2. Check config.properties on working directory or classpath
            File localFile = new File("config.properties");
            if (localFile.exists()) {
                try (FileInputStream fin = new FileInputStream(localFile)) {
                    props.load(fin);
                    LOGGER.info("Loaded database configuration from local file: {}", localFile.getAbsolutePath());
                } catch (Exception e) {
                    LOGGER.warn("Failed to read local config.properties: {}", e.getMessage());
                }
            } else {
                try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.properties")) {
                    if (in != null) {
                        props.load(in);
                        LOGGER.info("Loaded database configuration from classpath config.properties");
                    }
                } catch (Exception e) {
                    LOGGER.warn("Failed to read classpath config.properties: {}", e.getMessage());
                }
            }
        }

        applyProperties(props);
    }

    private void applyProperties(Properties props) {
        if (props.containsKey("db.url")) {
            this.jdbcUrl = props.getProperty("db.url").trim();
        }
        if (props.containsKey("db.user")) {
            this.username = props.getProperty("db.user").trim();
        }
        if (props.containsKey("db.password")) {
            this.password = props.getProperty("db.password").trim();
        }
        if (props.containsKey("db.driver")) {
            this.driverClassName = props.getProperty("db.driver").trim();
        }

        if (props.containsKey("hikari.maximumPoolSize")) {
            this.maximumPoolSize = Integer.parseInt(props.getProperty("hikari.maximumPoolSize").trim());
        }
        if (props.containsKey("hikari.minimumIdle")) {
            this.minimumIdle = Integer.parseInt(props.getProperty("hikari.minimumIdle").trim());
        }
        if (props.containsKey("hikari.idleTimeout")) {
            this.idleTimeoutMs = Long.parseLong(props.getProperty("hikari.idleTimeout").trim());
        }
        if (props.containsKey("hikari.connectionTimeout")) {
            this.connectionTimeoutMs = Long.parseLong(props.getProperty("hikari.connectionTimeout").trim());
        }
        if (props.containsKey("hikari.maxLifetime")) {
            this.maxLifetimeMs = Long.parseLong(props.getProperty("hikari.maxLifetime").trim());
        }
        if (props.containsKey("hikari.leakDetectionThreshold")) {
            this.leakDetectionThresholdMs = Long.parseLong(props.getProperty("hikari.leakDetectionThreshold").trim());
        }
    }

    private void applyEnvironmentVariables() {
        String envUrl = System.getenv("DATABASE_URL");
        if (envUrl == null || envUrl.isBlank()) {
            envUrl = System.getenv("DB_URL");
        }
        if (envUrl != null && !envUrl.isBlank()) {
            envUrl = envUrl.trim();
            // Handle postgres:// or postgresql:// URI format from Render/Heroku/Neon/Supabase
            if (envUrl.startsWith("postgres://") || envUrl.startsWith("postgresql://")) {
                try {
                    java.net.URI uri = new java.net.URI(envUrl);
                    String userInfo = uri.getUserInfo();
                    if (userInfo != null && userInfo.contains(":")) {
                        String[] parts = userInfo.split(":", 2);
                        if (this.username == null || this.username.equals("sa")) {
                            this.username = parts[0];
                        }
                        if (this.password == null || this.password.isEmpty()) {
                            this.password = parts[1];
                        }
                    }
                    int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                    String path = uri.getPath();
                    this.jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + path;
                    if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
                        this.jdbcUrl += "?" + uri.getQuery();
                    }
                } catch (Exception ex) {
                    // Fallback to simple replace
                    this.jdbcUrl = envUrl.replaceFirst("^postgres://", "jdbc:postgresql://");
                }
                this.driverClassName = "org.postgresql.Driver";
            } else {
                this.jdbcUrl = envUrl;
                if (envUrl.contains("postgresql")) {
                    this.driverClassName = "org.postgresql.Driver";
                }
            }
        }

        String envUser = System.getenv("DB_USERNAME");
        if (envUser == null || envUser.isBlank()) {
            envUser = System.getenv("DB_USER");
        }
        if (envUser != null && !envUser.isBlank()) {
            this.username = envUser.trim();
        }

        String envPass = System.getenv("DB_PASSWORD");
        if (envPass != null) {
            this.password = envPass.trim();
        }

        String envDriver = System.getenv("DB_DRIVER");
        if (envDriver != null && !envDriver.isBlank()) {
            this.driverClassName = envDriver.trim();
        }

        String envPoolSize = System.getenv("HIKARI_MAX_POOL_SIZE");
        if (envPoolSize != null && !envPoolSize.isBlank()) {
            try {
                this.maximumPoolSize = Integer.parseInt(envPoolSize.trim());
            } catch (NumberFormatException ignored) {}
        }
        String envMinIdle = System.getenv("HIKARI_MIN_IDLE");
        if (envMinIdle != null && !envMinIdle.isBlank()) {
            try {
                this.minimumIdle = Integer.parseInt(envMinIdle.trim());
            } catch (NumberFormatException ignored) {}
        }
    }

    // Getters and Setters
    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public int getMinimumIdle() {
        return minimumIdle;
    }

    public void setMinimumIdle(int minimumIdle) {
        this.minimumIdle = minimumIdle;
    }

    public long getIdleTimeoutMs() {
        return idleTimeoutMs;
    }

    public void setIdleTimeoutMs(long idleTimeoutMs) {
        this.idleTimeoutMs = idleTimeoutMs;
    }

    public long getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    public void setConnectionTimeoutMs(long connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }

    public long getMaxLifetimeMs() {
        return maxLifetimeMs;
    }

    public void setMaxLifetimeMs(long maxLifetimeMs) {
        this.maxLifetimeMs = maxLifetimeMs;
    }

    public long getLeakDetectionThresholdMs() {
        return leakDetectionThresholdMs;
    }

    public void setLeakDetectionThresholdMs(long leakDetectionThresholdMs) {
        this.leakDetectionThresholdMs = leakDetectionThresholdMs;
    }
}
