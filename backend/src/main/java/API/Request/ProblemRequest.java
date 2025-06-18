package API.Request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProblemRequest(
        String topic,
        int difficulty,
        String[] additionalTopics,
        String[] excludedTopics,
        int studentId,
        Method method,
        boolean save
) {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public enum Method {
        DEFAULT("default"),
        CHASE("chase"),
        OPENAI("openai");

        private final String value;

        Method(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public static Method fromValue(String value) {
            for (Method method : Method.values()) if (method.value.equalsIgnoreCase(value)) return method;
            return DEFAULT;
        }
    }

    @JsonCreator
    public ProblemRequest(
            @JsonProperty("topic") String topic,
            @JsonProperty("difficulty") int difficulty,
            @JsonProperty("additionalTopics") String[] additionalTopics,
            @JsonProperty("excludedTopics") String[] excludedTopics,
            @JsonProperty("studentId") Integer studentId, // nullable input
            @JsonProperty("method") Method method,
            @JsonProperty(value = "save", defaultValue = "true") boolean save
    ) {
        this(topic, difficulty, additionalTopics, excludedTopics,
                studentId != null ? studentId : 0,
                method != null ? method : Method.DEFAULT, save);
    }
}
