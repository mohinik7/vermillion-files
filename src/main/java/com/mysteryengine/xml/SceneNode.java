package com.mysteryengine.xml;

import java.util.ArrayList;
import java.util.List;

public class SceneNode {
    private String id;
    private String title;
    private String narrative;
    private String atmosphere;
    private String endingType;
    private final List<ChoiceNode> choices = new ArrayList<>();
    private final List<String> clueIds = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getNarrative() { return narrative; }
    public void setNarrative(String narrative) { this.narrative = narrative; }
    public String getAtmosphere() { return atmosphere; }
    public void setAtmosphere(String atmosphere) { this.atmosphere = atmosphere; }
    public String getEndingType() { return endingType; }
    public void setEndingType(String endingType) { this.endingType = endingType; }
    public List<ChoiceNode> getChoices() { return choices; }
    public List<String> getClueIds() { return clueIds; }
    public boolean isEnding() { return endingType != null && !endingType.isBlank(); }
}
