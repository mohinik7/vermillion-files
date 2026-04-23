package com.mysteryengine.xml;

public class ChoiceNode {
    private String id;
    private String text;
    private String leadsTo;
    private String requiresClue;
    private boolean deadEnd;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getLeadsTo() { return leadsTo; }
    public void setLeadsTo(String leadsTo) { this.leadsTo = leadsTo; }
    public String getRequiresClue() { return requiresClue; }
    public void setRequiresClue(String requiresClue) { this.requiresClue = requiresClue; }
    public boolean isDeadEnd() { return deadEnd; }
    public void setDeadEnd(boolean deadEnd) { this.deadEnd = deadEnd; }
}
