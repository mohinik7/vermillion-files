package com.mysteryengine.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "session_scenes")
public class SessionScene {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "session_id")
    private GameSession session;

    @Column(name = "scene_id", nullable = false)
    private String sceneId;

    @Column(name = "choice_made")
    private String choiceMade;

    @Column(name = "is_dead_end")
    private boolean deadEnd;

    @Column(name = "visited_at", nullable = false)
    private LocalDateTime visitedAt;

    @PrePersist
    public void prePersist() {
        this.visitedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public GameSession getSession() { return session; }
    public void setSession(GameSession session) { this.session = session; }
    public String getSceneId() { return sceneId; }
    public void setSceneId(String sceneId) { this.sceneId = sceneId; }
    public String getChoiceMade() { return choiceMade; }
    public void setChoiceMade(String choiceMade) { this.choiceMade = choiceMade; }
    public boolean isDeadEnd() { return deadEnd; }
    public void setDeadEnd(boolean deadEnd) { this.deadEnd = deadEnd; }
    public LocalDateTime getVisitedAt() { return visitedAt; }
    public void setVisitedAt(LocalDateTime visitedAt) { this.visitedAt = visitedAt; }
}
