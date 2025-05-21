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

        // Build form data in application/x-www-form-urlencoded format.
        String formData = "solution=solution&title=Arrays&content=Solve+this+problem&answerable=true";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("POST /ai/submit response:");
        System.out.println("Status: " + response.statusCode());
        System.out.println("Body: " + response.body());
    }
}