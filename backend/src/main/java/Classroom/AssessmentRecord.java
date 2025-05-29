package Classroom;

import Ollama.GradingResponse;
import Utils.Json;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.UUID;

import Ollama.GradingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@JsonIgnoreProperties(ignoreUnknown = true)
@Embeddable
public record AssessmentRecord(@Column(name = "uuid")
                               String uuid,

                               @Enumerated(EnumType.STRING)
                               @Column(name = "score")
                               GradingStatus score,

                               @JsonProperty("studentAnswer")
                               @JsonAlias("answer")
                               @Column(name = "student_answer")
                               String studentAnswer,

                               @Column(name = "student_id")
                               int studentId,

                               @Column(name = "feedback")
                               String feedback){

    @JsonCreator
    public AssessmentRecord(
            @JsonProperty("uuid") String uuid,
            @JsonProperty("score") GradingStatus score,
            @JsonProperty(value = "studentAnswer") String studentAnswer,
            @JsonProperty(value = "answer") String answer,
            @JsonProperty("studentId") int studentId,
            @JsonProperty("feedback") String feedback) {
        this(uuid, score, studentAnswer != null ? studentAnswer : answer, studentId, feedback);
    }

    public AssessmentRecord(GradingStatus score,
                            String answer,
                            int studentId,
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

    public GradingStatus getScore() {
        return score;
    }

    public String getAnswer() {
        return studentAnswer;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getFeedback() {
        return feedback;
    }
}