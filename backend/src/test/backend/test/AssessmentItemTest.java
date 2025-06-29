package backend.test;

import Classroom.AssessmentItem;
import Classroom.AssessmentRecord;
import Classroom.LearningMaterial;
import Ollama.GradingStatus;
import Utils.Json;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;


public class AssessmentItemTest {
    @Test
    public void testAssessmentItemCreation() {
        AssessmentItem ai = new AssessmentItem();
        Assertions.assertNotNull(ai);
    }

    @Test
    public void testAssessmentItemCreation2() throws IOException {
        LearningMaterial lm = new LearningMaterial("Sample Learning Material", "This is a sample content.", true);
        AssessmentItem ai = new AssessmentItem();
        lm.setAssessmentItem(ai);
        Assertions.assertNotNull(ai);
        ai.submitSolution(GradingStatus.CORRECT, "Sample Answer", 705123456, "Good job!");
        ai.submitSolution(new AssessmentRecord(GradingStatus.INCORRECT, "Trash studentAnswer", 705456789, "You're trash!"));
        Json.toJsonFile("src/test/resources/LM_" + lm.getUuid() +".json", lm);
    }

    @Test
    public void testSubissions() throws IOException {
        LearningMaterial lm = Json.fromJsonFile("src/test/resources/LLMTestFixedQuestion_32cd931e-784d-4ab8-be4a-c2cb6121d032.json", LearningMaterial.class);
        AssessmentItem ai = lm.getAssessmentItem();
        Assertions.assertEquals(2, ai.getSubmissions().size());

        ai.submitSolution(GradingStatus.CORRECT, "Sample Answer", 705123456, "Good job!");
        ai.submitSolution(new AssessmentRecord(GradingStatus.INCORRECT, "Trash studentAnswer", 705456789, "You're trash!"));
        Assertions.assertEquals(4, ai.getSubmissions().size());
    }
}
