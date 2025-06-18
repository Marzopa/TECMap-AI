package API.Request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApproveRequest(String username, String password, String problemId) {
    @JsonCreator
    public ApproveRequest(@JsonProperty("username") String username,
                        @JsonProperty("password") String password,
                        @JsonProperty("problemId") String problemId) {
        this.username = username;
        this.password = password;
        this.problemId = problemId;
    }
}