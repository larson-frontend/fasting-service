package com.larslab.fasting.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

@Configuration
// Only active when NOT running with the 'test' profile. Tests should use the H2 datasource from application-test.properties.
@Profile("!test")
public class DatabaseConfigSimple {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfigSimple.class);

    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        logger.info("Configuring DataSource with DATABASE_URL: {}", 
            databaseUrl.isEmpty() ? "EMPTY" : databaseUrl.substring(0, Math.min(50, databaseUrl.length())) + "...");
        
        if (!databaseUrl.isEmpty()) {
            try {
                // Support both postgres:// and postgresql:// schemes from Render
                String normalized = databaseUrl.replaceFirst("^postgres(ql)?://", "postgresql://");
                java.net.URI uri = java.net.URI.create(normalized);

                String userInfo = uri.getUserInfo();
                if (userInfo == null || !userInfo.contains(":")) {
                    throw new IllegalArgumentException("DATABASE_URL missing user or password in userinfo");
                }
                String user = userInfo.substring(0, userInfo.indexOf(':'));
                String password = userInfo.substring(userInfo.indexOf(':') + 1);

                String host = uri.getHost();
                int portNum = uri.getPort() == -1 ? 5432 : uri.getPort();
                String path = uri.getPath();
                if (path == null || path.length() <= 1) {
                    throw new IllegalArgumentException("DATABASE_URL missing database name in path");
                }
                String database = path.startsWith("/") ? path.substring(1) : path;

                String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s?sslmode=require", host, portNum, database);

                logger.info("Parsed connection details - Host: {}, Port: {}, Database: {}, User: {}",
                    host, portNum, database, user);
                logger.info("Generated JDBC URL: {}", jdbcUrl);

                config.setJdbcUrl(jdbcUrl);
                config.setUsername(user);
                config.setPassword(password);
            } catch (Exception e) {
                logger.error("Failed to parse DATABASE_URL: {}", e.getMessage());
                throw new RuntimeException("Failed to parse DATABASE_URL", e);
            }
        } else {
            logger.warn("DATABASE_URL is empty, using fallback configuration");
            // Fallback configuration
            config.setJdbcUrl("jdbc:postgresql://localhost:5432/postgres?sslmode=require");
            config.setUsername("postgres");
            config.setPassword("password");
        }
        
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        return new HikariDataSource(config);
    }
}
