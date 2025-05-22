import Classroom.AssessmentItem;
import Classroom.AssessmentRecord;
import Classroom.LearningMaterial;
import Ollama.OllamaClient;
import Ollama.GradingResponse;
import Utils.Json;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class LLMTest {
    @Test
    public void testLLM() throws IOException, InterruptedException {
        LearningMaterial learningMaterial = OllamaClient.generateLearningMaterialProblem("Dictionaries", 1);
        GradingResponse gradingResponse = OllamaClient.solutionRequest(learningMaterial.getContent(), "screw everyone");
        AssessmentRecord assessmentRecord = new AssessmentRecord(gradingResponse.grade(), "screw everyone", 705999999, gradingResponse.feedback());
        learningMaterial.getAssessmentItem().submitSolution(assessmentRecord);
        Json.toJsonFile("src/test/resources/LLMTest_" + learningMaterial.getUuid() +".json", learningMaterial);
    }

    @Test
    public void testSubmitResponse() throws IOException, InterruptedException {
        LearningMaterial learningMaterial = new LearningMaterial("Dictionaries", "Make a loop that prints numbers from 1 to 10", true);
        learningMaterial.setAssessmentItem(new AssessmentItem(100));
        GradingResponse gradingResponse = OllamaClient.solutionRequest(learningMaterial.getContent(), "for(int i=1; i<=10) System.out.println(i);", "java");
        AssessmentRecord assessmentRecord = new AssessmentRecord(gradingResponse.grade(), "for(int i=1; i<=10) System.out.println(i);", 705256789, gradingResponse.feedback());
        learningMaterial.getAssessmentItem().submitSolution(assessmentRecord);

        GradingResponse gradingResponse2 = OllamaClient.solutionRequest(learningMaterial.getContent(), "for(int i=1; i<=10; i++) System.out.println(i);", "java");
        AssessmentRecord assessmentRecord2 = new AssessmentRecord(gradingResponse2.grade(), "for(int i=1; i<=10; i++) System.out.println(i);", 705123456, gradingResponse2.feedback());
        learningMaterial.getAssessmentItem().submitSolution(assessmentRecord2);
        Json.toJsonFile("src/test/resources/LLMTestFixedQuestion_" + learningMaterial.getUuid() +".json", learningMaterial);
    }
}
