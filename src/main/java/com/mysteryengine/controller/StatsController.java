package com.mysteryengine.controller;

import com.mysteryengine.model.GameSession;
import com.mysteryengine.model.Review;
import com.mysteryengine.repository.GameSessionRepository;
import com.mysteryengine.repository.LeaderboardEntryRepository;
import com.mysteryengine.repository.ReviewRepository;
import com.mysteryengine.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StatsController {
    private final LeaderboardEntryRepository leaderboardEntryRepository;
    private final GameSessionRepository gameSessionRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public StatsController(LeaderboardEntryRepository leaderboardEntryRepository,
                           GameSessionRepository gameSessionRepository,
                           ReviewRepository reviewRepository,
                           UserRepository userRepository) {
        this.leaderboardEntryRepository = leaderboardEntryRepository;
        this.gameSessionRepository = gameSessionRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/leaderboard/{mysteryId}")
    public List<Map<String, Object>> leaderboard(@PathVariable String mysteryId) {
        return leaderboardEntryRepository.findTop10ByMysteryIdOrderByScoreDesc(mysteryId).stream()
                .map(e -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("username", e.getUser().getUsername());
                    row.put("score", e.getScore());
                    row.put("wrongBranches", e.getWrongBranches());
                    return row;
                })
                .toList();
    }

    @GetMapping("/players/{id}/stats")
    public Map<String, Object> playerStats(@PathVariable Long id) {
        List<GameSession> sessions = gameSessionRepository.findByUserIdOrderByStartedAtDesc(id);
        List<Review> reviews = reviewRepository.findByUserId(id);
        long solvedCount = sessions.stream().filter(s -> "SOLVED".equals(s.getStatus())).count();
        double avgScore = sessions.stream().filter(s -> s.getScore() != null).mapToInt(GameSession::getScore).average().orElse(0);
        String joinedAt = userRepository.findById(id)
                .map(user -> user.getCreatedAt().toLocalDate().toString())
                .orElse(null);
        return Map.of(
                "sessions", sessions.size(),
                "solved", solvedCount,
                "avgScore", Math.round(avgScore),
                "reviews", reviews.size(),
                "createdAt", joinedAt
        );
    }
}
