package com.mysteryengine.repository;

import com.mysteryengine.model.SessionClue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionClueRepository extends JpaRepository<SessionClue, Long> {
    List<SessionClue> findBySessionIdOrderByRevealedAtAsc(Long sessionId);
    Optional<SessionClue> findBySessionIdAndClueId(Long sessionId, String clueId);
    long countBySessionId(Long sessionId);
}
