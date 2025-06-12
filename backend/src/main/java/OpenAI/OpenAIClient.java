package OpenAI;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;

@Service
public class OpenAIClient {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Logger log = LoggerFactory.getLogger(OpenAIClient.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    public enum Model {
        GPT_4_1("gpt-4.1-2025-04-14");
        private final String modelName;
        Model(String modelName) {
            this.modelName = modelName;
        }
        public String getModelName() {
            return modelName;
        }
    }

    /**
     * Sends a prompt to the OpenAI chat completions endpoint using the specified model.
     * @param model e.g., "gpt-4", "gpt-3.5-turbo"
     * @param content a user message (the prompt)
     * @param temperature controls randomness in the response (0.0 = deterministic, 1.0 = more random)
     * @param max_completion_tokens maximum number of tokens in the response
     * @return the assistant’s reply as plain text
     */
    public static String openAIRequest(Model model, String developer, String content,
                                       float temperature, int max_completion_tokens, float top_p
    ) throws IOException, InterruptedException {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null) throw new IllegalStateException("Missing OPENAI_API_KEY environment variable");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(buildRequestJson(model.modelName, developer, content,
                        temperature, max_completion_tokens, top_p)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200)
            throw new IOException("OpenAI service error: " + response.body());

        return parseResponse(response.body());
    }

    private static String buildRequestJson(String model, String developer, String content,
                                           float temperature, int max_completion_tokens, float top_p) {
        ObjectNode root = mapper.createObjectNode();
        root.put("model", model);
        ArrayNode messages = root.putArray("messages");

        ObjectNode systemMsg = messages.addObject();
        systemMsg.put("role", "developer");
        systemMsg.put("content", developer);

        ObjectNode userMsg = messages.addObject();
        userMsg.put("role", "user");
        userMsg.put("content", content);

        root.put("temperature", temperature);
        root.put("max_completion_tokens", max_completion_tokens);
        root.put("top_p", top_p);

        try {
            return mapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing OpenAI request", e);
        }
    }

    private static String parseResponse(String json) throws JsonProcessingException {
        JsonNode root = mapper.readTree(json);
        return root.path("choices").get(0).path("message").path("content").asText();
    }
}
