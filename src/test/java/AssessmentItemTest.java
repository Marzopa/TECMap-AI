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
        AssessmentItem ai = new AssessmentItem("Sample Question?", 100);
        Assertions.assertNotNull(ai);
        assertEquals("Sample Question?", ai.getQuestion());
        Assertions.assertEquals(100, ai.getMaxScore());
    }

    @Test
    public void testAssessmentItemCreation2() throws IOException {
        LearningMaterial lm = new LearningMaterial("Sample Learning Material", "This is a sample content.", true);
        AssessmentItem ai = new AssessmentItem(100);
        lm.setAssessmentItem(ai);
        Assertions.assertNotNull(ai);
        ai.submitSolution(90, "Sample Answer", "oscar123", "Good job!");
        ai.submitSolution(new AssessmentRecord(50, "Trash studentAnswer", "geeker456", "You're trash!"));
        Json.toJsonFile("src/test/resources/LM_" + lm.getUuid() +".json", lm);
    }
}
