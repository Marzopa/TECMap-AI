package Classroom;

import jakarta.persistence.*;

@Entity
@Table(name = "LEARNING_MATERIAL_TAGS")
public class LearningMaterialTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "LEARNING_MATERIAL_UUID", nullable = false)
    private String learningMaterialUuid;

    @Column(name = "TAG", nullable = false)
    private String tag;

    public LearningMaterialTag() {}

    public LearningMaterialTag(String learningMaterialUuid, String tag) {
        this.learningMaterialUuid = learningMaterialUuid;
        this.tag = tag;
    }

    public Long getId() {
        return id;
    }

    public String getLearningMaterialUuid() {
        return learningMaterialUuid;
    }

    public void setLearningMaterialUuid(String learningMaterialUuid) {
        this.learningMaterialUuid = learningMaterialUuid;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
