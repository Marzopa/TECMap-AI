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
        // Escape quotes in the solution string
        String escapedSolution = solution.replace("\"", "\\\"");
        String content = String.format("problem: %s solution: %s language: %s",
                problem.replace("\"", "\\\""),
                escapedSolution,
                language);

        String json = """
        {
          "model": "cs-grader",
          "messages": [
            { "role": "user", "content": "%s" }
          ]
        }
        """.formatted(content);

        System.out.println("Sending grader request with payload:");
        System.out.println(json);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/chat"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Grader service error: " + response.body());
        }

        String parsedResponse = OllamaResponseParser.parseResponse(response.body());
        System.out.println("Parsed response: " + parsedResponse);

        // Add response validation and cleaning
        try {
            // Split response and trim any whitespace
            String[] parts = parsedResponse.trim().split(";");
            if (parts.length != 2) {
                throw new IOException("Invalid response format: expected 'feedback;grade'");
            }

            String feedback = parts[0].trim();
            // Clean the grade string and parse
            String gradeStr = parts[1].trim().replaceAll("[^0-9-]", "");
            int grade = Integer.parseInt(gradeStr);

            return new GradingResponse(feedback, grade);
        } catch (NumberFormatException e) {
            throw new IOException("Invalid grade format in response: " + parsedResponse);
        } catch (Exception e) {
            throw new IOException("Error processing grader response: " + e.getMessage());
        }
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