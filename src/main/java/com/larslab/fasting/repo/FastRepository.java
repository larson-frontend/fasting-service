package com.larslab.fasting.repo;

import com.larslab.fasting.model.FastSession;
import com.larslab.fasting.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FastRepository extends JpaRepository<FastSession, Long> {
    Optional<FastSession> findFirstByEndAtIsNullOrderByStartAtDesc();
    
    Optional<FastSession> findFirstByUserAndEndAtIsNullOrderByStartAtDesc(User user);
    
    List<FastSession> findByUserOrderByStartAtDesc(User user);
}
