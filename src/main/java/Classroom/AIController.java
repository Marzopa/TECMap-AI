package Classroom;

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
     * @param learningMaterial The ID of the student.
     * @param solution The password of the student.
     * @return A LearningMaterial object containing the problem.
     */
    @PostMapping("/submit")
    public GradingResponse submitSolution(LearningMaterial learningMaterial,
                                             @RequestParam String solution)
            throws IOException, InterruptedException {

        String problem = learningMaterial.getContent();

        return OllamaClient.solutionRequest(problem, solution);
    }

}