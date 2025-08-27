package com.larslab.fasting.repo;

import com.larslab.fasting.model.RefreshToken;
import com.larslab.fasting.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    long deleteByUserAndExpiresAtBefore(User user, Instant cutoff);
}
