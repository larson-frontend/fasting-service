package com.larslab.fasting.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fastingServiceOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8080");
        devServer.setDescription("Development Server");

        Server dockerServer = new Server();
        dockerServer.setUrl("http://localhost:8080");
        dockerServer.setDescription("Docker Server");

        Contact contact = new Contact();
        contact.setEmail("info@larslab.com");
        contact.setName("LarsLab");

        License license = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("Fasting Service API")
                .version("0.0.1")
                .contact(contact)
                .description("Ein REST-API Service zur Verwaltung von Fasten-Sessions. " +
                           "Mit diesem Service können Sie Fasten-Sessions starten, stoppen, " +
                           "den aktuellen Status abfragen und die Historie einsehen.")
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, dockerServer));
    }
}
