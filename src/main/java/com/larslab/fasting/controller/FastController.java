package com.larslab.fasting.controller;

import com.larslab.fasting.model.FastSession;
import com.larslab.fasting.service.FastService;
import com.larslab.fasting.dto.StartFastRequest;
import com.larslab.fasting.dto.FastStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/api/fast")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:8000", "http://localhost:8080", "http://localhost:4200"})
@Tag(name = "Fasting", description = "API zur Verwaltung von Fasten-Sessions mit Ziel-System")
@Validated
public class FastController {
    private final FastService service;

    public FastController(FastService service) {
        this.service = service;
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
}
