package com.larslab.fasting.controller;

import com.larslab.fasting.model.FastSession;
import com.larslab.fasting.service.FastService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;
import java.util.*;

@RestController
@RequestMapping("/api/fast")
@Tag(name = "Fasting", description = "API zur Verwaltung von Fasten-Sessions")
public class FastController {
    private final FastService service;
    public FastController(FastService service) { this.service = service; }

    @PostMapping("/start")
    @Operation(summary = "Neue Fasten-Session starten", description = "Startet eine neue Fasten-Session oder gibt die bereits aktive Session zurück")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fasten-Session erfolgreich gestartet oder bereits aktiv")
    })
    public FastSession start() { return service.start(); }

    @PostMapping("/stop")
    @Operation(summary = "Aktive Fasten-Session beenden", description = "Beendet die aktuell aktive Fasten-Session")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fasten-Session erfolgreich beendet"),
        @ApiResponse(responseCode = "400", description = "Keine aktive Fasten-Session vorhanden")
    })
    public FastSession stop() { return service.stop(); }

    @GetMapping("/status")
    @Operation(summary = "Status der aktuellen Fasten-Session", description = "Gibt den Status der aktuellen Fasten-Session zurück (aktiv/inaktiv mit Dauer)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status erfolgreich abgerufen")
    })
    public Map<String, Object> status() {
        Map<String, Object> m = new HashMap<>();
        service.getActive().ifPresentOrElse(active -> {
            m.put("active", true);
            Duration d = active.getDuration();
            m.put("hours", d.toHours());
            m.put("minutes", d.toMinutesPart());
            m.put("since", active.getStartAt());
        }, () -> m.put("active", false));
        return m;
    }

    @GetMapping("/history")
    @Operation(summary = "Historie aller Fasten-Sessions", description = "Gibt eine Liste aller bisherigen Fasten-Sessions zurück")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Historie erfolgreich abgerufen")
    })
    public List<FastSession> history() { return service.history(); }
}
