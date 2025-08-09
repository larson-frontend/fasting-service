package com.larslab.fasting;

import com.larslab.fasting.model.FastSession;
import com.larslab.fasting.repo.FastRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.*;

@SpringBootApplication
public class FastingApplication {
    public static void main(String[] args) {
        SpringApplication.run(FastingApplication.class, args);
    }

    @Bean
    CommandLineRunner seed(FastRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                Instant now = Instant.now();
                FastSession done = new FastSession(now.minus(Duration.ofHours(24)), now.minus(Duration.ofHours(6)));
                FastSession active = new FastSession(now.minus(Duration.ofHours(4)), null);
                repo.save(done);
                repo.save(active);
            }
        };
    }
}
