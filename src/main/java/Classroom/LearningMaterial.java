package Classroom;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import Utils.Json;

public class LearningMaterial {
    private final String title;
    private final String content;
    private final UUID uuid;
    private final AssessmentItem assessmentItem;

    public LearningMaterial(String title, String content, boolean answerable) {
        this.title = title;
        this.content = content;
        this.uuid = UUID.randomUUID();
        this.assessmentItem = null;
    }

    public LearningMaterial(String title) {
        this.title = title;
        this.content = "";
        this.uuid = UUID.randomUUID();
        this.assessmentItem = null;
    }

    @JsonCreator
    public LearningMaterial(@JsonProperty("title") String title,
                            @JsonProperty("content") String content,
                            @JsonProperty("uuid") UUID uuid,
                            @JsonProperty("assessmentItems") UUID assessmentItem) {
        this.title = title;
        this.content = content;
        this.uuid = uuid;
        this.assessmentItem = null;
    }

    /**
     * Saves the learning material to a file.
     *
     * @return The name to the saved file.
     */
    public String saveToFile(String path) throws IOException {
        String filename = path + "LM_" + uuid + ".json";
        Json.toJsonFile(filename, this);
        return filename;
    }

    public void addAssessmentItem(AssessmentItem item) {
        if (item != null && this.answerable) {
            assessmentItems.add(item.getUuid());
        }
        else {
            throw new IllegalArgumentException("Cannot add assessment item to non-answerable learning material.");
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getContent() {
        return content;
    }

    public List<UUID> getAssessmentItems() {
        return assessmentItems;
    }

    public String getTitle() {
        return title;
    }

    public boolean isAnswerable() {
        return answerable;
    }
}
