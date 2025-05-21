package Classroom;

import Utils.Json;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AssessmentRecord(String uuid, int score,
                               @JsonProperty("studentAnswer") @JsonAlias("answer") String studentAnswer,
                               String studentId, String feedback) {

    @JsonCreator
    public AssessmentRecord(
            @JsonProperty("uuid") String uuid,
            @JsonProperty("score") int score,
            @JsonProperty(value = "studentAnswer", required = false) String studentAnswer,
            @JsonProperty(value = "answer", required = false) String answer,
            @JsonProperty("studentId") String studentId,
            @JsonProperty("feedback") String feedback) {
        this(uuid, score, studentAnswer != null ? studentAnswer : answer, studentId, feedback);
    }

    public AssessmentRecord(int score,
                            String answer,
                            String studentId,
                            String feedback) {
        this(UUID.randomUUID().toString(), score, answer, studentId, feedback);
    }

    public static AssessmentRecord loadAssessmentRecord(String path, UUID id) throws IOException {
        String filename = path + "AR_" + id + ".json";
        return Json.fromJsonFile(filename, AssessmentRecord.class);
    }

    public String getUuid() {
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