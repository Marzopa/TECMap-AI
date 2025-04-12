package Classroom;

import Utils.Json;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class AssessmentItem {
    private final String question;
    private final int maxScore;
    private final List<AssessmentRecord> submissions;
    private final UUID uuid;

    public AssessmentItem(String question, int maxScore) {
        this.question = question;
        this.maxScore = maxScore;
        this.submissions = new LinkedList<>();
        this.uuid = UUID.randomUUID();
    }

    public AssessmentItem(int maxScore) {
        this("", maxScore);
    }

    @JsonCreator
    public AssessmentItem(@JsonProperty("question") String question,
                          @JsonProperty("maxScore") int maxScore,
                          @JsonProperty("uuid") UUID uuid,
                          @JsonProperty("submissions") List<AssessmentRecord> submissions) {
        this.question = question;
        this.maxScore = maxScore;
        this.uuid = uuid;
        this.submissions = submissions != null ? submissions : new LinkedList<>();
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

    public List<AssessmentRecord> getSubmissions() {
        return submissions;
    }

    public void addSubmission(AssessmentRecord record) {
        submissions.add(record);
    }

    public static AssessmentItem loadAssessmentItem(String path, UUID id) throws IOException {
        String filename = path + "AI_" + id + ".json";
        return Json.fromJsonFile(filename, AssessmentItem.class);
    }
}