package com.larslab.fasting.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fastingServiceOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
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
                .version("1.0.0")
                .contact(contact)
                .description("Ein REST-API Service zur Verwaltung von Fasten-Sessions mit JWT Authentication. " +
                           "Mit diesem Service können Sie Fasten-Sessions starten, stoppen, " +
                           "den aktuellen Status abfragen und die Historie einsehen.\n\n" +
                           "🔒 **SECURITY**: User-specific endpoints require JWT authentication.\n" +
                           "1. Login with POST /api/users/login-or-create\n" +
                           "2. Use the returned 'token' in Authorization header\n" +
                           "3. Format: 'Bearer YOUR_JWT_TOKEN'")
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, dockerServer))
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Authentication\n\n" +
                                        "To access secured endpoints:\n" +
                                        "1. Login with POST /api/users/login-or-create\n" +
                                        "2. Copy the 'token' from the response\n" +
                                        "3. Click 'Authorize' button above\n" +
                                        "4. Enter: Bearer YOUR_JWT_TOKEN\n\n" +
                                        "⚠️ Secured endpoints: All /api/fast/user/** endpoints require JWT")));
    }
}
