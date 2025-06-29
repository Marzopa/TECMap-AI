package backend.test;

import Classroom.AssessmentItem;
import Classroom.AssessmentRecord;
import Classroom.LearningMaterial;
import Ollama.GradingStatus;
import Utils.Json;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class LearningMaterialTest {

    private static final String TEST_PATH = "src/test/resources/";

    @BeforeEach
    public void setUp() throws IOException {
        // Ensure the test directory exists
        Files.createDirectories(Paths.get(TEST_PATH));
    }

    @Test
    public void testLearningMaterialCreation() {
        LearningMaterial lm = new LearningMaterial("Sample Learning Material", "This is a sample content.", true);
        assertNotNull(lm);
        assertEquals("Sample Learning Material", lm.getTitle());
        assertEquals("This is a sample content.", lm.getContent());
        assertTrue(lm.isAnswerable());
    }

    @Test
    public void testLearningMaterialWithAssessmentItem() {
        LearningMaterial lm = new LearningMaterial("Sample Learning Material", "This is a sample content.", true);
        AssessmentItem ai = new AssessmentItem();
        lm.setAssessmentItem(ai);

        assertNotNull(lm.getAssessmentItem());
    }

    @Test
    public void testSaveAndLoadLearningMaterial() throws IOException {
        LearningMaterial lm = new LearningMaterial("Sample Learning Material", "This is a sample content.", true);
        AssessmentItem ai = new AssessmentItem();
        AssessmentRecord ar = new AssessmentRecord(GradingStatus.CORRECT, "Sample Answer", 705123456, "Good job!");
        ai.addSubmission(ar);
        lm.setAssessmentItem(ai);

        // Save to file
        String filename = lm.saveToFile(TEST_PATH);
        assertNotNull(filename);

        // Load from file
        LearningMaterial loadedLm = Json.fromJsonFile(filename, LearningMaterial.class);
        assertNotNull(loadedLm);
        assertEquals(lm.getTitle(), loadedLm.getTitle());
        assertEquals(lm.getContent(), loadedLm.getContent());
        assertTrue(loadedLm.isAnswerable());

        AssessmentItem loadedAi = loadedLm.getAssessmentItem();
        assertNotNull(loadedAi);

        AssessmentRecord loadedAr = loadedAi.getSubmissions().get(0);
        assertNotNull(loadedAr);
        assertEquals(ar.score(), loadedAr.score());
        assertEquals(ar.studentAnswer(), loadedAr.getAnswer());
        assertEquals(ar.studentId(), loadedAr.studentId());
        assertEquals(ar.feedback(), loadedAr.feedback());
    }

    @Test
    public void testLoadFromSampleJson() throws IOException {
        // Load from sample JSON file
        LearningMaterial loadedLm = Json.fromJsonFile(TEST_PATH + "LM_123e4567-e89b-12d3-a456-426614174000.json", LearningMaterial.class);
        assertNotNull(loadedLm);
        assertEquals("Sample Learning Material", loadedLm.getTitle());
        assertEquals("This is a sample content.", loadedLm.getContent());
        assertTrue(loadedLm.isAnswerable());

        AssessmentItem loadedAi = loadedLm.getAssessmentItem();
        assertNotNull(loadedAi);

        AssessmentRecord loadedAr = loadedAi.getSubmissions().get(0);
        assertNotNull(loadedAr);
        assertEquals(GradingStatus.CORRECT, loadedAr.score());
        assertEquals("Sample Answer", loadedAr.studentAnswer());
        assertEquals(705123456, loadedAr.studentId());
        assertEquals("Good job!", loadedAr.feedback());

        AssessmentRecord loadedAr2 = loadedAi.getSubmissions().get(1);
        assertNotNull(loadedAr2);
        assertEquals(GradingStatus.PARTIALLY_CORRECT, loadedAr2.score());
        assertEquals("Another Sample Answer", loadedAr2.studentAnswer());
        assertEquals(705456789, loadedAr2.studentId());
        assertEquals("Well done!", loadedAr2.feedback());

        LearningMaterial loadedLm2 = Json.fromJsonFile(TEST_PATH + "LM_2467c622-1441-4318-9850-f1b07301b2f5.json", LearningMaterial.class);
        assertNotNull(loadedLm2);
        assertFalse(loadedLm2.isAnswerable());
        assertNull(loadedLm2.getAssessmentItem());
    }
}