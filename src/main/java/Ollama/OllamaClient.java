package Ollama;

import Classroom.AssessmentItem;
import Classroom.LearningMaterial;

import java.io.IOException;
import java.net.http.*;
import java.net.URI;
import java.net.http.HttpResponse.BodyHandlers;

public class OllamaClient {
    private static String problemRequest(String topic, int difficulty) throws IOException, InterruptedException {

        String json = String.format("""
        {
        "model": "cs-problemGenerator",
            "messages": [
                { "role": "user", "content": "%s %d"}
            ]
        }
        """, topic, difficulty);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/chat"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        return OllamaResponseParser.parseResponse(response.body());
    }

    public static GradingResponse solutionRequest(String problem, String solution) throws IOException, InterruptedException {
        return solutionRequest(problem, solution, "python");
    }

    public static GradingResponse solutionRequest(String problem, String solution, String language) throws IOException, InterruptedException {

        String json = String.format("""
        {
        "model": "cs-grader",
            "messages": [
                { "role": "user", "content": problem: %s solution: %s language: %s"}
            ]
        }
        """, problem, solution, language);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/chat"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        String parsedResponse = OllamaResponseParser.parseResponse(response.body());
        String[] parts = parsedResponse.split(";");
        String feedback = parts[0];
        System.err.println(feedback);
        System.err.println(parts[1]);
        int grade = Integer.parseInt(parts[1].trim());
        return new GradingResponse(feedback, grade);
    }

    public static LearningMaterial generateLearningMaterialProblem(String topic, int difficulty) throws IOException, InterruptedException {
        String problem = problemRequest(topic, difficulty);
        LearningMaterial learningMaterial = new LearningMaterial(topic, problem, true);
        AssessmentItem assessmentItem = new AssessmentItem(100);
        learningMaterial.setAssessmentItem(assessmentItem);

        return learningMaterial;
    }

    public static void main(String[] args) throws Exception {
        LearningMaterial lm = generateLearningMaterialProblem("arrays", 3);
        System.out.println("Learning Material Title: " + lm.getTitle());
        System.out.println("Learning Material Content: " + lm.getContent());
    }
}