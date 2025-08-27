package com.larslab.fasting.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        DataSourceProperties properties = new DataSourceProperties();
        
        logger.info("Raw DATABASE_URL: {}", databaseUrl);
        
        if (!databaseUrl.isEmpty()) {
            // Convert Render's postgresql:// URL to jdbc:postgresql:// format
            String jdbcUrl = databaseUrl.startsWith("postgresql://") 
                ? databaseUrl.replace("postgresql://", "jdbc:postgresql://")
                : databaseUrl;
            
            logger.info("Converted JDBC URL: {}", jdbcUrl);
            properties.setUrl(jdbcUrl);
        } else {
            logger.warn("DATABASE_URL is empty, using fallback configuration");
            // Fallback to individual environment variables
            String host = System.getenv().getOrDefault("DB_HOST", "localhost");
            String port = System.getenv().getOrDefault("DB_PORT", "5432");
            String database = System.getenv().getOrDefault("DB_NAME", "postgres");
            String fallbackUrl = String.format("jdbc:postgresql://%s:%s/%s?sslmode=require", host, port, database);
            logger.info("Fallback JDBC URL: {}", fallbackUrl);
            properties.setUrl(fallbackUrl);
        }
        
        return properties;
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        return dataSourceProperties().initializeDataSourceBuilder().build();
    }
}
