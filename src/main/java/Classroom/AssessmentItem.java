package Classroom;

import Utils.Json;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class AssessmentItem {
    private final LearningMaterial material;
    private final String question;
    private final int maxScore;
    private final List<AssessmentRecord> submissions;
    private final UUID uuid;

    public AssessmentItem(LearningMaterial material, String question, int maxScore) {
        this.material = material;
        this.question = question;
        this.maxScore = maxScore;
        this.submissions = new LinkedList<>();
        this.uuid = UUID.randomUUID();
    }

    public AssessmentItem(LearningMaterial material, int maxScore) {
        this(material, "", maxScore);
    }

    @JsonCreator
    public AssessmentItem(@JsonProperty("question") String question,
                          @JsonProperty("maxScore") int maxScore,
                          @JsonProperty("uuid") UUID uuid,
                          @JsonProperty("submissions") List<AssessmentRecord> submissions,
                          @JsonProperty("material") LearningMaterial material) {
        this.question = question;
        this.maxScore = maxScore;
        this.uuid = uuid;
        this.submissions = submissions != null ? submissions : new LinkedList<>();
        this.material = material;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public String getQuestion() {
        return question;
    }

    public LearningMaterial getMaterial() {
        return material;
    }

    public List<AssessmentRecord> getSubmissions() {
        return submissions;
    }

    public void addSubmission(AssessmentRecord record) {
        submissions.add(record);
    }

    public static AssessmentItem loadAssessmentItem(LearningMaterial material, String path, UUID id) throws IOException {
        String filename = path + "AI_" + id + ".json";
        return Json.fromJsonFile(filename, AssessmentItem.class);
    }
}