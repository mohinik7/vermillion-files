package com.mysteryengine.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "session_clues")
public class SessionClue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "session_id")
    private GameSession session;

    @Column(name = "clue_id", nullable = false)
    private String clueId;

    @Column(name = "revealed_at", nullable = false)
    private LocalDateTime revealedAt;

    @PrePersist
    public void prePersist() {
        this.revealedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public GameSession getSession() { return session; }
    public void setSession(GameSession session) { this.session = session; }
    public String getClueId() { return clueId; }
    public void setClueId(String clueId) { this.clueId = clueId; }
    public LocalDateTime getRevealedAt() { return revealedAt; }
    public void setRevealedAt(LocalDateTime revealedAt) { this.revealedAt = revealedAt; }
}
