package Classroom;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import Utils.Json;
import jakarta.persistence.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class LearningMaterial {
    private final String title;
    private final String content;
    @Id
    private final String uuid;
    private final boolean answerable;
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean approved;
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "assessment_item_uuid", referencedColumnName = "uuid")
    private AssessmentItem assessmentItem;

    @ElementCollection
    @CollectionTable(name = "learning_material_tags", joinColumns = @JoinColumn(name = "learning_material_uuid"))
    @Column(name = "tag")
    @JsonIgnore
    private List<String> tags = new ArrayList<>();

    protected LearningMaterial() {
        this.title = null;
        this.content = null;
        this.uuid = null;
        this.answerable = false;
        this.assessmentItem = null;
        this.approved = false;
        this.tags = new ArrayList<>();
    }

    public LearningMaterial(String title, String content, boolean answerable) {
        this.title = title;
        this.content = content;
        this.uuid = UUID.randomUUID().toString();
        this.answerable = answerable;
        this.assessmentItem = null;
        this.approved = false;
        this.tags = new ArrayList<>();
    }

    @JsonCreator
    public LearningMaterial(@JsonProperty("title") String title,
                            @JsonProperty("content") String content,
                            @JsonProperty("uuid") String uuid,
                            @JsonProperty("answerable") boolean answerable,
                            @JsonProperty("assessmentItem") AssessmentItem assessmentItem,
                            @JsonProperty("approved") boolean approved) {
        this.title = title;
        this.content = content;
        this.uuid = uuid;
        this.answerable = answerable;
        this.assessmentItem = assessmentItem;
        this.approved = approved;
        this.tags = new ArrayList<>();
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

    public boolean isApproved() {
        return approved;
    }

    public void approve() {
        this.approved = true;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
