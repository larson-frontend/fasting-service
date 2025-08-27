package com.larslab.fasting.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        DataSourceProperties properties = new DataSourceProperties();
        
        if (!databaseUrl.isEmpty()) {
            // Convert Render's postgresql:// URL to jdbc:postgresql:// format
            String jdbcUrl = databaseUrl.startsWith("postgresql://") 
                ? databaseUrl.replace("postgresql://", "jdbc:postgresql://")
                : databaseUrl;
            properties.setUrl(jdbcUrl);
        }
        
        return properties;
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        return dataSourceProperties().initializeDataSourceBuilder().build();
    }
}
