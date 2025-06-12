package OpenAI;

import Classroom.AssessmentItem;
import Classroom.LearningMaterial;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class OpenAIClient {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Logger log = LoggerFactory.getLogger(OpenAIClient.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    public enum Model {
        GPT_4_1("gpt-4.1-2025-04-14"),
        GPT_4_1_NANO("gpt-4.1-nano-2025-04-14"),
        GPT_4_1_MINI("gpt-4.1-mini-2025-04-14");
        private final String modelName;
        Model(String modelName) {
            this.modelName = modelName;
        }
        public String getModelName() {
            return modelName;
        }
    }

    /**
     * @param modelfile valid name for the model file, e.g. "cs-problemGenerator.txt"
     * @param userMessage the message to send to the OpenAI API
     * @return a string containing only the response from the OpenAI API
     * @throws IllegalArgumentException if the modelfile is not found in InterfaceOpenAI.MODELS
     * @throws IOException if there is an error sending the request or parsing the response
     * @throws InterruptedException if the request is interrupted
     */
    public String openAIRequest(String modelfile, String userMessage) throws IllegalArgumentException,
            IOException, InterruptedException {
        ChatRequest chatRequest = InterfaceOpenAI.MODELS.get(modelfile);
        if (chatRequest == null) throw new IllegalArgumentException("Model file not found: " + modelfile);
        return openAIRequest(chatRequest.userMessage(userMessage));
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
     * Generates a learning material problem based on the given topic, difficulty, additional topics, and excluded topics.
     * This method uses the OpenAI API to generate a problem and returns a LearningMaterial object.
     * @param topic The main topic for the problem.
     * @param difficulty The difficulty level of the problem.
     * @param additionalTopics Additional topics to consider in the problem generation.
     * @param excludedTopics Topics to exclude from the problem generation.
     * @return A LearningMaterial object containing the generated problem.
     */
    public LearningMaterial generateLearningMaterialProblem(String topic, int difficulty, String[] additionalTopics, String[] excludedTopics) throws IOException, InterruptedException {
        String problem = problemRequest(topic, difficulty, additionalTopics, excludedTopics);
        log.info("Open AI Generated problem: {}", problem);
        LearningMaterial learningMaterial = new LearningMaterial(topic, problem, true);
        AssessmentItem assessmentItem = new AssessmentItem();
        learningMaterial.setAssessmentItem(assessmentItem);
        String solution = problemSolverHelper(learningMaterial, "java");
        log.info("Open AI Generated solution: {}", solution);
        List<String> scanTopics = List.of(scanTopics(learningMaterial.getContent(), solution));
        learningMaterial.setTags(scanTopics);
        return learningMaterial;
    }

    private String problemSolverHelper(LearningMaterial learningMaterial, String language) throws IOException, InterruptedException {
        String content = "problem: " + learningMaterial.getContent() + "language: " + language;
        ChatRequest chatRequest = InterfaceOpenAI.MODELS.get("cs-smallProblemSolver")
                .model(Model.GPT_4_1_NANO).userMessage(content);
        return openAIRequest(chatRequest);
    }

    public String[] scanTopics(String problem, String solution) throws IOException, InterruptedException {
        return scanTopics(String.format("problem: %s ~~~ solution: %s", problem, solution));
    }

    public String[] scanTopics(String requestString) throws IOException, InterruptedException {
        ChatRequest chatRequest = InterfaceOpenAI.MODELS.get("cs-topicScanner")
                .model(Model.GPT_4_1_NANO).userMessage(requestString);
        String response = openAIRequest(chatRequest);

        log.info("Open AI Parsed topics response: {}", response);
        String[] scanTopics = response.replace("\n", "").split(", ");
        scanTopics = Arrays.stream(scanTopics).map(String::trim).toArray(String[]::new);
        if (scanTopics[scanTopics.length - 1].equalsIgnoreCase("etc.")) scanTopics = java.util.Arrays.copyOf(scanTopics, scanTopics.length - 1);
        log.info("Open AI Scanned topics: {}", (Object) scanTopics);
        return scanTopics;
    }

    private String problemRequest(String topic, int difficulty, String[] additionalTopics, String[] excludedTopics) throws IOException, InterruptedException {
        return openAIRequest("cs-problemGenerator",
                String.format("topic: %s ~~~ difficulty: %d ~~~ additionalTopics: %s ~~~ excludedTopics: %s",
                        topic, difficulty,
                        String.join(", ", additionalTopics),
                        String.join(", ", excludedTopics)));
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
