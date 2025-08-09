package com.larslab.fasting.service;

import com.larslab.fasting.model.FastSession;
import com.larslab.fasting.repo.FastRepository;
import org.springframework.stereotype.Service;
import java.time.Instant;
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
        return getActive().orElseGet(() -> repo.save(new FastSession(Instant.now())));
    }

    public FastSession stop() {
        FastSession active = getActive().orElseThrow(() -> new IllegalStateException("Kein aktives Fasten"));
        active.setEndAt(Instant.now());
        return repo.save(active);
    }

    public List<FastSession> history() {
        return repo.findAll();
    }
}
