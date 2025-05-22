package API;

import Classroom.AssessmentItem;
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
     * The method SHOULD update submissions in LearningMaterial from studentId in SubmissionRequest
     * TODO: once database is set up, this should receive the uuid of the LearningMaterial to update it in there
     * @param submission The submission request containing the LearningMaterial, solution, and language.
     * @return a GradingResponse object containing the feedback and score.
     * */
    @PostMapping("/submit")
    public GradingResponse submitSolution(@RequestBody SubmissionRequest submission)
            throws IOException, InterruptedException {

        GradingResponse gradingResponse =  OllamaClient.solutionRequest(submission.getProblem(),
                submission.getSolution());

        // Update the LearningMaterial's assessment item with the new submission
        AssessmentItem problem = submission.getLearningMaterial().getAssessmentItem();
        problem.submitSolution(gradingResponse.grade(),
                submission.getSolution(),
                submission.getStudentId(),
                gradingResponse.feedback());

        return gradingResponse;
    }

    @PostMapping("/solve")
    public String solveProblem(@RequestBody LearningMaterial learningMaterial)
            throws IOException, InterruptedException {
        return OllamaClient.problemSolverHelper(learningMaterial);
    }

}