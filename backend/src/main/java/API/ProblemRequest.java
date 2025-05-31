package API;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProblemRequest(String topic, int difficulty, String[] additionalTopics, String[] excludedTopics) {
    @JsonCreator
    public ProblemRequest(@JsonProperty("topic") String topic,
                          @JsonProperty("difficulty") int difficulty,
                          @JsonProperty("additionalTopics") String[] additionalTopics,
                          @JsonProperty("excludedTopics") String[] excludedTopics) {
        this.topic = topic;
        this.difficulty = difficulty;
        this.additionalTopics = additionalTopics;
        this.excludedTopics = excludedTopics;
    }

    public String getTopic() {
        return topic;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public String[] getAdditionalTopics() {
        return additionalTopics;
    }

    public String[] getExcludedTopics() {
        return excludedTopics;
    }
}
