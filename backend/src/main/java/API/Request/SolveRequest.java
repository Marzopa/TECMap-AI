package API.Request;

import Classroom.LearningMaterial;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SolveRequest(LearningMaterial learningMaterial, String language, int studentId) {
    @JsonCreator
    public SolveRequest(@JsonProperty("learningMaterial") LearningMaterial learningMaterial,
                             @JsonProperty("language") String language,
                             @JsonProperty("studentId") int studentId) {
        this.learningMaterial = learningMaterial;
        this.language = language;
        this.studentId = studentId;
    }

    public String getProblem() {
        return learningMaterial.getContent();
    }
}