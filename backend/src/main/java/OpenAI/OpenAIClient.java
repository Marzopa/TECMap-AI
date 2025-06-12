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
import java.util.Optional;

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
     * Sends a chat request to the OpenAI API using parameters set in ChatRequest.
     * It ignores the model if it is not set, and uses the default model GPT_4_1.
     */
    public String openAIRequest(ChatRequest req) throws IOException, InterruptedException {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null) throw new IllegalStateException("Missing OPENAI_API_KEY environment variable");

        ObjectNode root = mapper.createObjectNode();
        root.put("model", req.model.getModelName());

        ArrayNode messages = root.putArray("messages");
        ObjectNode systemMsg = messages.addObject();
        systemMsg.put("role", "developer");
        systemMsg.put("content", req.systemPrompt);

        ObjectNode userMsg = messages.addObject();
        userMsg.put("role", "user");
        userMsg.put("content", req.userMessage);

        req.temperature.ifPresent(t -> root.put("temperature", t));
        req.topP.ifPresent(tp -> root.put("top_p", tp));
        req.maxTokens.ifPresent(mt -> root.put("max_completion_tokens", mt));

        log.info("Sending OpenAI request: {}", root.toPrettyString());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(root)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new IOException("OpenAI service error: " + response.body());
        return parseResponse(response.body());
    }

    private static String parseResponse(String json) throws JsonProcessingException {
        log.info("Parsing response: {}", json);
        JsonNode root = mapper.readTree(json);
        return root.path("choices").get(0).path("message").path("content").asText();
    }

    /**
     * Represents a chat request to the OpenAI API.
     * To use this class, create an instance with the user message and optionally set the model, system prompt, temperature, top_p, and max_tokens.
     * It includes the model, system prompt, user message, and optional parameters like temperature, top_p, and max_tokens.
     */
    public static class ChatRequest {
        private Model model = Model.GPT_4_1;
        private String systemPrompt = "You are a helpful assistant.";
        private String userMessage;
        private Optional<Float> temperature = Optional.empty();
        private Optional<Float> topP = Optional.empty();
        private Optional<Integer> maxTokens = Optional.empty();

        public ChatRequest(String userMessage) {
            this.userMessage = userMessage;
        }

        public ChatRequest userMessage(String userMessage) {
            this.userMessage = userMessage;
            return this;
        }

        public ChatRequest model(Model model) {
            this.model = model;
            return this;
        }

        public ChatRequest systemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
            return this;
        }

        public ChatRequest temperature(float temperature) {
            this.temperature = Optional.of(temperature);
            return this;
        }

        public ChatRequest topP(float topP) {
            this.topP = Optional.of(topP);
            return this;
        }

        public ChatRequest maxTokens(int maxTokens) {
            this.maxTokens = Optional.of(maxTokens);
            return this;
        }

        @Override
        public String toString() {
            return "ChatRequest{" +
                    "model=" + model +
                    ", systemPrompt='" + systemPrompt + '\'' +
                    ", userMessage='" + userMessage + '\'' +
                    ", temperature=" + temperature +
                    ", topP=" + topP +
                    ", maxTokens=" + maxTokens +
                    '}';
        }
    }
}
