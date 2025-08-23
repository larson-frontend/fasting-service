package com.larslab.fasting.controller;

import com.larslab.fasting.model.FastSession;
import com.larslab.fasting.model.User;
import com.larslab.fasting.service.FastService;
import com.larslab.fasting.service.UserService;
import com.larslab.fasting.security.UserAuthorizationService;
import com.larslab.fasting.dto.StartFastRequest;
import com.larslab.fasting.dto.FastStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.validation.Valid;
import java.util.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/fast")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:8000", "http://localhost:8080", "http://localhost:4200"})
@Tag(name = "Fasting", description = "API zur Verwaltung von Fasten-Sessions mit Ziel-System")
@Validated
public class FastController {
    private final FastService service;
    private final UserService userService;
    private final UserAuthorizationService authorizationService;

    public FastController(FastService service, UserService userService, UserAuthorizationService authorizationService) {
        this.service = service;
        this.userService = userService;
        this.authorizationService = authorizationService;
    }

    @PostMapping("/start")
    @Operation(summary = "Neue Fasten-Session starten", 
               description = "Startet eine neue Fasten-Session mit optionalem Ziel oder gibt die bereits aktive Session zurück")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fasten-Session erfolgreich gestartet oder bereits aktiv"),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabedaten (goalHours muss zwischen 1 und 48 liegen)")
    })
    public FastSession start(@Valid @RequestBody(required = false) StartFastRequest request) {
        if (request == null) {
            request = new StartFastRequest(16); // Default 16 Stunden
        }
        return service.start(request);
    }

    @PostMapping("/stop")
    @Operation(summary = "Aktive Fasten-Session beenden", description = "Beendet die aktuell aktive Fasten-Session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fasten-Session erfolgreich beendet"),
            @ApiResponse(responseCode = "400", description = "Keine aktive Fasten-Session vorhanden")
    })
    public FastSession stop() {
        return service.stop();
    }

    @GetMapping("/status")
    @Operation(summary = "Status der aktuellen Fasten-Session", 
               description = "Gibt den Status der aktuellen Fasten-Session zurück (aktiv/inaktiv mit Dauer und Ziel)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status erfolgreich abgerufen")
    })
    public FastStatusResponse status() {
        return service.getStatus();
    }

    @GetMapping("/history")
    @Operation(summary = "Historie aller Fasten-Sessions", description = "Gibt eine Liste aller bisherigen Fasten-Sessions zurück")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historie erfolgreich abgerufen")
    })
    public List<FastSession> history() {
        return service.history();
    }

    // User-specific endpoints for cross-device login
    @GetMapping("/user/{identifier}/status")
    @Operation(summary = "Status der aktuellen Fasten-Session für spezifischen User", 
               description = "Gibt den Status der aktuellen Fasten-Session für einen spezifischen User zurück (über Username oder Email). Requires JWT authentication.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status erfolgreich abgerufen"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User can only access their own data"),
            @ApiResponse(responseCode = "404", description = "User nicht gefunden")
    })
    public ResponseEntity<FastStatusResponse> statusByUser(@PathVariable String identifier) {
        // Get authenticated user from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String authenticatedUsername = authentication.getName();
        
        // Check if authenticated user matches requested identifier
        if (!authorizationService.userMatches(authenticatedUsername, identifier)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Optional<User> user = userService.getUserByIdentifier(identifier);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(service.getStatus(user.get()));
    }

    @GetMapping("/user/{identifier}/history")
    @Operation(summary = "Historie aller Fasten-Sessions für spezifischen User", 
               description = "Gibt eine Liste aller bisherigen Fasten-Sessions für einen spezifischen User zurück (über Username oder Email). Requires JWT authentication.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historie erfolgreich abgerufen"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User can only access their own data"),
            @ApiResponse(responseCode = "404", description = "User nicht gefunden")
    })
    public ResponseEntity<List<FastSession>> historyByUser(@PathVariable String identifier) {
        // Get authenticated user from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String authenticatedUsername = authentication.getName();
        
        // Check if authenticated user matches requested identifier
        if (!authorizationService.userMatches(authenticatedUsername, identifier)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Optional<User> user = userService.getUserByIdentifier(identifier);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(service.history(user.get()));
    }

    @PostMapping("/user/{identifier}/start")
    @Operation(summary = "Neue Fasten-Session für spezifischen User starten", 
               description = "Startet eine neue Fasten-Session für einen spezifischen User mit optionalem Ziel. Requires JWT authentication.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fasten-Session erfolgreich gestartet"),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabedaten oder User bereits aktiv"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User can only start their own sessions"),
            @ApiResponse(responseCode = "404", description = "User nicht gefunden")
    })
    public ResponseEntity<FastSession> startByUser(@PathVariable String identifier, 
                                                   @Valid @RequestBody(required = false) StartFastRequest request) {
        // Get authenticated user from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String authenticatedUsername = authentication.getName();
        
        // Check if authenticated user matches requested identifier
        if (!authorizationService.userMatches(authenticatedUsername, identifier)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Optional<User> user = userService.getUserByIdentifier(identifier);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        if (request == null) {
            request = new StartFastRequest(16); // Default 16 Stunden
        }
        
        try {
            FastSession session = service.start(user.get(), request);
            return ResponseEntity.ok(session);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/user/{identifier}/stop")
    @Operation(summary = "Aktive Fasten-Session für spezifischen User beenden", 
               description = "Beendet die aktuell aktive Fasten-Session für einen spezifischen User. Requires JWT authentication.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fasten-Session erfolgreich beendet"),
            @ApiResponse(responseCode = "400", description = "Keine aktive Fasten-Session vorhanden"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User can only stop their own sessions"),
            @ApiResponse(responseCode = "404", description = "User nicht gefunden")
    })
    public ResponseEntity<FastSession> stopByUser(@PathVariable String identifier) {
        // Get authenticated user from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String authenticatedUsername = authentication.getName();
        
        // Check if authenticated user matches requested identifier
        if (!authorizationService.userMatches(authenticatedUsername, identifier)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Optional<User> user = userService.getUserByIdentifier(identifier);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            FastSession session = service.stop(user.get());
            return ResponseEntity.ok(session);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleIllegalStateException(IllegalStateException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Conflict");
        error.put("message", e.getMessage());
        error.put("status", "409");
        return error;
    }
}
