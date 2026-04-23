package com.mysteryengine.service;

import com.mysteryengine.xml.*;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.HashMap;
import java.util.Map;

@Service
public class StoryLoaderService {

    private final Map<String, MysteryNode> mysteryCache = new HashMap<>();

    @PostConstruct
    public void loadStories() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath*:stories/*.xml");
            for (Resource resource : resources) {
                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(resource.getInputStream());
                Element root = document.getDocumentElement();
                MysteryNode mystery = new MysteryNode();
                mystery.setId(root.getAttribute("id"));
                mystery.setTitle(root.getAttribute("title"));
                mystery.setGenre(root.getAttribute("genre"));
                mystery.setDifficulty(root.getAttribute("difficulty"));
                mystery.setTeaser(root.getAttribute("teaser"));
                mystery.setStartScene(root.getAttribute("start-scene"));
                loadClues(root, mystery);
                loadSuspects(root, mystery);
                loadScenes(root, mystery);
                mysteryCache.put(mystery.getId(), mystery);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load story XML files", e);
        }
    }

    private void loadClues(Element root, MysteryNode mystery) {
        NodeList clues = root.getElementsByTagName("clue");
        for (int i = 0; i < clues.getLength(); i++) {
            Element el = (Element) clues.item(i);
            ClueNode clue = new ClueNode();
            clue.setId(el.getAttribute("id"));
            clue.setType(el.getAttribute("type"));
            clue.setDescription(el.getTextContent().trim());
            mystery.getClues().put(clue.getId(), clue);
        }
    }

    private void loadSuspects(Element root, MysteryNode mystery) {
        NodeList suspects = root.getElementsByTagName("suspect");
        for (int i = 0; i < suspects.getLength(); i++) {
            Element el = (Element) suspects.item(i);
            SuspectNode suspect = new SuspectNode();
            suspect.setId(el.getAttribute("id"));
            suspect.setName(el.getAttribute("name"));
            suspect.setAlibi(el.getAttribute("alibi"));
            suspect.setFacts(el.getTextContent().trim());
            suspect.setUnlockAtScene(el.getAttribute("unlock-at-scene"));
            mystery.getSuspects().put(suspect.getId(), suspect);
        }
    }

    private void loadScenes(Element root, MysteryNode mystery) {
        NodeList scenes = root.getElementsByTagName("scene");
        for (int i = 0; i < scenes.getLength(); i++) {
            Element el = (Element) scenes.item(i);
            SceneNode scene = new SceneNode();
            scene.setId(el.getAttribute("id"));
            scene.setTitle(el.getAttribute("title"));
            scene.setAtmosphere(el.getAttribute("atmosphere"));
            scene.setEndingType(el.getAttribute("ending"));
            NodeList narratives = el.getElementsByTagName("narrative");
            if (narratives.getLength() > 0) {
                scene.setNarrative(narratives.item(0).getTextContent().trim());
            }
            NodeList clueRefs = el.getElementsByTagName("clue-ref");
            for (int c = 0; c < clueRefs.getLength(); c++) {
                scene.getClueIds().add(((Element) clueRefs.item(c)).getAttribute("id"));
            }
            NodeList choices = el.getElementsByTagName("choice");
            for (int c = 0; c < choices.getLength(); c++) {
                Element cEl = (Element) choices.item(c);
                ChoiceNode choice = new ChoiceNode();
                choice.setId(cEl.getAttribute("id"));
                choice.setText(cEl.getTextContent().trim());
                choice.setLeadsTo(cEl.getAttribute("leads-to"));
                choice.setRequiresClue(cEl.getAttribute("requires-clue"));
                choice.setDeadEnd(Boolean.parseBoolean(cEl.getAttribute("dead-end")));
                scene.getChoices().add(choice);
            }
            mystery.getScenes().put(scene.getId(), scene);
        }
    }

    public Map<String, MysteryNode> getMysteryCache() {
        return mysteryCache;
    }
}
