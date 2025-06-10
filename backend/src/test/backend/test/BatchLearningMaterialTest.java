package backend.test;

import Classroom.LearningMaterial;
import Ollama.OllamaClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

@SpringBootTest(classes = API.MicroserviceApp.class)
public class BatchLearningMaterialTest {

    @Autowired

    private OllamaClient ollamaClient;

    record Scenario(String topic, int difficulty, String[] additional, String[] excluded) {}

    record GenerationResult(
            String id,
            String topic,
            int difficulty,
            String[] additionalTopics,
            String[] excludedTopics,
            String content,
            List<String> tags,
            long latencyMs,
            Instant generatedAt
    ) {}

    static List<Scenario> scenarios() {
        return List.of(
                new Scenario("arrays", 1, new String[]{}, new String[]{}),
                new Scenario("graphs", 2, new String[]{"BFS", "recursion"}, new String[]{"DFS"}),
                new Scenario("hashmaps", 2, new String[]{}, new String[]{"recursion", "dynamic programming"}),
                new Scenario("linked lists", 2, new String[]{"recursion"}, new String[]{"dynamic programming"}),
                new Scenario("dynamic programming", 3, new String[]{"recursion"}, new String[]{"graphs"}),
                new Scenario("binary trees", 2, new String[]{"recursion"}, new String[]{"graphs", "dynamic programming"}),
                new Scenario("sorting algorithms", 4, new String[]{"recursion"}, new String[]{"graphs", "dynamic programming"}),
                new Scenario("binary search", 3, new String[]{"recursion"}, new String[]{"graphs", "dynamic programming"}),
                new Scenario("arrays", 5, new String[]{"dynamic programming"}, new String[]{"graphs", "binary trees"})
        );
    }

    @Test
    public void generateAllProblems() throws IOException, InterruptedException {
        int repeatPerScenario = 5;
        List<GenerationResult> results = new ArrayList<>();

        for (Scenario sc : scenarios()) {
            for (int i = 0; i < repeatPerScenario; i++) {
                long start = System.nanoTime();
                LearningMaterial material = ollamaClient.generateLearningMaterialProblem(
                        sc.topic(), sc.difficulty(), sc.additional(), sc.excluded());
                long latencyMs = (System.nanoTime() - start) / 1_000_000;

                GenerationResult result = new GenerationResult(
                        UUID.randomUUID().toString(),
                        sc.topic(),
                        sc.difficulty(),
                        sc.additional(),
                        sc.excluded(),
                        material.getContent(),
                        material.getTags(),
                        latencyMs,
                        Instant.now()
                );

                results.add(result);
                System.out.printf("Done [%s-%d] #%d (%d ms):\n%s\n\n",
                        sc.topic(), sc.difficulty(), i + 1, latencyMs, material.getContent());
            }
        }

        saveResultsToJsonl(results, "src/test/batches/generated-problems.jsonl");
    }

    private void saveResultsToJsonl(List<GenerationResult> results, String fileName) throws IOException {
        Path path = Path.of(fileName);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        try (BufferedWriter writer = Files.newBufferedWriter(path,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            for (GenerationResult r : results) {
                writer.write(mapper.writeValueAsString(r));
                writer.newLine();
            }
        }
        System.out.printf("Saved %d problems to %s%n", results.size(), path.toAbsolutePath());
    }
}
