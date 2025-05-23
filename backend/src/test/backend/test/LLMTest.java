package backend.test;

import Classroom.AssessmentItem;
import Classroom.AssessmentRecord;
import Classroom.LearningMaterial;
import Ollama.OllamaClient;
import Ollama.GradingResponse;
import Utils.Json;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LLMTest {
    @Test
    public void testLLM() throws IOException, InterruptedException {
        LearningMaterial learningMaterial = OllamaClient.generateLearningMaterialProblem("Dictionaries", 1);
        GradingResponse gradingResponse = OllamaClient.solutionRequest(learningMaterial.getContent(), "screw everyone");
        AssessmentRecord assessmentRecord = new AssessmentRecord(gradingResponse.grade(), "screw everyone", 705999999, gradingResponse.feedback());
        learningMaterial.getAssessmentItem().submitSolution(assessmentRecord);
        Json.toJsonFile("backend/src/test/resources/LLMTest_" + learningMaterial.getUuid() +".json", learningMaterial);
    }

    @Test
    public void testSubmitResponse() throws IOException, InterruptedException {
        LearningMaterial learningMaterial = new LearningMaterial("Dictionaries", "Make a loop that prints numbers from 1 to 10", true);
        learningMaterial.setAssessmentItem(new AssessmentItem(100));
        GradingResponse gradingResponse = OllamaClient.solutionRequest(learningMaterial.getContent(), "for(int i=1; i<=10) System.out.println(i);");
        AssessmentRecord assessmentRecord = new AssessmentRecord(gradingResponse.grade(), "for(int i=1; i<=10) System.out.println(i);", 705256789, gradingResponse.feedback());
        learningMaterial.getAssessmentItem().submitSolution(assessmentRecord);

        GradingResponse gradingResponse2 = OllamaClient.solutionRequest(learningMaterial.getContent(), "for(int i=1; i<=10; i++) System.out.println(i);");
        AssessmentRecord assessmentRecord2 = new AssessmentRecord(gradingResponse2.grade(), "for(int i=1; i<=10; i++) System.out.println(i);", 705123456, gradingResponse2.feedback());
        learningMaterial.getAssessmentItem().submitSolution(assessmentRecord2);
        Json.toJsonFile("backend/src/test/resources/LLMTestFixedQuestion_" + learningMaterial.getUuid() +".json", learningMaterial);
    }

    @Test
    public void testSyntaxChecker() throws IOException, InterruptedException {
        String code = "for(int i=1; i<=10; i++) System.out.println(i);";
        String result = OllamaClient.checkSyntax(code);
        System.err.println(result);
        assertEquals("java", result.toLowerCase());
        String code2 = "i hate everyone";
        String result2 = OllamaClient.checkSyntax(code2);
        System.err.println(result2);
        assertEquals("not code", result2.toLowerCase());
        String code3 = "for(i in range(10)): print(i)";
        String result3 = OllamaClient.checkSyntax(code3);
        System.err.println(result3);
        assertEquals("python", result3.toLowerCase());
    }

    @Test
    public void generateProblems() throws IOException, InterruptedException {
        String topic = "Dictionaries";
        for(int i = 0; i<5; i++) {
            int difficulty = 3;
            LearningMaterial learningMaterial = OllamaClient.generateLearningMaterialProblem(topic, difficulty);
            System.out.println("Generated Learning Material " + i + ":a\t" +  learningMaterial.getContent());
        }
    }

    @Test
    public void testGradingResponse() throws IOException, InterruptedException {
        String problem = "Make a loop that prints numbers from 1 to 10";
        String code = "for(int i=1; i<=10; i++) System.out.println(i);";
        for(int i = 0; i<10; i++) {
            GradingResponse gradingResponse = OllamaClient.solutionRequest(problem, code);
            System.err.println("Number " + i + ":");
            System.err.println("\t" + gradingResponse.grade());
            System.err.println("\t" + gradingResponse.detectedLanguage());
            System.err.println("\t" + gradingResponse.feedback());
        }
        System.err.println("##################################################");
        code = "for(i in range(10)): print(i)";
        for(int i = 0; i<10; i++) {
            GradingResponse gradingResponse = OllamaClient.solutionRequest(problem, code);
            System.err.println("Number " + i + ":");
            System.err.println("\t" + gradingResponse.grade());
            System.err.println("\t" + gradingResponse.detectedLanguage());
            System.err.println("\t" + gradingResponse.feedback());
        }
        System.err.println("##################################################");
        code = "i dont know bro";
        for(int i = 0; i<10; i++) {
            GradingResponse gradingResponse = OllamaClient.solutionRequest(problem, code);
            System.err.println("Number " + i + ":");
            System.err.println("\t" + gradingResponse.grade());
            System.err.println("\t" + gradingResponse.detectedLanguage());
            System.err.println("\t" + gradingResponse.feedback());
        }
    }

    @Test
    public void testGradingResponse2() throws IOException, InterruptedException {
        String problem = "Make a loop that prints numbers from 1 to 10";
        String code = "System.out.println(5);";
        for(int i = 0; i<10; i++) {
            GradingResponse gradingResponse = OllamaClient.solutionRequest(problem, code);
            System.err.println("Number " + i + ":");
            System.err.println("\t" + gradingResponse.grade());
            System.err.println("\t" + gradingResponse.detectedLanguage());
            System.err.println("\t" + gradingResponse.feedback());
        }
    }
}
