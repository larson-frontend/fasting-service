package com.larslab.fasting.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfigSimple {

    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        if (!databaseUrl.isEmpty()) {
            // Parse the DATABASE_URL manually if needed
            // Format: postgresql://user:password@host:port/database
            String cleanUrl = databaseUrl.replace("postgresql://", "");
            String[] parts = cleanUrl.split("@");
            String[] userPass = parts[0].split(":");
            String user = userPass[0];
            String password = userPass[1];
            
            String[] hostDb = parts[1].split("/");
            String[] hostPort = hostDb[0].split(":");
            String host = hostPort[0];
            String port = hostPort.length > 1 ? hostPort[1] : "5432";
            String database = hostDb[1];
            
            String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s?sslmode=require", host, port, database);
            
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(user);
            config.setPassword(password);
        } else {
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
