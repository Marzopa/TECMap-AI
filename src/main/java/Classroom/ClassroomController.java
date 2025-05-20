package Classroom;

import Ollama.OllamaClient;
import Ollama.Response;
import Utils.Json;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/classroom")
public class ClassroomController {

    // A map that holds student credentials loaded from a JSON file.
    private static final Map<String, StudentCredential> credentials = loadCredentials();

    public static Map<String, StudentCredential> getCredentials() {
        return credentials;
    }

    // A map that holds the currently assigned problem for each student.
    public static final Map<String, LearningMaterial> currentAssignments = new ConcurrentHashMap<>();

    // Load credentials from the file at startup.
//    private static Map<String, StudentCredential> loadCredentials() {
//        ObjectMapper mapper = new ObjectMapper();
//        try {
//            JsonNode root = mapper.readTree(new File("src/test/resources/ControllerCredentials.json"));
//            JsonNode studentNodes = root.get("students");
//            Map<String, StudentCredential> credentialMap = new ConcurrentHashMap<>();
//            studentNodes.fields().forEachRemaining(entry -> {
//                JsonNode node = entry.getValue();
//                credentialMap.put(entry.getKey(), new StudentCredential(
//                        node.get("username").asText(),
//                        node.get("password").asText()));
//            });
//            return credentialMap;
//        } catch (IOException e) {
//            System.err.println("Error loading credentials from file: " + e.getMessage());
//            return Collections.emptyMap();
//        }
//    }

    private static Map<String, StudentCredential> loadCredentials() {
        try {
            return Json.fromJsonFile("src/test/resources/ControllerCredentials.json",
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, StudentCredential>>() {});
        } catch (IOException e) {
            System.err.println("Error loading credentials from file: " + e.getMessage());
            return Collections.emptyMap();
        }
    }

    // Authenticate the student using the provided id and password.
    private boolean authenticate(String studentId, String password) {
        StudentCredential studentCredential = credentials.get(studentId);
        return studentCredential != null && studentCredential.password().equals(password);
    }

    // Endpoint to get a problem.
    @GetMapping("/problem")
    public ProblemResponse getProblem(@RequestParam String studentId, @RequestParam String password)
            throws IOException, InterruptedException {
        if (!authenticate(studentId, password)) {
            throw new IllegalArgumentException("Student credentials are not valid.");
        }
        // If the student already has a problem, return it.
        // Otherwise, request a new problem from the Ollama client.
        LearningMaterial material = currentAssignments.computeIfAbsent(studentId, id -> {
            try {
                return OllamaClient.createLearningMaterialFromResponse("arrays", 3);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Error when requesting a new problem.", e);
            }
        });
        return new ProblemResponse(material.getTitle(), material.getContent());
    }

    // Endpoint to submit a solution.
    @PostMapping("/submit")
    public SubmissionResponse submitSolution(@RequestParam String studentId,
                                             @RequestParam String password,
                                             @RequestParam String solution)
            throws IOException, InterruptedException {
        if (!authenticate(studentId, password)) {
            throw new IllegalArgumentException("Student credentials are not valid.");
        }
        LearningMaterial material = currentAssignments.get(studentId);
        if (material == null) {
            throw new IllegalArgumentException("No problem is currently assigned to the student.");
        }
        // Get the problem from the learning material.
        String problem = material.getContent();
        // Request feedback from the Ollama client.
        Response ollamaResponse = OllamaClient.solutionRequest(problem, solution);
        // Record the submission in the assessment item.
        AssessmentItem item = material.getAssessmentItem();
        if (item != null) {
            item.submitSolution(ollamaResponse.grade(), solution, studentId, ollamaResponse.feedback());
        }
        // Remove the assignment so that a new problem can be requested later.
        currentAssignments.remove(studentId);
        return new SubmissionResponse(ollamaResponse.feedback(), ollamaResponse.grade());
    }

    // Data transfer object for returning a problem.
    public record ProblemResponse(String title, String problem) {
    }

    public record SubmissionResponse(String feedback, int grade) {
    }

    public record StudentCredential(@JsonProperty("username") String username, @JsonProperty("password") String password) {

    }
}