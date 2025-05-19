import Classroom.AssessmentItem;
import Classroom.AssessmentRecord;
import Classroom.LearningMaterial;
import Ollama.OllamaClient;
import Ollama.Response;
import Utils.Json;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class LLMTest {
    @Test
    public void testLLM() throws IOException, InterruptedException {
        LearningMaterial learningMaterial = OllamaClient.createLearningMaterialFromResponse("Dictionaries", 1);
        Response response = OllamaClient.solutionRequest(learningMaterial.getContent(), "screw everyone");
        AssessmentRecord assessmentRecord = new AssessmentRecord(response.grade(), "screw everyone", "studentId", response.feedback());
        learningMaterial.getAssessmentItem().submitSolution(assessmentRecord);
        Json.toJsonFile("src/test/resources/LLMTest_" + learningMaterial.getUuid() +".json", learningMaterial);
    }

    @Test
    public void testSubmitResponse() throws IOException, InterruptedException {
        LearningMaterial learningMaterial = new LearningMaterial("Dictionaries", "Make a loop that prints numbers from 1 to 10", true);
        learningMaterial.setAssessmentItem(new AssessmentItem(100));
        Response response = OllamaClient.solutionRequest(learningMaterial.getContent(), "for(int i=1; i<=10) System.out.println(i);", "java");
        AssessmentRecord assessmentRecord = new AssessmentRecord(response.grade(), "for(int i=1; i<=10) System.out.println(i);", "tweaky256", response.feedback());
        learningMaterial.getAssessmentItem().submitSolution(assessmentRecord);

        Response response2 = OllamaClient.solutionRequest(learningMaterial.getContent(), "for(int i=1; i<=10; i++) System.out.println(i);", "java");
        AssessmentRecord assessmentRecord2 = new AssessmentRecord(response2.grade(), "for(int i=1; i<=10; i++) System.out.println(i);", "oscar123", response2.feedback());
        learningMaterial.getAssessmentItem().submitSolution(assessmentRecord2);
        Json.toJsonFile("src/test/resources/LLMTestFixedQuestion_" + learningMaterial.getUuid() +".json", learningMaterial);
    }
}
