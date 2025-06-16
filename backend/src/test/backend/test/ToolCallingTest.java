package test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import Ollama.OllamaClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

@SpringBootTest(classes = API.MicroserviceApp.class)
public class ToolCallingTest {
    @Autowired
    private OllamaClient ollamaClient;

    static final ObjectMapper MAPPER = new ObjectMapper();
    static final String MODEL = "Verifier";

    @Test
    void testRawToolCalling() throws Exception {

        Map<String,Object> runCodeTool = Map.of(
                "type", "function",
                "function", Map.of(
                        "name", "run_code",
                        "description", "Compile & execute candidate code against tests",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "language", Map.of("type","string","enum", List.of("python")),
                                        "code",     Map.of("type","string"),
                                        "tests", Map.of(
                                                "type","array",
                                                "items", Map.of(
                                                        "type","object",
                                                        "properties", Map.of(
                                                                "input",  Map.of("type","string"),
                                                                "output", Map.of("type","string")
                                                        ),
                                                        "required", List.of("input","output")
                                                )
                                        )
                                ),
                                "required", List.of("language","code","tests")
                        )
                )
        );

        // SYSTEM prompt to force tool use
        List<Map<String,Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content",
                "You are a strict grader. Use the run_code tool to test answers."));

        messages.add(Map.of("role","user",
                "content", String.join("\n",
                        "TOPIC: simple math",
                        "DIFFICULTY: 1",
                        "ADDITIONAL:",
                        "EXCLUDED:",
                        "CONTEXT:",
                        "QUESTION: Write a function that squares an integer.",
                        "ANSWER:\n```python\ndef square(x):\n    return x * x\n```"
                )));

        JsonNode first = ollamaClient.chatWithTools(MODEL, messages, List.of(runCodeTool));

        // model MUST ask to call run_code
        JsonNode toolCalls = first.at("/choices/0/message/tool_calls");
        assert toolCalls.isArray() && toolCalls.size() == 1;

        String arguments = toolCalls.get(0).at("/function/arguments").asText();

        Map<String,Object> fakeResult = Map.of(
                "allPass", true,
                "details", List.of(
                        Map.of("input","2","expected","4","actual","4","ok",true),
                        Map.of("input","3","expected","9","actual","9","ok",true)
                )
        );
        messages.add(Map.of("role","assistant",
                "tool_calls", MAPPER.readValue(toolCalls.toString(), List.class)));
        messages.add(Map.of("role","tool",
                "name","run_code",
                "content", MAPPER.writeValueAsString(fakeResult)));
        messages.add(Map.of("role","assistant","content",""));

        JsonNode second = ollamaClient.chatWithTools(MODEL, messages, null);
        String verdict = second.at("/choices/0/message/content").asText();
        System.out.println("Verifier said: " + verdict);

        assert verdict.startsWith("VERIFIED");
    }
}
