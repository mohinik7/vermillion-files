package com.mysteryengine.xml;

import java.util.HashMap;
import java.util.Map;

public class MysteryNode {
    private String id;
    private String title;
    private String genre;
    private String difficulty;
    private String teaser;
    private String startScene;
    private final Map<String, SceneNode> scenes = new HashMap<>();
    private final Map<String, ClueNode> clues = new HashMap<>();
    private final Map<String, SuspectNode> suspects = new HashMap<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public String getTeaser() { return teaser; }
    public void setTeaser(String teaser) { this.teaser = teaser; }
    public String getStartScene() { return startScene; }
    public void setStartScene(String startScene) { this.startScene = startScene; }
    public Map<String, SceneNode> getScenes() { return scenes; }
    public Map<String, ClueNode> getClues() { return clues; }
    public Map<String, SuspectNode> getSuspects() { return suspects; }
}
