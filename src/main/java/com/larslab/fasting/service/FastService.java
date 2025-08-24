package com.larslab.fasting.service;

import com.larslab.fasting.model.FastSession;
import com.larslab.fasting.model.User;
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
    
    public Optional<FastSession> getActive(User user) {
        return repo.findFirstByUserAndEndAtIsNullOrderByStartAtDesc(user);
    }

    public FastSession start() {
        return start(new StartFastRequest(16));
    }

    public FastSession start(StartFastRequest request) {
        Optional<FastSession> activeSession = getActive();
        if (activeSession.isPresent()) {
            throw new IllegalStateException("Es läuft bereits eine Fasten-Session. Stoppen Sie diese zuerst mit /api/fast/stop");
        }
        Integer goalHours = request.getGoalHours();
        return repo.save(new FastSession(Instant.now(), goalHours));
    }
    
    public FastSession start(User user, StartFastRequest request) {
        Optional<FastSession> activeSession = getActive(user);
        if (activeSession.isPresent()) {
            throw new IllegalStateException("Es läuft bereits eine Fasten-Session. Stoppen Sie diese zuerst mit /api/fast/stop");
        }
        Integer goalHours = request.getGoalHours();
        return repo.save(new FastSession(user, Instant.now(), goalHours));
    }

    public FastSession stop() {
        FastSession active = getActive().orElseThrow(() -> new IllegalStateException("Kein aktives Fasten"));
        active.setEndAt(Instant.now());
        return repo.save(active);
    }
    
    public FastSession stop(User user) {
        FastSession active = getActive(user).orElseThrow(() -> new IllegalStateException("Kein aktives Fasten"));
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
    
    public FastStatusResponse getStatus(User user) {
        Optional<FastSession> activeSession = getActive(user);
        
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
    
    public List<FastSession> history(User user) {
        return repo.findByUserOrderByStartAtDesc(user);
    }
}
