package Classroom;

import java.util.UUID;

public class AssessmentItem {
    private final LearningMaterial material;
    private final String question;
    private final int max_score;
    private final UUID uuid;

    public AssessmentItem(LearningMaterial material, String question, int max_score) {
        this.material = material;
        this.question = question;
        this.max_score = max_score;
        this.uuid = UUID.randomUUID();
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

    public static AssessmentItem loadAssessmentItem(LearningMaterial material, String question, int max_score) {
        return new AssessmentItem(material, question, max_score);
    }
}
