package Classroom;

import Ollama.GradingStatus;
import Utils.Json;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class AssessmentItem {
    @Lob
    @Column(name = "submissions_json", columnDefinition = "TEXT")
    @Convert(converter = AssessmentRecordListConverter.class)
    private final List<AssessmentRecord> submissions;

    @Id
    private final String uuid;

    public AssessmentItem() {
        this.submissions = new LinkedList<>();
        this.uuid = UUID.randomUUID().toString();
    }

    @JsonCreator
    public AssessmentItem(@JsonProperty("uuid") String uuid,
                          @JsonProperty("submissions") List<AssessmentRecord> submissions) {
        this.uuid = uuid;
        this.submissions = submissions != null ? submissions : new LinkedList<>();
    }

    public String getUuid() {
        return uuid;
    }

    public List<AssessmentRecord> getSubmissions() {
        return submissions;
    }

    public void addSubmission(AssessmentRecord record) {
        submissions.add(record);
    }

    public static AssessmentItem loadAssessmentItem(String path, UUID id) throws IOException {
        String filename = path + "AI_" + id + ".json";
        return Json.fromJsonFile(filename, AssessmentItem.class);
    }

    public void submitSolution(AssessmentRecord record) {
        if (record != null && record.getStudentId() != 0) {
            submissions.add(record);
        }
        else {
            throw new IllegalArgumentException("Cannot add null submission. Student ID cannot be null/zero.");
        }
    }

    public void submitSolution(GradingStatus score, String answer, int studentId, String feedback) {
        if (answer != null && studentId != 0) {
            AssessmentRecord record = new AssessmentRecord(score, answer, studentId, feedback);
            submissions.add(record);
        } else {
            throw new IllegalArgumentException("Student ID and studentAnswer cannot be null/zero.");
        }
    }

    public boolean hasStudentSubmitted(int studentId) {
        for (AssessmentRecord record : submissions) {
            if (record.getStudentId() == studentId) {
                return true;
            }
        }
        return false;
    }

    @Converter
    public static class AssessmentRecordListConverter
            implements AttributeConverter<List<AssessmentRecord>, String> {

        private static final ObjectMapper MAPPER = new ObjectMapper();

        @Override
        public String convertToDatabaseColumn(List<AssessmentRecord> attribute) {
            try {
                return attribute == null ? "[]" : MAPPER.writeValueAsString(attribute);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to convert submissions to JSON", e);
            }
        }

        @Override
        public List<AssessmentRecord> convertToEntityAttribute(String dbData) {
            try {
                if (dbData == null || dbData.isBlank()) {
                    return new LinkedList<>();
                }
                return MAPPER.readValue(dbData,
                        new TypeReference<List<AssessmentRecord>>() {});
            } catch (Exception e) {
                throw new IllegalStateException("Failed to read submissions from JSON", e);
            }
        }
    }
}
