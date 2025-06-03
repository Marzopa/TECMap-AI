package backend.test;

import API.SolveRequest;
import API.SubmissionRequest;
import Classroom.LearningMaterial;
import Utils.Json;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DataControllerTest {
    @Test
    public void unsolvedMatchingProblemTest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        String url = "http://localhost:8080/ai/problem";

        String jsonPayload = """
                {
                    "topic": "hashmaps",
                    "difficulty": 5,
                    "additionalTopics": ["recursion", "loops", "conditionals", "arrays"],
                    "excludedTopics": ["caching", "concurrency"],
                    "studentId": 705123456
                }
                """;

        System.out.println(">>>> SENDING TO OLLAMA: " + jsonPayload);

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("POST /ai/problem response:");
        System.out.println("Status: " + postResponse.statusCode());
        System.out.println("Body: " + postResponse.body());
        LearningMaterial glorbo = Json.fromJsonString(postResponse.body(), LearningMaterial.class);
        System.out.println("Glorbo item: " + glorbo.getAssessmentItem());

        url = "http://localhost:8080/ai/submit";
        SubmissionRequest request = new SubmissionRequest(glorbo, "System.out.println(\"Geeking\");", 705123456);
        postRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(Json.toJsonString(request)))
                .build();

        postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("POST /ai/problem response:");
        System.out.println("Status: " + postResponse.statusCode());
        System.out.println("Body: " + postResponse.body());
    }
}
