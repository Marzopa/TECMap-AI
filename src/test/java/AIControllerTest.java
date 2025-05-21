import Classroom.LearningMaterial;
import Utils.Json;
import org.junit.jupiter.api.Test;

import Ollama.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AIControllerTest {

    // This method performs a GET request to /ai/problem and prints its status and body.
    @Test
    public void testGetProblem() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String url = "http://localhost:8080/ai/problem?topic=hashmaps&difficulty=5";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("GET /ai/problem response:");
        System.out.println("Status: " + response.statusCode());
        System.out.println("Body: " + response.body());
    }

    // This method performs a POST request to /ai/submit and prints its status and body.
    @Test
    public void testSubmitSolution() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String url = "http://localhost:8080/ai/submit";

        String learningMaterialJson = Json.toJsonString(Json.fromJsonFile("src/test/resources/LLMTestFixedQuestion_32cd931e-784d-4ab8-be4a-c2cb6121d032.json", LearningMaterial.class));
        System.out.println("Learning Material: " + learningMaterialJson);

        String jsonPayload = String.format("""
        {
            "learningMaterial": %s,
            "solution": "System.out.println(\\"Hello World\\");",
            "language": "java",
            "studentId": "12345"
        }
        """, learningMaterialJson);

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());

        System.out.println("POST Response status: " + postResponse.statusCode());
        System.err.println("POST Response body 1: " + postResponse.body());


        jsonPayload = String.format("""
        {
            "learningMaterial": %s,
            "solution": "for(int i=1; i<=10; i++) System.out.println(i);",
            "language": "java",
            "studentId": "12345"
        }
        """, learningMaterialJson);

        postRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        System.err.println("POST Response body 2: " + postResponse.body());
    }
}