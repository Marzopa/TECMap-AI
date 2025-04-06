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

    public LearningMaterial(String title, String content) {
        this.title = title;
        this.content = content;
        this.uuid = UUID.randomUUID();
    }

    public LearningMaterial(String title) {
        this.title = title;
        this.content = "";
        this.uuid = UUID.randomUUID();
    }

    @JsonCreator
    public LearningMaterial(@JsonProperty("title") String title,
                            @JsonProperty("content") String content,
                            @JsonProperty("uuid") UUID uuid) {
        this.title = title;
        this.content = content;
        this.uuid = uuid;
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
}
