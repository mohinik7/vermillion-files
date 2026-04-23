package com.mysteryengine.repository;

import com.mysteryengine.model.SuspectTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SuspectTagRepository extends JpaRepository<SuspectTag, Long> {
    Optional<SuspectTag> findBySessionIdAndSuspectId(Long sessionId, String suspectId);
    List<SuspectTag> findBySessionId(Long sessionId);
}
