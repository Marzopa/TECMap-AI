package OpenAI;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class InterfaceOpenAI {

    private static final String MODEL_DIR = "src/main/resources/Modelfiles/";

    public static final Map<String, OpenAIClient.ChatRequest> MODELS = new HashMap<>();

    static {
        register("cs-feedbackGenerator.txt", "cs-feedbackGenerator");
        register("cs-problemGenerator.txt", "cs-problemGenerator");
        register("cs-problemGrader.txt", "cs-problemGrader");
        register("cs-problemSolver.txt", "cs-problemSolver");
        register("cs-smallProblemSolver.txt", "cs-smallProblemSolver");
        register("cs-syntaxChecker.txt", "cs-syntaxChecker");
        register("cs-topicScanner.txt", "cs-topicScanner");
    }

    private static void register(String fileName, String key) {
        try {
            String raw = Files.readString(Path.of(MODEL_DIR + fileName), StandardCharsets.UTF_8);
            String systemPrompt = extractSystem(raw);
            Float temperature = extractFloat(raw, "temperature");
            Float topP = extractFloat(raw, "top_p");
            Integer maxTok = extractInt(raw, "num_predict");

            OpenAIClient.ChatRequest req = new OpenAIClient.ChatRequest("")
                    .model(OpenAIClient.Model.GPT_4_1)
                    .systemPrompt(systemPrompt);
            if (temperature != null) req.temperature(temperature);
            if (topP != null) req.topP(topP);
            if (maxTok != null) req.maxTokens(maxTok);

            MODELS.put(key, req);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load modelfile: " + fileName, e);
        }
    }

    private static String extractSystem(String text) {
        String start = "SYSTEM \"""";
        int s = text.indexOf(start);
        if (s == -1) return "";
        s += start.length();
        int e = text.indexOf("\"""", s);
        return e == -1 ? text.substring(s).trim() : text.substring(s, e).trim();
    }

    private static Float extractFloat(String text, String param) {
        String marker = "PARAMETER " + param + " ";
        int idx = text.indexOf(marker);
        if (idx == -1) return null;
        int end = text.indexOf('\n', idx);
        String num = (end == -1 ? text.substring(idx + marker.length()) : text.substring(idx + marker.length(), end)).trim();
        try { return Float.parseFloat(num); } catch (NumberFormatException e) { return null; }
    }

    private static Integer extractInt(String text, String param) {
        String marker = "PARAMETER " + param + " ";
        int idx = text.indexOf(marker);
        if (idx == -1) return null;
        int end = text.indexOf('\n', idx);
        String num = (end == -1 ? text.substring(idx + marker.length()) : text.substring(idx + marker.length(), end)).trim();
        try { return Integer.parseInt(num); } catch (NumberFormatException e) { return null; }
    }
}
