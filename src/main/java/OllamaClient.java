import java.io.IOException;
import java.net.http.*;
import java.net.URI;
import java.net.http.HttpResponse.BodyHandlers;

public class OllamaClient {
    public static String problemRequest(String topic, int difficulty) throws IOException, InterruptedException {
        String systemPrompt = "For the following topic and difficulty (on a scale of 1 to 5), " +
                "generate an original coding problem, language agnostic." +
                "For example: example input: 'arrays 3'" +
                "example output: 'Write a function that takes an array of integers and returns the sum of all even numbers.'" +
                "Please provide a similar output for the input below, and do not include any additional text.";

        String json = String.format("""
        {
        "model": "llama3.1:8b",
            "messages": [
                { "role": "user", "content": "%s %s %d"}
            ]
        }
        """, systemPrompt, topic, difficulty);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/chat"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        return response.body();
    }

    public static int solutionRequest(String problem, String solution) throws IOException, InterruptedException {
        return solutionRequest(problem, solution, "python");
    }

    public static int solutionRequest(String problem, String solution, String language) throws IOException, InterruptedException {
        String systemPrompt = "For the following solution of the problem in the selected language, " +
                "grade the input on a scale from 0 (completely incorrect) to 100 (completely correct)." +
                "For example: example input: 'problem: Write a function that takes an array of integers and returns the sum of all even numbers. " +
                "solution: def sum_evens(arr): return sum(x for x in arr if x % 2 == 0) language: python" +
                "example output: 100" +
                "Another example input: 'problem: Write a function that takes an array of integers and returns the sum of all even numbers. " +
                "solution: def sum_evens(arr): return sum(x for x in arr if x % 2 == 1) language: python" +
                "example output: 60" +
                "Please provide a similar output for the input below (just ), and do not include any additional text." +
                "Be lenient with the grading. Don't worry about minor syntax errors OR indentation, but do consider the overall correctness of the solution." +
                "If the solution is completely incorrect, please provide a grade of 0. If the solution is completely correct, please provide a grade of 100.";

        String json = String.format("""
        {
        "model": "llama3.1:8b",
            "messages": [
                { "role": "user", "content": "%s problem: %s solution: %s language: %s"}
            ]
        }
        """, systemPrompt, problem, solution, language);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/chat"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        //System.err.println(response.body());
        String actual_number = OllamaResponseParser.parseResponse(response.body());
        return Integer.parseInt(actual_number);
    }

    public static void main(String[] args) throws Exception {

//        String dict_problem = problemRequest("dictionaries", 3);
//        System.out.println("Response:\n" + OllamaResponseParser.parseResponse(dict_problem));
//        System.out.println("Grade:\n" + solutionRequest(dict_problem, "i dont know it"));
        System.out.println("Grade:\n" + solutionRequest("Write a simple for loop that prints the numbers from 1 to 10.",
                "for(int i=1; i<11; i++) { System.out.println(i); }", "java"));
    }
}
