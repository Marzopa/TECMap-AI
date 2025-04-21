package Classroom;

import Utils.Json;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class AssessmentItem {
    private final int maxScore;
    private final List<AssessmentRecord> submissions;
    private final UUID uuid;

    public AssessmentItem(int maxScore) {
        this.maxScore = maxScore;
        this.submissions = new LinkedList<>();
        this.uuid = UUID.randomUUID();
    }

    @JsonCreator
    public AssessmentItem(@JsonProperty("maxScore") int maxScore,
                          @JsonProperty("uuid") UUID uuid,
                          @JsonProperty("submissions") List<AssessmentRecord> submissions) {
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

    public void submitSolution(AssessmentRecord record) {
        if (record != null) {
            submissions.add(record);
        } else {
            throw new IllegalArgumentException("Cannot add null submission.");
        }
    }

    public void submitSolution(int score, String answer, String studentId, String feedback) {
        if (studentId != null && answer != null) {
            AssessmentRecord record = new AssessmentRecord(score, answer, studentId, feedback);
            submissions.add(record);
        } else {
            throw new IllegalArgumentException("Student ID and studentAnswer cannot be null.");
        }

    }
}