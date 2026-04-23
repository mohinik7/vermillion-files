package com.mysteryengine.repository;

import com.mysteryengine.model.LeaderboardEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaderboardEntryRepository extends JpaRepository<LeaderboardEntry, Long> {
    List<LeaderboardEntry> findTop10ByMysteryIdOrderByScoreDesc(String mysteryId);
}
