package Classroom;

import Utils.Json;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.UUID;

public record AssessmentRecord(UUID uuid, int score, String studentAnswer, String studentId, String feedback) {

    @JsonCreator
    public AssessmentRecord(@JsonProperty("uuid") UUID uuid,
                            @JsonProperty("score") int score,
                            @JsonProperty("studentAnswer") String studentAnswer,
                            @JsonProperty("studentId") String studentId,
                            @JsonProperty("feedback") String feedback) {
        this.uuid = uuid;
        this.score = score;
        this.studentAnswer = studentAnswer;
        this.studentId = studentId;
        this.feedback = feedback;
    }

    public AssessmentRecord(int score,
                            String answer,
                            String studentId,
                            String feedback) {
        this(UUID.randomUUID(), score, answer, studentId, feedback);
    }

    public static AssessmentRecord loadAssessmentRecord(String path, UUID id) throws IOException {
        String filename = path + "AR_" + id + ".json";
        return Json.fromJsonFile(filename, AssessmentRecord.class);
    }

    public UUID getUuid() {
        return uuid;
    }
    public int getScore() {
        return score;
    }
    public String getAnswer() {
        return studentAnswer;
    }
    public String getStudentId() {
        return studentId;
    }
    public String getFeedback() {
        return feedback;
    }

}