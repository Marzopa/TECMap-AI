package Classroom;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.UUID;
import Utils.Json;

public class LearningMaterial {
    private final String title;
    private final String content;
    private final UUID uuid;
    private final boolean answerable;

    public LearningMaterial(String title, String content, boolean answerable) {
        this.title = title;
        this.content = content;
        this.uuid = UUID.randomUUID();
        this.answerable = answerable;
    }

    public LearningMaterial(String title) {
        this.title = title;
        this.content = "";
        this.uuid = UUID.randomUUID();
        this.answerable = false;
    }

    @JsonCreator
    public LearningMaterial(@JsonProperty("title") String title,
                            @JsonProperty("content") String content,
                            @JsonProperty("uuid") UUID uuid,
                            @JsonProperty("answerable") boolean answerable) {
        this.title = title;
        this.content = content;
        this.uuid = uuid;
        this.answerable = answerable;
    }

    /**
     * Saves the learning material to a file.
     *
     * @return The name to the saved file.
     */
    public String saveToFile(String path) throws IOException {
        String filename = path + "LM_" + uuid + ".json";
        Json.toJsonFile(filename, this);
        return filename;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getContent() {
        return content;
    }

    public String getTitle() {
        return title;
    }

    public boolean isAnswerable() {
        return answerable;
    }
}
