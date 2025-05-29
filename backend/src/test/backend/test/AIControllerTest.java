package backend.test;

import Classroom.LearningMaterial;
import Utils.Json;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
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

        LearningMaterial learningMaterialObject = Json.fromJsonFile("src/test/resources/LLMTestFixedQuestion_32cd931e-784d-4ab8-be4a-c2cb6121d032.json", LearningMaterial.class);
        String learningMaterialJson = Json.toJsonString(learningMaterialObject);
        System.out.println("Learning Material: " + learningMaterialJson);

        System.err.println("Number of submissions before: " + learningMaterialObject.getAssessmentItem().getSubmissions().size());

        String jsonPayload = String.format("""
        {
            "learningMaterial": %s,
            "solution": "System.out.println(5);",
            "studentId": 12345
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

        System.err.println("Number of submissions after: " + learningMaterialObject.getAssessmentItem().getSubmissions().size());

        jsonPayload = String.format("""
        {
            "learningMaterial": %s,
            "solution": "for(int i=1; i<=10; i++){System.out.println(i);}",
            "studentId": 12345
        }
        """, learningMaterialJson);

        postRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        System.err.println("POST Response body 2: " + postResponse.body());
        System.err.println("Number of submissions after after: " + learningMaterialObject.getAssessmentItem().getSubmissions().size());
    }

    @Test
    public void testSolveProblemDone() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        String url = "http://localhost:8080/ai/solve";

        LearningMaterial learningMaterialObject = Json.fromJsonFile("src/test/resources/LLMTestFixedQuestion_32cd931e-784d-4ab8-be4a-c2cb6121d032.json", LearningMaterial.class);
        String learningMaterialJson = Json.toJsonString(learningMaterialObject);

        String jsonPayload = String.format("""
        {
            "learningMaterial": %s,
            "language": "java",
            "studentId": 705123456
        }
        """, learningMaterialJson);

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        System.err.println("POST Response body: " + postResponse.body());
    }

    @Test
    public void testSolveProblemNotDone() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        String url = "http://localhost:8080/ai/solve";

        LearningMaterial learningMaterialObject = Json.fromJsonFile("src/test/resources/LLMTestFixedQuestion_32cd931e-784d-4ab8-be4a-c2cb6121d032.json", LearningMaterial.class);
        String learningMaterialJson = Json.toJsonString(learningMaterialObject);

        String jsonPayload = String.format("""
        {
            "learningMaterial": %s,
            "language": "java",
            "studentId": 123
        }
        """, learningMaterialJson);

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        System.err.println("POST Response body: " + postResponse.body());
        Assertions.assertEquals("You need to attempt the problem first.", postResponse.body());
    }

    @Test
    public void testDatabaseConnection() throws IOException, InterruptedException {
        /* GENERATE TWO PROBLEMS */
        HttpClient client = HttpClient.newHttpClient();
        String url = "http://localhost:8080/ai/problem?topic=hashmaps&difficulty=5";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> hashmapLM = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Response 1: " + hashmapLM.body());
        LearningMaterial learningMaterial1CLEAN = Json.fromJsonString(hashmapLM.body(), LearningMaterial.class);

        url = "http://localhost:8080/ai/problem?topic=arrays&difficulty=3";
        request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> arraysLM = client.send(request, HttpResponse.BodyHandlers.ofString());

        /* SUBMIT SOLUTIONS FOR EACH*/
        String jsonPayload1 = String.format("""
        {
            "learningMaterial": %s,
            "solution": "System.out.println(5);",
            "studentId": 705456789
        }
        """, hashmapLM.body());

        HttpRequest postRequest1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/ai/submit"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload1))
                .build();

        HttpResponse<String> postResponse1 = client.send(postRequest1, HttpResponse.BodyHandlers.ofString());

        System.out.println("POST Response status for first problem (1): " + postResponse1.statusCode());
        System.out.println("POST Response body for first problem (1): " + postResponse1.body());

        // Second submission
        String jsonPayload2 = String.format("""
        {
            "learningMaterial": %s,
            "solution": "for(int i=1; i<=10; i++){System.out.println(i);}",
            "studentId": 705123456
        }
        """, arraysLM.body());
        HttpRequest postRequest2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/ai/submit"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload2))
                .build();
        HttpResponse<String> postResponse2 = client.send(postRequest2, HttpResponse.BodyHandlers.ofString());
        System.out.println("POST Response status for first problem (2): " + postResponse2.statusCode());
        System.out.println("POST Response body for first problem (2): " + postResponse2.body());

        Assertions.assertNotEquals(learningMaterial1CLEAN, Json.fromJsonString(postResponse1.body(), LearningMaterial.class),
                "The learning material should not be equal to the one returned by the POST request, as it should have been updated with the submission.");

        Assertions.assertNotEquals(learningMaterial1CLEAN, Json.fromJsonString(postResponse2.body(), LearningMaterial.class),
                "The learning material should not be equal to the one returned by the second POST request, as it should have been updated with the submission.");

        // Second problem
        String jsonPayload3 = String.format("""
        {
            "learningMaterial": %s,
            "solution": "gerbinni",
            "studentId": 999999999
        }
        """, hashmapLM.body());

        HttpRequest postRequest3 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/ai/submit"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload3))
                .build();

        HttpResponse<String> postResponse3 = client.send(postRequest3, HttpResponse.BodyHandlers.ofString());

        System.out.println("POST Response status for second problem (1): " + postResponse3.statusCode());
        System.out.println("POST Response body for second problem (1): " + postResponse3.body());

        /* SOLVING ID DETECTION IN DB */
        // Has solved
        String jsonPayload4 = String.format("""
        {
            "learningMaterial": %s,
            "language": "java",
            "studentId": 705123456
        }
        """, arraysLM.body());
        HttpRequest postRequest4 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/ai/solve"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload4))
                .build();
        HttpResponse<String> postResponse4 = client.send(postRequest4, HttpResponse.BodyHandlers.ofString());
        System.out.println("POST Response status for checking attempt (did): " + postResponse4.statusCode());
        System.out.println("POST Response body for checking attempt (did): " + postResponse4.body());

        // Hasn't solved
        String jsonPayload5 = String.format("""
        {
            "learningMaterial": %s,
            "language": "java",
            "studentId": 632
        }
        """, arraysLM.body());
        HttpRequest postRequest5 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/ai/solve"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload5))
                .build();
        HttpResponse<String> postResponse5 = client.send(postRequest5, HttpResponse.BodyHandlers.ofString());
        System.out.println("POST Response status for checking attempt (didn't): " + postResponse5.statusCode());
        System.out.println("POST Response body for checking attempt (didn't): " + postResponse5.body());

    }
}