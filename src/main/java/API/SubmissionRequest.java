// File: src/main/java/API/SubmissionRequest.java
package API;

import Classroom.LearningMaterial;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SubmissionRequest {
    private final LearningMaterial learningMaterial;
    private final String solution;
    private final String language;
    private final String studentId;

    @JsonCreator
    public SubmissionRequest(@JsonProperty("learningMaterial") LearningMaterial learningMaterial,
                             @JsonProperty("solution") String solution,
                             @JsonProperty("language") String language,
                             @JsonProperty("studentId") String studentId) {
        this.learningMaterial = learningMaterial;
        this.solution = solution;
        this.language = language;
        this.studentId = studentId;
    }

    public LearningMaterial getLearningMaterial() {
        return learningMaterial;
    }

    public String getProblem() {
        return learningMaterial.getContent();
    }

    public String getSolution() {
        return solution;
    }

    public String getLanguage() {
        return language;
    }

    public String getStudentId() {
        return studentId;
    }
}