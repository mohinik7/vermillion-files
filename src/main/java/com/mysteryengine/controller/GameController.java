package com.mysteryengine.controller;

import com.mysteryengine.model.GameSession;
import com.mysteryengine.model.SessionClue;
import com.mysteryengine.repository.GameSessionRepository;
import com.mysteryengine.repository.SessionClueRepository;
import com.mysteryengine.repository.SuspectTagRepository;
import com.mysteryengine.service.GameService;
import com.mysteryengine.xml.ChoiceNode;
import com.mysteryengine.xml.MysteryNode;
import com.mysteryengine.xml.SceneNode;
import com.mysteryengine.xml.SuspectNode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class GameController {
    private final GameService gameService;
    private final GameSessionRepository sessionRepository;
    private final SessionClueRepository sessionClueRepository;
    private final SuspectTagRepository suspectTagRepository;

    public GameController(GameService gameService, GameSessionRepository sessionRepository,
                          SessionClueRepository sessionClueRepository, SuspectTagRepository suspectTagRepository) {
        this.gameService = gameService;
        this.sessionRepository = sessionRepository;
        this.sessionClueRepository = sessionClueRepository;
        this.suspectTagRepository = suspectTagRepository;
    }

    @GetMapping("/game/scene")
    public Map<String, Object> currentScene(@RequestParam Long sessionId) {
        GameSession session = sessionRepository.findById(sessionId).orElseThrow();
        MysteryNode mystery = gameService.getMystery(session.getMysteryId());
        SceneNode scene = gameService.getCurrentScene(sessionId);
        Set<String> collected = sessionClueRepository.findBySessionIdOrderByRevealedAtAsc(sessionId)
                .stream().map(SessionClue::getClueId).collect(Collectors.toSet());
        List<Map<String, Object>> choices = scene.getChoices().stream().map(c -> mapChoice(c, collected)).toList();
        Set<String> visitedScenes = gameService.getSessionScenes(sessionId).stream()
                .map(ss -> ss.getSceneId())
                .collect(Collectors.toSet());
        List<Map<String, Object>> suspects = mystery.getSuspects().values().stream()
                .filter(s -> s.getUnlockAtScene() == null || s.getUnlockAtScene().isBlank() || visitedScenes.contains(s.getUnlockAtScene()))
                .map(s -> mapSuspect(sessionId, s)).toList();
        return Map.of(
                "sceneId", scene.getId(),
                "title", scene.getTitle(),
                "narrative", scene.getNarrative(),
                "atmosphere", scene.getAtmosphere(),
                "ending", scene.isEnding(),
                "endingType", scene.getEndingType() == null ? "" : scene.getEndingType(),
                "choices", choices,
                "suspects", suspects
        );
    }

    @PostMapping("/game/choice")
    public Map<String, Object> choose(@RequestBody Map<String, String> request) {
        String sessionIdRaw = request.get("sessionId");
        String choiceId = request.get("choiceId");
        if (sessionIdRaw == null || choiceId == null || choiceId.isBlank()) {
            throw new IllegalArgumentException("sessionId and choiceId are required");
        }
        Long sessionId = Long.valueOf(sessionIdRaw);
        SceneNode next = gameService.applyChoice(sessionId, choiceId);
        return Map.of("sceneId", next.getId(), "ending", next.isEnding());
    }

    @GetMapping("/game/clues")
    public List<Map<String, Object>> clues(@RequestParam Long sessionId, @RequestParam String sceneId) {
        return gameService.revealSceneClues(sessionId, sceneId);
    }

    @PostMapping("/game/suspect-tag")
    public Map<String, Object> tagSuspect(@RequestBody Map<String, String> request) {
        String sessionIdRaw = request.get("sessionId");
        String suspectId = request.get("suspectId");
        String tag = request.get("tag");
        if (sessionIdRaw == null || suspectId == null || tag == null || tag.isBlank()) {
            throw new IllegalArgumentException("sessionId, suspectId, and tag are required");
        }
        gameService.tagSuspect(Long.valueOf(sessionIdRaw), suspectId, tag);
        return Map.of("status", "ok");
    }

    @GetMapping("/sessions/{id}/resolution")
    public Map<String, Object> resolution(@PathVariable Long id) {
        return gameService.resolution(id);
    }

    @GetMapping("/sessions/{id}/casefile")
    public ResponseEntity<String> caseFile(@PathVariable Long id) {
        String xml = gameService.buildCaseFile(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=casefile-" + id + ".xml")
                .contentType(MediaType.APPLICATION_XML)
                .body(xml);
    }

    private Map<String, Object> mapChoice(ChoiceNode c, Set<String> collectedClues) {
        boolean enabled = c.getRequiresClue() == null || c.getRequiresClue().isBlank() || collectedClues.contains(c.getRequiresClue());
        return Map.of("id", c.getId(), "text", c.getText(), "enabled", enabled);
    }

    private Map<String, Object> mapSuspect(Long sessionId, SuspectNode suspect) {
        String tag = suspectTagRepository.findBySessionIdAndSuspectId(sessionId, suspect.getId())
                .map(t -> t.getTag()).orElse("UNSET");
        return Map.of("id", suspect.getId(), "name", suspect.getName(), "alibi", suspect.getAlibi(), "facts", suspect.getFacts(), "tag", tag);
    }
}
