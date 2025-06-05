package API.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProblemRequest(
        String topic,
        int difficulty,
        String[] additionalTopics,
        String[] excludedTopics,
        int studentId
) {
    @JsonCreator
    public ProblemRequest(
            @JsonProperty("topic") String topic,
            @JsonProperty("difficulty") int difficulty,
            @JsonProperty("additionalTopics") String[] additionalTopics,
            @JsonProperty("excludedTopics") String[] excludedTopics,
            @JsonProperty("studentId") Integer studentId // nullable input
    ) {
        this(topic, difficulty, additionalTopics, excludedTopics, studentId != null ? studentId : 0);
    }
}
