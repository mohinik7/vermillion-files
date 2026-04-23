package com.mysteryengine.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "leaderboard")
public class LeaderboardEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mystery_id", nullable = false)
    private String mysteryId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Integer score;

    @Column(name = "wrong_branches", nullable = false)
    private Integer wrongBranches;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    @PrePersist
    public void prePersist() { this.completedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMysteryId() { return mysteryId; }
    public void setMysteryId(String mysteryId) { this.mysteryId = mysteryId; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Integer getWrongBranches() { return wrongBranches; }
    public void setWrongBranches(Integer wrongBranches) { this.wrongBranches = wrongBranches; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
