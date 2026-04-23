package com.mysteryengine.controller;

import com.mysteryengine.model.Review;
import com.mysteryengine.model.User;
import com.mysteryengine.repository.ReviewRepository;
import com.mysteryengine.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ReviewController {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public ReviewController(ReviewRepository reviewRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/reviews")
    public Map<String, Object> submit(@RequestBody Map<String, String> request, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            throw new IllegalArgumentException("You must be logged in");
        }
        String mysteryId = request.get("mysteryId");
        String ratingRaw = request.get("rating");
        if (mysteryId == null || mysteryId.isBlank() || ratingRaw == null) {
            throw new IllegalArgumentException("mysteryId and rating are required");
        }
        int rating;
        try {
            rating = Integer.parseInt(ratingRaw);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("rating must be a number");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("rating must be between 1 and 5");
        }
        User user = userRepository.findById(userId).orElseThrow();
        Review review = new Review();
        review.setMysteryId(mysteryId);
        review.setUser(user);
        review.setRating(rating);
        review.setComment(request.getOrDefault("comment", ""));
        reviewRepository.save(review);
        return Map.of("status", "ok");
    }
}
