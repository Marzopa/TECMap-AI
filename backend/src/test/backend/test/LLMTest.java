package backend.test;

import Classroom.AssessmentItem;
import Classroom.AssessmentRecord;
import Classroom.LearningMaterial;
import Ollama.OllamaClient;
import Ollama.GradingResponse;
import Utils.Json;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = API.MicroserviceApp.class)
public class LLMTest {

    @Autowired
    private OllamaClient ollamaClient;

    @Test
    public void testLLM() throws IOException, InterruptedException {
        LearningMaterial learningMaterial = ollamaClient.generateLearningMaterialProblem("Dictionaries", 1);
        GradingResponse gradingResponse = ollamaClient.solutionRequest(learningMaterial.getContent(), "screw everyone", "Dictionaries");
        AssessmentRecord assessmentRecord = new AssessmentRecord(gradingResponse.grade(), "screw everyone", 705999999, gradingResponse.feedback());
        learningMaterial.getAssessmentItem().submitSolution(assessmentRecord);
        Json.toJsonFile("src/test/resources/LLMTest_" + learningMaterial.getUuid() +".json", learningMaterial);
    }

    @Test
    public void testSubmitResponse() throws IOException, InterruptedException {
        LearningMaterial learningMaterial = new LearningMaterial("Dictionaries", "Make a loop that prints numbers from 1 to 10", true);
        learningMaterial.setAssessmentItem(new AssessmentItem());
        GradingResponse gradingResponse = ollamaClient.solutionRequest(learningMaterial.getContent(), "for(int i=1; i<=10) System.out.println(i);", "Dictionaries");
        AssessmentRecord assessmentRecord = new AssessmentRecord(gradingResponse.grade(), "for(int i=1; i<=10) System.out.println(i);", 705256789, gradingResponse.feedback());
        learningMaterial.getAssessmentItem().submitSolution(assessmentRecord);

        GradingResponse gradingResponse2 = ollamaClient.solutionRequest(learningMaterial.getContent(), "for(int i=1; i<=10; i++) System.out.println(i);", "Dictionaries");
        AssessmentRecord assessmentRecord2 = new AssessmentRecord(gradingResponse2.grade(), "for(int i=1; i<=10; i++) System.out.println(i);", 705123456, gradingResponse2.feedback());
        learningMaterial.getAssessmentItem().submitSolution(assessmentRecord2);
        Json.toJsonFile("src/test/resources/LLMTestFixedQuestion_" + learningMaterial.getUuid() +".json", learningMaterial);
    }

    @Test
    public void testSyntaxChecker() throws IOException, InterruptedException {
        String code = "for(int i=1; i<=10; i++) System.out.println(i);";
        String result = ollamaClient.checkSyntax(code);
        System.err.println(result);
        assertEquals("java", result.toLowerCase());
        String code2 = "i hate everyone";
        String result2 = ollamaClient.checkSyntax(code2);
        System.err.println(result2);
        assertEquals("not code", result2.toLowerCase());
        String code3 = "for(i in range(10)): print(i)";
        String result3 = ollamaClient.checkSyntax(code3);
        System.err.println(result3);
        assertEquals("python", result3.toLowerCase());
    }

    @Test
    public void generateProblems() throws IOException, InterruptedException {
        String topic = "Dictionaries";
        for(int i = 0; i<5; i++) {
            int difficulty = 3;
            LearningMaterial learningMaterial = ollamaClient.generateLearningMaterialProblem(topic, difficulty);
            System.out.println("Generated Learning Material " + i + ":a\t" +  learningMaterial.getContent());
        }
    }

    @Test
    public void testGradingResponse() throws IOException, InterruptedException {
        String problem = "Make a loop that prints numbers from 1 to 10";
        String code = "for(int i=1; i<=10; i++) System.out.println(i);";
        for(int i = 0; i<10; i++) {
            GradingResponse gradingResponse = ollamaClient.solutionRequest(problem, code, "Loops");
            System.err.println("Number " + i + ":");
            System.err.println("\t" + gradingResponse.grade());
            System.err.println("\t" + gradingResponse.detectedLanguage());
            System.err.println("\t" + gradingResponse.feedback());
        }
        System.err.println("##################################################");
        code = "for(i in range(10)): print(i)";
        for(int i = 0; i<10; i++) {
            GradingResponse gradingResponse = ollamaClient.solutionRequest(problem, code, "Loops");
            System.err.println("Number " + i + ":");
            System.err.println("\t" + gradingResponse.grade());
            System.err.println("\t" + gradingResponse.detectedLanguage());
            System.err.println("\t" + gradingResponse.feedback());
        }
        System.err.println("##################################################");
        code = "i dont know bro";
        for(int i = 0; i<10; i++) {
            GradingResponse gradingResponse = ollamaClient.solutionRequest(problem, code, "Loops");
            System.err.println("Number " + i + ":");
            System.err.println("\t" + gradingResponse.grade());
            System.err.println("\t" + gradingResponse.detectedLanguage());
            System.err.println("\t" + gradingResponse.feedback());
        }
        System.err.println("##################################################");
        code = "System.out.println(5);";
        for(int i = 0; i<10; i++) {
            GradingResponse gradingResponse = ollamaClient.solutionRequest(problem, code, "Loops");
            System.err.println("Number " + i + ":");
            System.err.println("\t" + gradingResponse.grade());
            System.err.println("\t" + gradingResponse.detectedLanguage());
            System.err.println("\t" + gradingResponse.feedback());
        }
    }
}
