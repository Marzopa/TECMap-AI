package OpenAI;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InterfaceOpenAI {

    private static final String MODELFILE_DIR = "src/main/resources/Modelfiles/";

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
            String raw = Files.readString(Path.of(MODELFILE_DIR + fileName), StandardCharsets.UTF_8);

            String systemPrompt = extractSystemPrompt(raw);
            Float temperature = extractFloatParam(raw, "temperature");
            Float topP = extractFloatParam(raw, "top_p");
            Integer maxTok = extractIntParam(raw, "num_predict");

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

    private static String extractSystemPrompt(String text) {
        String startToken = "SYSTEM \"\"\"";
        int start = text.indexOf(startToken);
        if (start == -1) return "";
        start += startToken.length();
        int end = text.indexOf("\"\"\"", start);
        if (end == -1) end = text.length();
        return text.substring(start, end).trim();
    }

    private static Float extractFloatParam(String text, String name) {
        Pattern p = Pattern.compile("PARAMETER\\s+" + name + "\\s+([0-9]*\\.?[0-9]+)");
        Matcher m = p.matcher(text);
        return m.find() ? Float.parseFloat(m.group(1)) : null;
    }

    private static Integer extractIntParam(String text, String name) {
        Pattern p = Pattern.compile("PARAMETER\\s+" + name + "\\s+([0-9]+)");
        Matcher m = p.matcher(text);
        return m.find() ? Integer.parseInt(m.group(1)) : null;
    }

    private InterfaceOpenAI() {

    }
}
