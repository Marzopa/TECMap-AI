package API;

import Classroom.LearningMaterial;
import Ollama.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@RequestMapping("/ai")
public class AIController {

    @GetMapping("/problem")
    public LearningMaterial getProblem(@RequestParam String topic, @RequestParam int difficulty)
            throws IOException, InterruptedException {

        System.out.println("Getting problem");

        return OllamaClient.generateLearningMaterialProblem(topic, difficulty);
    }

    /**
     * The method does NOT update submissions in LearningMaterial
     * Eventually should be made private, other method in this same class should call database
     * @param submission The submission request containing the LearningMaterial, solution, and language.
     * @return a GradingResponse object containing the feedback and score.
     * */
    @PostMapping("/submit")
    public GradingResponse submitSolution(@RequestBody SubmissionRequest submission)
            throws IOException, InterruptedException {

        return OllamaClient.solutionRequest(submission.getProblem(),
                submission.getSolution(),
                submission.getLanguage());
    }

}