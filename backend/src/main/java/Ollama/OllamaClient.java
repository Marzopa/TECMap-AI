package Ollama;

import Classroom.AssessmentItem;
import Classroom.LearningMaterial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.http.*;
import java.net.URI;
import java.net.http.HttpResponse.BodyHandlers;

public class OllamaClient {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Logger log = LoggerFactory.getLogger(OllamaClient.class);

    /**
     * This method is used to send a request to the Ollama API. Returns the parsed response.
     * @param model the model to use for the request, e.g., "cs-problemGenerator", "cs-syntaxChecker", etc.
     * @param content the content to send in the request, typically a problem description or solution
     * @return the parsed response from the Ollama API
     */
    private static String OllamaRequest(String model, String content) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/chat"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(OllamaJsonBuilder(model, content)))
                .build();
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        if (response.statusCode() != 200) throw new IOException("Ollama service error: " + response.body());

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
        return OllamaRequest("cs-problemGenerator", topic + " " + difficulty);
    }

    public static String checkSyntax(String solution) throws IOException, InterruptedException {
        log.info("Checking syntax...");

        solution = solution.replace("\\", "\\\\")
                .replace("\"", "\\\"");

        log.info("Solution sent: {}", solution);
        String parsedResponse = OllamaRequest("cs-syntaxChecker", solution);
        log.info("Parsed response for syntax: {}", parsedResponse);
        return parsedResponse;
    }

    public static GradingResponse solutionRequest(String problem, String solution) throws IOException, InterruptedException {
        String detectedLanguage = checkSyntax(solution).toLowerCase().trim();

        if (detectedLanguage.equals("not code")) return new GradingResponse("Not code", GradingStatus.NOT_CODE, null);

        String escapedSolution = solution.replace("\"", "\\\"");
        String feedbackContent = String.format("problem: %s ~~~ solution: %s ~~~ language: %s",
                problem.replace("\"", "\\\""),
                escapedSolution, detectedLanguage);
        log.info("Sending feedback request with content: {}", feedbackContent);
        String feedback = OllamaRequest("cs-feedbackGenerator", feedbackContent);
        log.info("Parsed feedback: {}", feedback);

        String gradeContent = String.format("problem: %s ~~~ solution: %s ~~~ feedback: %s",
                problem.replace("\"", "\\\""),
                escapedSolution, feedback);
        String gradeStr = OllamaRequest("cs-problemGrader", gradeContent).trim();
        GradingStatus grade =  GradingStatus.valueOf(gradeStr);

        return new GradingResponse(feedback, grade, detectedLanguage);

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
        return OllamaRequest("cs-problemSolver", content);
    }

    public static void main(String[] args) throws Exception {
        LearningMaterial lm = generateLearningMaterialProblem("arrays", 3);
        System.out.println("Learning Material Title: " + lm.getTitle());
        System.out.println("Learning Material Content: " + lm.getContent());
    }
}