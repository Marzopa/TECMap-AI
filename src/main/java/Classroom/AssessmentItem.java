package Classroom;

import Utils.Json;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class AssessmentItem {
    private final LearningMaterial material;
    private final String question;
    private final int max_score;
    private final List<AssessmentRecord> submissions;
    private final UUID uuid;

    public AssessmentItem(LearningMaterial material, String question, int max_score) {
        this.material = material;
        this.question = question;
        this.max_score = max_score;
        this.submissions = new LinkedList<>();
        this.uuid = UUID.randomUUID();
    }

    public AssessmentItem(LearningMaterial material, int max_score) {
        if(material.isAnswerable()) throw new IllegalArgumentException("Answerable materials");
        this.material = material;
        this.question = "";
        this.max_score = max_score;
        this.submissions = new LinkedList<>();
        this.uuid = UUID.randomUUID();
    }

    @JsonCreator
    public AssessmentItem(@JsonProperty ("question") String question,
                          @JsonProperty ("max_score") int max_score,
                          @JsonProperty ("uuid") UUID uuid,
                          @JsonProperty ("submissions") List<UUID> submissions,
                          LearningMaterial learningMaterial,
                          String path) throws IOException {

        this.question = question;
        this.material = learningMaterial;
        this.max_score = max_score;
        this.submissions = new LinkedList<>();
        for(UUID record : submissions) {
            this.submissions.add(Json.fromJsonFile(path + "AssessmentRecord_" + record + ".json", AssessmentRecord.class));
        }
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getMax_score() {
        return max_score;
    }

    public String getQuestion() {
        return question;
    }

    public LearningMaterial getMaterial() {
        return material;
    }

    public static AssessmentItem loadAssessmentItem(LearningMaterial material, String path, UUID id) throws IOException {
        String filename = path + "AssessmentItem_" + id + ".json";
        return Json.fromJsonFile(filename, AssessmentItem.class);
    }
}
