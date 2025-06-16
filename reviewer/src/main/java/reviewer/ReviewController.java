package reviewer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.file.*;
import java.util.*;

@Controller
public class ReviewController {
    private final List<Problem> problems = new ArrayList<>();
    private final Path dataDir = Paths.get("data");
    private final Path reviewFile = Paths.get("reviews.csv");

    @PostConstruct
    public void loadProblems() throws IOException {
        ObjectMapper om = new ObjectMapper();
        try (DirectoryStream<Path> files = Files.newDirectoryStream(dataDir, "*.jsonl")) {
            for (Path file : files) {
                try (BufferedReader br = Files.newBufferedReader(file)) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        JsonNode node = om.readTree(line);
                        problems.add(new Problem(
                                UUID.randomUUID().toString(),
                                node.path("content").asText(),
                                node.path("topic").asText(),
                                node.path("difficulty").asInt(0),
                                file.getFileName().toString()
                        ));
                    }
                }
            }
        }
        Collections.shuffle(problems);
        if (!Files.exists(reviewFile)) Files.createFile(reviewFile);
    }

    @GetMapping({"/", "/review/{idx}"})
    public String show(@PathVariable(required = false) Integer idx, Model model) {
        int i = (idx == null) ? 0 : idx;
        if (i >= problems.size()) return "done";
        model.addAttribute("problem", problems.get(i));
        model.addAttribute("idx", i);
        model.addAttribute("next", i + 1);
        model.addAttribute("total", problems.size());
        return "review";
    }

    @PostMapping("/submit/{idx}")
    public String submit(@PathVariable int idx,
                         @RequestParam int rating,
                         @RequestParam(required = false) String comments) throws IOException {
        Problem p = problems.get(idx);
        String line = String.join(",",
                p.id(), p.source(), String.valueOf(rating),
                (comments == null ? "" : comments.replaceAll("[\\r\\n]+", " ").trim()));
        Files.writeString(reviewFile, line + System.lineSeparator(), StandardOpenOption.APPEND);
        return "redirect:/review/" + (idx + 1);
    }
}
