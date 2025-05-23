package API;

import Classroom.AssessmentItem;
import Classroom.LearningMaterial;
import Ollama.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.logging.Logger;


@RestController
@RequestMapping("/ai")
public class AIController {

    private static final Logger log = Logger.getLogger(AIController.class.getName());

    @GetMapping("/problem")
    public LearningMaterial getProblem(@RequestParam String topic, @RequestParam int difficulty)
            throws IOException, InterruptedException {
        log.info("Getting problem");
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

    /**
     * This method is used to solve a problem using the Ollama API.
     * @param learningMaterial The learning material object containing the problem to be solved.
     * @param studentId The integer ID of the student attempting to solve the problem.
     * @param language The programming language to be used for solving the problem.
     * TODO: this method should be hooked up to database to retrieve student concepts AND desired language
     */
    @PostMapping("/solve")
    public String solveProblem(@RequestBody LearningMaterial learningMaterial,
                               @RequestParam int studentId,
                               @RequestParam(required = false, defaultValue = "java") String language)
            throws IOException, InterruptedException {
        if(learningMaterial.getAssessmentItem().hasStudentSubmitted(studentId)) return OllamaClient.problemSolverHelper(learningMaterial, language);
        else return "You need to attempt the problem first.";
    }

}