package Ollama;

import Classroom.AssessmentItem;
import Classroom.LearningMaterial;

import java.io.IOException;
import java.net.http.*;
import java.net.URI;
import java.net.http.HttpResponse.BodyHandlers;

public class OllamaClient {

    private static final HttpClient client = HttpClient.newHttpClient();

    private static String OllamaRequest(String json) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/chat"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Ollama service error: " + response.body());
        }

        return OllamaResponseParser.parseResponse(response.body());
    }

    private static String OllamaJsonBuilder(String model, String content) {
        return String.format("""
        {
            "model": "%s",
            "messages": [
                { "role": "user", "content": "%s"}
            ]
        }
        """, model, content);
    }

    private static String problemRequest(String topic, int difficulty) throws IOException, InterruptedException {
        String json = OllamaJsonBuilder("cs-problemGenerator", topic + " " + difficulty);
        return OllamaRequest(json);
    }

    public static String checkSyntax(String solution) throws IOException, InterruptedException {
        //System.err.println("Checking syntax...");

        solution = solution.replace("\\", "\\\\")
                .replace("\"", "\\\"");

        //System.out.println("Solution sent: " + solution);
        String json = OllamaJsonBuilder("cs-syntaxChecker", solution);
        String parsedResponse = OllamaRequest(json);
        //System.out.println("Parsed response for syntax: " + parsedResponse);
        return parsedResponse;
    }

    public static GradingResponse solutionRequest(String problem, String solution) throws IOException, InterruptedException {
        String detectedLanguage = checkSyntax(solution).toLowerCase().trim();

        if (detectedLanguage.equals("not code")) return new GradingResponse("Not code", 0, null);

        String escapedSolution = solution.replace("\"", "\\\"");

        String json = OllamaJsonBuilder("cs-problemGrader",
                String.format("problem: %s solution: %s language: %s",
                problem.replace("\"", "\\\""),
                escapedSolution, detectedLanguage));

        //System.out.println("Sending grader request with payload:");
        //System.out.println(json);

        String parsedResponse = OllamaRequest(json).split("\n")[0]; // Keep only the first line
        //System.out.println("Parsed response: " + parsedResponse);

        try {
            String[] parts = parsedResponse.trim().split("~~~");
            if (parts.length != 2) throw new IOException("Invalid response format: expected 'feedback ~~~ grade'");
            String feedback = parts[0].trim();
            String gradeStr = parts[1].trim().replaceAll("[^0-9]", "");
            int grade = Integer.parseInt(gradeStr);
            return new GradingResponse(feedback, grade, detectedLanguage);
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

    /**
     * This method is used to solve a problem using the problemSolver model.
     * It takes a LearningMaterial object as input and returns the solution as a string.
     * TODO: this method should be hooked up to database to retrieve student concepts AND desired language
     */
    public static String problemSolverHelper(LearningMaterial learningMaterial, String language) throws IOException, InterruptedException {
        String content = "problem: " + learningMaterial.getContent() + "language: " + language;
        return OllamaRequest(OllamaJsonBuilder("cs-problemSolver", content));
    }

    public static void main(String[] args) throws Exception {
        LearningMaterial lm = generateLearningMaterialProblem("arrays", 3);
        System.out.println("Learning Material Title: " + lm.getTitle());
        System.out.println("Learning Material Content: " + lm.getContent());
    }
}