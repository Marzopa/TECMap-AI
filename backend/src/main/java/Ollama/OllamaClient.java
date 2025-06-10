package Ollama;

import Classroom.AssessmentItem;
import Classroom.LearningMaterial;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import Repo.LearningMaterialRepo;

import java.io.IOException;
import java.net.http.*;
import java.net.URI;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.Objects;

@Service
public class OllamaClient {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Logger log = LoggerFactory.getLogger(OllamaClient.class);
    private static final ObjectMapper mapper = new ObjectMapper();


    /**
     * This method is used to send a request to the Ollama API. Returns the parsed response.
     * @param model the model to use for the request, e.g., "cs-problemGenerator", "cs-syntaxChecker", etc.
     * @param content the content to send in the request, typically a problem description or solution
     * @return the parsed response from the Ollama API
     */
    private static String OllamaRequest(String model, String content) throws IOException, InterruptedException {
        String baseUrl = System.getenv().getOrDefault("OLLAMA_HOST", "http://localhost:11434");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/chat"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(OllamaJsonBuilder(model, content)))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200)
            throw new IOException("Ollama service error: " + response.body());

        return OllamaResponseParser.parseResponse(response.body());
    }


    private static String OllamaJsonBuilder(String model, String content) {
        ObjectNode root = mapper.createObjectNode();
        root.put("model", model);
        ArrayNode messages = root.putArray("messages");
        ObjectNode message = messages.addObject();
        message.put("role", "user");
        message.put("content", content);
        try {
            return mapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    private static String problemRequest(String topic, int difficulty, String[] additionalTopics, String[] excludedTopics) throws IOException, InterruptedException {
        return OllamaRequest("cs-problemGenerator",
                String.format("topic: %s ~~~ difficulty: %d ~~~ additionalTopics: %s ~~~ excludedTopics: %s",
                        topic, difficulty,
                        String.join(", ", additionalTopics),
                        String.join(", ", excludedTopics)));
    }

    public String checkSyntax(String solution) throws IOException, InterruptedException {
        log.info("Checking syntax...");

        solution = solution.replace("\\", "\\\\")
                .replace("\"", "\\\"");

        log.info("Solution sent: {}", solution);
        String parsedResponse = OllamaRequest("cs-syntaxChecker", solution);
        log.info("Parsed response for syntax: {}", parsedResponse);
        return parsedResponse;
    }

    public GradingResponse solutionRequest(String problem, String solution, String topic) throws IOException, InterruptedException {
        String detectedLanguage = checkSyntax(solution).toLowerCase().trim();

        if (detectedLanguage.equals("not code")) return new GradingResponse("Not code", GradingStatus.NOT_CODE, null);

        String escapedSolution = solution.replace("\"", "\\\"");
        String feedbackContent = String.format("problem: %s ~~~ solution: %s ~~~ language: %s ~~~ topic: %s",
                problem.replace("\"", "\\\""),
                escapedSolution, detectedLanguage, topic);
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

    public String[] scanTopics(String problem, String solution) throws IOException, InterruptedException {
        String response = OllamaRequest("cs-topicScanner", String.format("problem: %s ~~~ solution: %s", problem, solution));
        log.info("Parsed topics response: {}", response);
        return response.replace("\n", "").split(", ");
    }

    /**
     * This method generates a LearningMaterial object with a problem based on the given topic and difficulty.
     * It uses the problemRequest method to get the problem content and then creates a LearningMaterial object.
     * The tags for the database are created by first solving the problem and then scanning the topics in that solution.
     * The solution is generated using the problemSolverHelper method.
     * @param topic            The topic for which the problem is generated.
     * @param difficulty       The difficulty level of the problem.
     * @param additionalTopics Additional topics to consider when generating the problem.
     * @param excludedTopics   Topics to exclude from the problem generation.
     * @return A LearningMaterial object containing the generated problem and its solution.
     * TODO: this method currently solves the problem in Java, but it should be hooked up to the database to retrieve the desired language for the classroom.
     */
    public LearningMaterial generateLearningMaterialProblem(String topic, int difficulty, String[] additionalTopics, String[] excludedTopics) throws IOException, InterruptedException {
        String problem = problemRequest(topic, difficulty, additionalTopics, excludedTopics);
        log.info("Generated problem: {}", problem);
        LearningMaterial learningMaterial = new LearningMaterial(topic, problem, true);
        AssessmentItem assessmentItem = new AssessmentItem();
        learningMaterial.setAssessmentItem(assessmentItem);
        String solution = problemSolverHelper(learningMaterial, "java", true);
        log.info("Generated solution: {}", solution);
        List<String> scanTopics = new java.util.ArrayList<>(List.of(scanTopics(learningMaterial.getContent(), solution)));
        if (scanTopics.get(scanTopics.size()-1).toLowerCase().strip().equals("etc.")) scanTopics.remove(scanTopics.size() - 1);
        log.info("Scanned topics: {}", scanTopics);
        learningMaterial.setTags(scanTopics);
        return learningMaterial;
    }

    /**
     * This method is used to solve a problem using the problemSolver model.
     * It takes a LearningMaterial object as input and returns the solution as a string.
     * TODO: this method should be hooked up to database to retrieve student concepts AND desired language
     */
    public String problemSolverHelper(LearningMaterial learningMaterial, String language) throws IOException, InterruptedException {
        return problemSolverHelper(learningMaterial, language, false);
    }

    private String problemSolverHelper(LearningMaterial learningMaterial, String language, boolean small) throws IOException, InterruptedException {
        String content = "problem: " + learningMaterial.getContent() + "language: " + language;
        if (small) return OllamaRequest("cs-smallProblemSolver", content);
        return OllamaRequest("cs-problemSolver", content);
    }

}