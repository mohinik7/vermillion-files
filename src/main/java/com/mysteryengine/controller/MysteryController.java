package com.mysteryengine.controller;

import com.mysteryengine.model.GameSession;
import com.mysteryengine.model.Review;
import com.mysteryengine.model.User;
import com.mysteryengine.repository.ReviewRepository;
import com.mysteryengine.repository.UserRepository;
import com.mysteryengine.service.GameService;
import com.mysteryengine.service.StoryLoaderService;
import com.mysteryengine.xml.MysteryNode;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class MysteryController {
    private final StoryLoaderService storyLoaderService;
    private final ReviewRepository reviewRepository;
    private final GameService gameService;
    private final UserRepository userRepository;

    public MysteryController(StoryLoaderService storyLoaderService, ReviewRepository reviewRepository,
                             GameService gameService, UserRepository userRepository) {
        this.storyLoaderService = storyLoaderService;
        this.reviewRepository = reviewRepository;
        this.gameService = gameService;
        this.userRepository = userRepository;
    }

    @GetMapping("/mysteries")
    public List<Map<String, Object>> mysteries(@RequestParam(required = false) String genre,
                                               @RequestParam(required = false) String difficulty) {
        return storyLoaderService.getMysteryCache().values().stream()
                .filter(m -> genre == null || genre.equalsIgnoreCase(m.getGenre()))
                .filter(m -> difficulty == null || difficulty.equalsIgnoreCase(m.getDifficulty()))
                .map(this::toCard).collect(Collectors.toList());
    }

    @GetMapping("/mysteries/{id}")
    public Map<String, Object> mystery(@PathVariable String id) {
        return toCard(gameService.getMystery(id));
    }

    @PostMapping("/sessions")
    public Map<String, Object> startSession(@RequestBody Map<String, String> request, HttpSession httpSession) {
        Long userId = (Long) httpSession.getAttribute("userId");
        if (userId == null) {
            throw new IllegalArgumentException("You must be logged in");
        }
        String mysteryId = request.get("mysteryId");
        if (mysteryId == null || mysteryId.isBlank()) {
            throw new IllegalArgumentException("mysteryId is required");
        }
        User user = userRepository.findById(userId).orElseThrow();
        GameSession session = gameService.startSession(user, mysteryId);
        return Map.of("sessionId", session.getId(), "mysteryId", session.getMysteryId());
    }

    private Map<String, Object> toCard(MysteryNode mystery) {
        List<Review> reviews = reviewRepository.findByMysteryId(mystery.getId());
        double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0);
        Map<String, Object> card = new HashMap<>();
        card.put("id", mystery.getId());
        card.put("title", mystery.getTitle());
        card.put("genre", mystery.getGenre());
        card.put("difficulty", mystery.getDifficulty());
        card.put("teaser", mystery.getTeaser());
        card.put("averageRating", Math.round(avg * 10.0) / 10.0);
        return card;
    }
}
