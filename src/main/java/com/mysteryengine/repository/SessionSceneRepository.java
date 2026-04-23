package com.mysteryengine.repository;

import com.mysteryengine.model.SessionScene;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionSceneRepository extends JpaRepository<SessionScene, Long> {
    List<SessionScene> findBySessionIdOrderByVisitedAtAsc(Long sessionId);
    long countBySessionIdAndDeadEndTrue(Long sessionId);
}
