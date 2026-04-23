package com.mysteryengine.service;

import com.mysteryengine.model.*;
import com.mysteryengine.repository.*;
import com.mysteryengine.xml.ChoiceNode;
import com.mysteryengine.xml.MysteryNode;
import com.mysteryengine.xml.SceneNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class GameService {
    private final GameSessionRepository sessionRepository;
    private final SessionSceneRepository sessionSceneRepository;
    private final SessionClueRepository sessionClueRepository;
    private final SuspectTagRepository suspectTagRepository;
    private final LeaderboardEntryRepository leaderboardEntryRepository;
    private final StoryLoaderService storyLoaderService;

    public GameService(GameSessionRepository sessionRepository, SessionSceneRepository sessionSceneRepository,
                       SessionClueRepository sessionClueRepository, SuspectTagRepository suspectTagRepository,
                       LeaderboardEntryRepository leaderboardEntryRepository, StoryLoaderService storyLoaderService) {
        this.sessionRepository = sessionRepository;
        this.sessionSceneRepository = sessionSceneRepository;
        this.sessionClueRepository = sessionClueRepository;
        this.suspectTagRepository = suspectTagRepository;
        this.leaderboardEntryRepository = leaderboardEntryRepository;
        this.storyLoaderService = storyLoaderService;
    }

    @Transactional
    public GameSession startSession(User user, String mysteryId) {
        GameSession session = new GameSession();
        session.setUser(user);
        session.setMysteryId(mysteryId);
        session = sessionRepository.save(session);
        MysteryNode mystery = getMystery(mysteryId);
        logScene(session, mystery.getStartScene(), null, false);
        return session;
    }

    public MysteryNode getMystery(String mysteryId) {
        MysteryNode mystery = storyLoaderService.getMysteryCache().get(mysteryId);
        if (mystery == null) {
            throw new IllegalArgumentException("Mystery not found");
        }
        return mystery;
    }

    public SceneNode getCurrentScene(Long sessionId) {
        GameSession session = sessionRepository.findById(sessionId).orElseThrow();
        List<SessionScene> logs = sessionSceneRepository.findBySessionIdOrderByVisitedAtAsc(sessionId);
        if (logs.isEmpty()) {
            throw new IllegalArgumentException("Session has no scene history");
        }
        String currentSceneId = logs.get(logs.size() - 1).getSceneId();
        return getMystery(session.getMysteryId()).getScenes().get(currentSceneId);
    }

    public List<SessionScene> getSessionScenes(Long sessionId) {
        return sessionSceneRepository.findBySessionIdOrderByVisitedAtAsc(sessionId);
    }

    @Transactional
    public SceneNode applyChoice(Long sessionId, String choiceId) {
        GameSession session = sessionRepository.findById(sessionId).orElseThrow();
        SceneNode current = getCurrentScene(sessionId);
        ChoiceNode choice = current.getChoices().stream()
                .filter(c -> c.getId().equals(choiceId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Choice not found"));
        if (choice.getRequiresClue() != null && !choice.getRequiresClue().isBlank()) {
            sessionClueRepository.findBySessionIdAndClueId(sessionId, choice.getRequiresClue())
                    .orElseThrow(() -> new IllegalArgumentException("Required clue not found"));
        }
        SceneNode next = getMystery(session.getMysteryId()).getScenes().get(choice.getLeadsTo());
        if (next == null) {
            throw new IllegalArgumentException("Next scene not found in mystery");
        }
        logScene(session, next.getId(), choiceId, choice.isDeadEnd());
        if (next.isEnding()) {
            finalizeSession(session, next.getEndingType());
        }
        return next;
    }

    @Transactional
    public List<Map<String, Object>> revealSceneClues(Long sessionId, String sceneId) {
        GameSession session = sessionRepository.findById(sessionId).orElseThrow();
        MysteryNode mystery = getMystery(session.getMysteryId());
        SceneNode scene = mystery.getScenes().get(sceneId);
        if (scene == null) {
            throw new IllegalArgumentException("Scene not found");
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (String clueId : scene.getClueIds()) {
            sessionClueRepository.findBySessionIdAndClueId(sessionId, clueId).orElseGet(() -> {
                SessionClue sc = new SessionClue();
                sc.setSession(session);
                sc.setClueId(clueId);
                return sessionClueRepository.save(sc);
            });
            Map<String, Object> clueMap = new HashMap<>();
            clueMap.put("id", clueId);
            clueMap.put("type", mystery.getClues().get(clueId).getType());
            clueMap.put("description", mystery.getClues().get(clueId).getDescription());
            result.add(clueMap);
        }
        return result;
    }

    @Transactional
    public void tagSuspect(Long sessionId, String suspectId, String tag) {
        GameSession session = sessionRepository.findById(sessionId).orElseThrow();
        SuspectTag suspectTag = suspectTagRepository.findBySessionIdAndSuspectId(sessionId, suspectId)
                .orElseGet(SuspectTag::new);
        suspectTag.setSession(session);
        suspectTag.setSuspectId(suspectId);
        suspectTag.setTag(tag);
        suspectTagRepository.save(suspectTag);
    }

    public Map<String, Object> resolution(Long sessionId) {
        GameSession session = sessionRepository.findById(sessionId).orElseThrow();
        MysteryNode mystery = getMystery(session.getMysteryId());
        long wrongBranches = sessionSceneRepository.countBySessionIdAndDeadEndTrue(sessionId);
        long cluesFound = sessionClueRepository.countBySessionId(sessionId);
        Map<String, Object> map = new HashMap<>();
        map.put("status", session.getStatus());
        map.put("score", session.getScore());
        map.put("wrongBranches", wrongBranches);
        map.put("cluesFound", cluesFound);
        map.put("totalClues", mystery.getClues().size());
        map.put("scenesVisited", sessionSceneRepository.findBySessionIdOrderByVisitedAtAsc(sessionId).size());
        return map;
    }

    public String buildCaseFile(Long sessionId) {
        GameSession session = sessionRepository.findById(sessionId).orElseThrow();
        List<SessionScene> logs = sessionSceneRepository.findBySessionIdOrderByVisitedAtAsc(sessionId);
        StringBuilder xml = new StringBuilder();
        xml.append("<case-file session-id=\"").append(sessionId).append("\">");
        xml.append("<detective>").append(session.getUser().getUsername()).append("</detective>");
        xml.append("<mystery-id>").append(session.getMysteryId()).append("</mystery-id>");
        xml.append("<score>").append(Optional.ofNullable(session.getScore()).orElse(0)).append("</score>");
        xml.append("<verdict>").append(session.getStatus()).append("</verdict><timeline>");
        for (SessionScene log : logs) {
            xml.append("<step scene-id=\"").append(log.getSceneId()).append("\" choice=\"")
                    .append(Optional.ofNullable(log.getChoiceMade()).orElse("START")).append("\"/>");
        }
        xml.append("</timeline></case-file>");
        return xml.toString();
    }

    private void logScene(GameSession session, String sceneId, String choiceId, boolean deadEnd) {
        SessionScene log = new SessionScene();
        log.setSession(session);
        log.setSceneId(sceneId);
        log.setChoiceMade(choiceId);
        log.setDeadEnd(deadEnd);
        sessionSceneRepository.save(log);
    }

    private void finalizeSession(GameSession session, String endingType) {
        long wrongBranches = sessionSceneRepository.countBySessionIdAndDeadEndTrue(session.getId());
        long hintsUsed = 0;
        long minutes = Math.max(1, Duration.between(session.getStartedAt(), LocalDateTime.now()).toMinutes());
        int timeBonus = (int) Math.max(0, 300 - minutes * 10);
        int score = (int) (1000 - (50 * wrongBranches) - (80 * hintsUsed) + timeBonus);
        session.setScore(score);
        session.setStatus("SOLVED".equalsIgnoreCase(endingType) ? "SOLVED" : "COLD_CASE");
        session.setEndedAt(LocalDateTime.now());
        sessionRepository.save(session);

        LeaderboardEntry entry = new LeaderboardEntry();
        entry.setMysteryId(session.getMysteryId());
        entry.setUser(session.getUser());
        entry.setScore(score);
        entry.setWrongBranches((int) wrongBranches);
        leaderboardEntryRepository.save(entry);
    }
}
