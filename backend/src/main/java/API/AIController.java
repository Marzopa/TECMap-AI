package API;

import Classroom.AssessmentItem;
import Classroom.LearningMaterial;
import Ollama.*;
import Repo.LearningMaterialRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;


@RestController
@RequestMapping("/ai")
public class AIController {

    private final OllamaClient ollamaClient;
    private final DataController dataController;
    private static final Logger log = Logger.getLogger(AIController.class.getName());

    @Autowired
    private LearningMaterialRepo learningMaterialRepo;

    public AIController(OllamaClient ollamaClient, DataController dataController) {
        this.ollamaClient = ollamaClient;
        this.dataController = dataController;
    }

    public record LearningMaterialDto(
            String uuid,
            String title,
            String content,
            boolean answerable,
            boolean approved
    ) {}

    @PostMapping("/problem")
    public ResponseEntity<LearningMaterialDto> getProblem(@RequestBody ProblemRequest problemRequest)
            throws IOException, InterruptedException {
        log.info(String.format("Getting problem for %s (%d)", problemRequest.topic(), problemRequest.difficulty()));
        LearningMaterial existingProblem = dataController.unsolvedMatchingProblem(problemRequest);
        if (existingProblem != null) {
            log.info("Found unsolved matching problem in database: " + existingProblem.getUuid());
            LearningMaterialDto dto = new LearningMaterialDto(
                    existingProblem.getUuid(),
                    existingProblem.getTitle(),
                    existingProblem.getContent(),
                    existingProblem.isAnswerable(),
                    existingProblem.isApproved()
            );
            return ResponseEntity.ok(dto);
        }
        LearningMaterial generatedMaterial = ollamaClient.generateLearningMaterialProblem(
                problemRequest.topic(),
                problemRequest.difficulty(),
                problemRequest.additionalTopics(),
                problemRequest.excludedTopics()
        );
        learningMaterialRepo.save(generatedMaterial);
        LearningMaterialDto dto = new LearningMaterialDto(
                generatedMaterial.getUuid(),
                generatedMaterial.getTitle(),
                generatedMaterial.getContent(),
                generatedMaterial.isAnswerable(),
                generatedMaterial.isApproved()
        );
        return ResponseEntity.ok(dto);
    }

    /**
     * The method SHOULD update submissions in LearningMaterial from studentId in SubmissionRequest
     * @param submission The submission request containing the LearningMaterial, solution, and studentId.
     * @return a GradingResponse object containing the feedback and score.
     * */
    @PostMapping("/submit")
    public GradingResponse submitSolution(@RequestBody SubmissionRequest submission)
            throws IOException, InterruptedException {

        GradingResponse gradingResponse = ollamaClient.solutionRequest(submission.getProblem(),
                submission.solution(), submission.learningMaterial().getTitle());

        // Update the LearningMaterial's assessment item with the new submission
        AssessmentItem problem = submission.learningMaterial().getAssessmentItem();
        problem.submitSolution(gradingResponse.grade(),
                submission.solution(),
                submission.studentId(),
                gradingResponse.feedback());

        // Update the LearningMaterial in the database with the new submission, only if it was already in the database
        if (learningMaterialRepo.existsById(submission.learningMaterial().getUuid()))
            learningMaterialRepo.save(submission.learningMaterial());

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
        // If in database, check there
        // If not, check the LearningMaterial object in the request
        if(learningMaterialRepo.findById(request.learningMaterial().getUuid()).orElse(request.learningMaterial()).getAssessmentItem().hasStudentSubmitted(request.studentId()))
            return ollamaClient.problemSolverHelper(request.learningMaterial(), request.language());
        else return "You need to attempt the problem first.";
    }

}