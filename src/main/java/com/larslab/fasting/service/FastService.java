package com.larslab.fasting.service;

import com.larslab.fasting.model.FastSession;
import com.larslab.fasting.repo.FastRepository;
import com.larslab.fasting.dto.StartFastRequest;
import com.larslab.fasting.dto.FastStatusResponse;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
public class FastService {
    private final FastRepository repo;

    public FastService(FastRepository repo) {
        this.repo = repo;
    }

    public Optional<FastSession> getActive() {
        return repo.findFirstByEndAtIsNullOrderByStartAtDesc();
    }

    public FastSession start() {
        return start(new StartFastRequest(16));
    }

    public FastSession start(StartFastRequest request) {
        Integer goalHours = request.getGoalHours();
        return getActive().orElseGet(() -> repo.save(new FastSession(Instant.now(), goalHours)));
    }

    public FastSession stop() {
        FastSession active = getActive().orElseThrow(() -> new IllegalStateException("Kein aktives Fasten"));
        active.setEndAt(Instant.now());
        return repo.save(active);
    }

    public FastStatusResponse getStatus() {
        Optional<FastSession> activeSession = getActive();
        
        if (activeSession.isEmpty()) {
            return new FastStatusResponse(false);
        }
        
        FastSession session = activeSession.get();
        Duration duration = Duration.between(session.getStartAt(), Instant.now());
        
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        String since = session.getStartAt().toString();
        Integer goalHours = session.getGoalHours();
        
        return new FastStatusResponse(true, (int) hours, (int) minutes, since, goalHours);
    }

    public List<FastSession> history() {
        return repo.findAll();
    }
}
