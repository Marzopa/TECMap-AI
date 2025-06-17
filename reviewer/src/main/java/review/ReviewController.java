package review;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ReviewController {
    private final List<Problem> problems = new ArrayList<>();
    private final Path dataDir = Paths.get("data");
    private final Path reviewFile = Paths.get("reviews.csv");
    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);

    @PostConstruct
    public void loadProblems() throws IOException {
        log.info("Loading problems from directory: {}", dataDir);
        ObjectMapper om = new ObjectMapper();
        try (DirectoryStream<Path> files = Files.newDirectoryStream(dataDir, "*.jsonl")) {
            for (Path file : files) {
                log.info("Loading problems from file {}", file);
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
                        if (problems.size() % 10 == 0) log.info("{} problems loaded", problems.size());
                    }
                }
            }
        }
        Collections.shuffle(problems);
        log.info("Total problems loaded: {}", problems.size());
        if (!Files.exists(reviewFile)) Files.createFile(reviewFile);
    }

    @GetMapping({"/", "/review"})
    public String show(Model model) throws IOException {
        Set<String> reviewedIds = Files.lines(reviewFile)
                .map(line -> line.split(",", 2)[0])
                .collect(Collectors.toSet());

        for (int i = 0; i < problems.size(); i++) {
            Problem p = problems.get(i);
            if (!reviewedIds.contains(p.id())) {
                model.addAttribute("problem", p);
                model.addAttribute("idx", i);
                model.addAttribute("graded", reviewedIds.size());
                model.addAttribute("remaining", problems.size() - reviewedIds.size());
                return "review";
            }
        }

        return "done";
    }

    @PostMapping("/submit/{idx}")
    public String submit(@PathVariable int idx,
                         @RequestParam int clarity,
                         @RequestParam int depth,
                         @RequestParam int difficulty,
                         @RequestParam int verbosity,
                         @RequestParam String main,
                         @RequestParam String additional,
                         @RequestParam int overall,
                         @RequestParam(required = false) String comments) throws IOException {
        Problem p = problems.get(idx);
        additional = "\"" + additional + "\"";
        String line = String.join(",",
                p.id(), p.source(), String.valueOf(clarity), String.valueOf(depth),
                String.valueOf(difficulty), String.valueOf(verbosity),
                main.replace(",", ""), additional, String.valueOf(overall),
                (comments == null ? "" : comments.replace(",", "").replaceAll("[\\r\\n]+", " ").trim()));
        Files.writeString(reviewFile, line + System.lineSeparator(), StandardOpenOption.APPEND);
        return "redirect:/review";
    }
}
