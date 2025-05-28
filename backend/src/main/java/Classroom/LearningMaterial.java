package Classroom;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import Utils.Json;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.io.IOException;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class LearningMaterial {
    private final String title;
    private final String content;
    @Id
    private final String uuid;
    private final boolean answerable;
    private AssessmentItem assessmentItem;

    protected LearningMaterial() {
        this.title = null;
        this.content = null;
        this.uuid = null;
        this.answerable = false;
        this.assessmentItem = null;
    }

    public LearningMaterial(String title, String content, boolean answerable) {
        this.title = title;
        this.content = content;
        this.uuid = UUID.randomUUID().toString();
        this.answerable = answerable;
        this.assessmentItem = null;
    }

    @JsonCreator
    public LearningMaterial(@JsonProperty("title") String title,
                            @JsonProperty("content") String content,
                            @JsonProperty("uuid") String uuid,
                            @JsonProperty("answerable") boolean answerable,
                            @JsonProperty("assessmentItem") AssessmentItem assessmentItem) {
        this.title = title;
        this.content = content;
        this.uuid = uuid;
        this.answerable = answerable;
        this.assessmentItem = assessmentItem;
    }

    public String saveToFile(String path) throws IOException {
        String filename = path + "LM_" + uuid + ".json";
        Json.toJsonFile(filename, this);
        return filename;
    }

    public void setAssessmentItem(AssessmentItem item) {
        if (item != null && this.answerable) {
            this.assessmentItem = item;
        } else {
            throw new IllegalArgumentException("Cannot add assessment item to non-answerable learning material.");
        }
    }

    public String getUuid() {
        return uuid;
    }

    public String getContent() {
        return content;
    }

    public String getTitle() {
        return title;
    }

    public boolean isAnswerable() {
        return answerable;
    }

    public AssessmentItem getAssessmentItem() {
        return assessmentItem;
    }
}