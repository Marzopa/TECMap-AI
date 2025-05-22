import Classroom.AssessmentItem;
import Classroom.AssessmentRecord;
import Classroom.LearningMaterial;
import Utils.Json;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;


public class AssessmentItemTest {
    @Test
    public void testAssessmentItemCreation() {
        AssessmentItem ai = new AssessmentItem(100);
        Assertions.assertNotNull(ai);
        Assertions.assertEquals(100, ai.getMaxScore());
    }

    @Test
    public void testAssessmentItemCreation2() throws IOException {
        LearningMaterial lm = new LearningMaterial("Sample Learning Material", "This is a sample content.", true);
        AssessmentItem ai = new AssessmentItem(100);
        lm.setAssessmentItem(ai);
        Assertions.assertNotNull(ai);
        ai.submitSolution(90, "Sample Answer", 705123456, "Good job!");
        ai.submitSolution(new AssessmentRecord(50, "Trash studentAnswer", 705456789, "You're trash!"));
        Json.toJsonFile("src/test/resources/LM_" + lm.getUuid() +".json", lm);
    }

    @Test
    public void testSubissions() throws IOException {
        LearningMaterial lm = Json.fromJsonFile("src/test/resources/LLMTestFixedQuestion_32cd931e-784d-4ab8-be4a-c2cb6121d032.json", LearningMaterial.class);
        AssessmentItem ai = lm.getAssessmentItem();
        Assertions.assertEquals(2, ai.getSubmissions().size());
        Assertions.assertEquals(100, ai.getMaxScore());

        ai.submitSolution(90, "Sample Answer", 705123456, "Good job!");
        ai.submitSolution(new AssessmentRecord(50, "Trash studentAnswer", 705456789, "You're trash!"));
        Assertions.assertEquals(4, ai.getSubmissions().size());
    }
}
