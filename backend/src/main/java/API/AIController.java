package API;

import Classroom.AssessmentItem;
import Classroom.LearningMaterial;
import Ollama.*;
import Repo.LearningMaterialRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.logging.Logger;


@RestController
@RequestMapping("/ai")
public class AIController {

    private final OllamaClient ollamaClient;
    private static final Logger log = Logger.getLogger(AIController.class.getName());

    @Autowired
    private LearningMaterialRepo learningMaterialRepo;

    public AIController(OllamaClient ollamaClient) {
        this.ollamaClient = ollamaClient;
    }

    @GetMapping("/problem")
    public LearningMaterial getProblem(@RequestParam String topic, @RequestParam int difficulty)
            throws IOException, InterruptedException {
        log.info("Getting problem");
        return ollamaClient.generateLearningMaterialProblem(topic, difficulty);
    }

    /**
     * The method SHOULD update submissions in LearningMaterial from studentId in SubmissionRequest
     * TODO: once database is set up, this should receive the uuid of the LearningMaterial to update it in there
     * @param submission The submission request containing the LearningMaterial, solution, and studentId.
     * @return a GradingResponse object containing the feedback and score.
     * */
    @PostMapping("/submit")
    public GradingResponse submitSolution(@RequestBody SubmissionRequest submission)
            throws IOException, InterruptedException {

        GradingResponse gradingResponse = ollamaClient.solutionRequest(submission.getProblem(),
                submission.solution());

        // Update the LearningMaterial's assessment item with the new submission
        AssessmentItem problem = submission.learningMaterial().getAssessmentItem();
        problem.submitSolution(gradingResponse.grade(),
                submission.solution(),
                submission.studentId(),
                gradingResponse.feedback());

        return gradingResponse;
    }

    /**
     * This method is used to solve a problem using the Ollama API.
     * @param request The request request containing the LearningMaterial, studentId, and language.
     * TODO: this method should be hooked up to database to retrieve student concepts AND default desired language
     */
    @PostMapping("/solve")
    public String solveProblem(@RequestBody SolveRequest request)
            throws IOException, InterruptedException {
        log.info("Solving problem for student ID: " + request.studentId() + " in language: " + request.language());
        //if(request.learningMaterial().getAssessmentItem().hasStudentSubmitted(request.studentId()))
            return ollamaClient.problemSolverHelper(request.learningMaterial(), request.language());
        //else return "You need to attempt the problem first.";
    }

}