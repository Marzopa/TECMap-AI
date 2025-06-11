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
//                new Scenario("arrays", 1, new String[]{}, new String[]{}),
//                new Scenario("strings", 1, new String[]{}, new String[]{"regex"}),
//                new Scenario("linked lists", 2, new String[]{"iteration"}, new String[]{"recursion"}),
//                new Scenario("hash maps", 2, new String[]{}, new String[]{"dynamic programming"}),
//                new Scenario("stacks & queues", 2, new String[]{}, new String[]{"graphs"}),
//                new Scenario("binary search", 3, new String[]{"recursion"}, new String[]{"graphs"}),
//                new Scenario("sorting algorithms", 3, new String[]{"selection sort"}, new String[]{"quick sort"}),
//                new Scenario("graphs", 4, new String[]{"BFS"}, new String[]{"DFS", "dynamic programming"}),
                new Scenario("dynamic programming", 4, new String[]{"memoization"}, new String[]{"graphs"}),
                new Scenario("arrays", 5, new String[]{"two-pointer"}, new String[]{"graphs", "binary trees"})
        );
    }

    @FunctionalInterface
    public interface QuadFunction<A, B, C, D, R> {
        R apply(A a, B b, C c, D d) throws IOException, InterruptedException;
    }

    @Test
    public void testWithNormal() throws IOException, InterruptedException {
        generateAllProblems(5, ollamaClient::generateLearningMaterialProblem);
    }

    @Test
    public void testWithCHASE() throws IOException, InterruptedException {
        generateAllProblems(1, ollamaClient::generateLearningMaterialCHASE);
    }

    public void generateAllProblems(int repeatPerScenario,
                                    QuadFunction<String, Integer, String[], String[], LearningMaterial> generator)
            throws IOException, InterruptedException {
        List<GenerationResult> results = new ArrayList<>();
        int total = scenarios().size() * repeatPerScenario;
        int count = 0;

        for (Scenario sc : scenarios()) {
            for (int i = 1; i <= repeatPerScenario; i++) {
                System.err.printf("Generating problem %d out of %d... \n", count, total);
                long start = System.nanoTime();
                LearningMaterial material = generator.apply(
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
                count++;
            }
        }

        saveResultsToJsonl(results, "src/test/batches/50.jsonl");
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
