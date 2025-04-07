package Classroom;

import Utils.Json;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.UUID;

public record AssessmentRecord(UUID uuid, int score, String answer, String studentId, String feedback, AssessmentItem assessmentItem) {

    @JsonCreator
    public AssessmentRecord(@JsonProperty("uuid") UUID uuid,
                            @JsonProperty("score") int score,
                            @JsonProperty("answer") String answer,
                            @JsonProperty("studentId") String studentId,
                            @JsonProperty("feedback") String feedback,
                            AssessmentItem assessmentItem) {
        this.uuid = uuid;
        this.score = score;
        this.answer = answer;
        this.studentId = studentId;
        this.feedback = feedback;
        this.assessmentItem = assessmentItem;
    }

    public AssessmentRecord(int score,
                            String answer,
                            String studentId,
                            String feedback,
                            AssessmentItem assessmentItem) {
        this(UUID.randomUUID(), score, answer, studentId, feedback, assessmentItem);
    }

    public static AssessmentRecord loadAssessmentRecord(AssessmentItem material, String path, UUID id) throws IOException {
        String filename = path + "AssessmentRecord_" + id + ".json";
        return Json.fromJsonFile(filename, AssessmentItem.class);
    }
}