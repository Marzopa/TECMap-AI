package API.Request;

import Classroom.LearningMaterial;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SubmissionRequest(LearningMaterial learningMaterial, String solution, Integer studentId) {
    @JsonCreator
    public SubmissionRequest(@JsonProperty("learningMaterial") LearningMaterial learningMaterial,
                             @JsonProperty("solution") String solution,
                             @JsonProperty("studentId") Integer studentId) {
        this.learningMaterial = learningMaterial;
        this.solution = solution;
        this.studentId = studentId;
    }

    public String getProblem() {
        return learningMaterial.getContent();
    }
}