package API;

import API.Dto.GradingResponse;
import API.Dto.LearningMaterialDto;
import API.Request.*;
import Classroom.AssessmentItem;
import Classroom.LearningMaterial;
import Ollama.*;
import OpenAI.OpenAIClient;
import Utils.LearningMaterialMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.logging.Logger;


@RestController
@RequestMapping("/ai")
public class AIController {

    private final OllamaClient ollamaClient;
    private final OpenAIClient openAIClient;
    private final DataController dataController;
    private static final Logger log = Logger.getLogger(AIController.class.getName());

    @FunctionalInterface
    public interface QuadFunction<A, B, C, D, R> {
        R apply(A a, B b, C c, D d) throws IOException, InterruptedException;
    }

    public AIController(OllamaClient ollamaClient, DataController dataController, OpenAIClient openAIClient) {
        this.ollamaClient = ollamaClient;
        this.dataController = dataController;
        this.openAIClient = openAIClient;
    }

    /**
     * This method is used to generate a problem based on the topic and difficulty level.
     * It checks if there is an unsolved matching problem in the database, and if not, it generates a new one.
     * The generated problem is then saved to the database.
     * @param problemRequest The request containing the topic, difficulty, additional topics, and excluded topics.
     * @return A ResponseEntity containing the generated LearningMaterialDto (with same attributes as LearningMaterial).
     */
    @PostMapping("/problem")
    public ResponseEntity<LearningMaterialDto> getProblem(@RequestBody ProblemRequest problemRequest)
            throws IOException, InterruptedException {
        log.info(String.format("Getting problem for %s (difficulty %d)", problemRequest.topic(), problemRequest.difficulty()));
        LearningMaterial existingProblem = dataController.unsolvedMatchingProblem(problemRequest);
        if (existingProblem != null) {
            log.info("Found unsolved matching problem in database: " + existingProblem.getUuid());
            return ResponseEntity.ok(LearningMaterialMapper.toDto(existingProblem));
        }

        QuadFunction<String, Integer, String[], String[], LearningMaterial> problemGenerator;

        switch(problemRequest.method()){
            case CHASE -> {
                log.info("Using Chase method to generate problem.");
                problemGenerator = ollamaClient::generateLearningMaterialCHASE;
            }
            case OPENAI -> {
                log.info("Using OpenAI method to generate problem.");
                problemGenerator = openAIClient::generateLearningMaterialProblem;
            }
            default -> {
                log.info("Using default method to generate problem.");
                problemGenerator = ollamaClient::generateLearningMaterialProblem;
            }
        }

        LearningMaterial generatedMaterial = problemGenerator.apply(
                problemRequest.topic(), problemRequest.difficulty(),
                problemRequest.additionalTopics(), problemRequest.excludedTopics()
        );

        // Set the title of the generated material to the topic
        int last = dataController.findLatestTitleOccurrence(problemRequest.topic());
        generatedMaterial.setTitle(problemRequest.topic() + " " + (last + 1));
        log.info("Generated title: " + generatedMaterial.getTitle());

        if (problemRequest.save()) dataController.save(generatedMaterial);

        return ResponseEntity.ok(LearningMaterialMapper.toDto(generatedMaterial));
    }

    /**
     * The method SHOULD update submissions in LearningMaterial from studentId in SubmissionRequest
     * @param submission The submission request containing the LearningMaterial, solution, and studentId.
     * @return a GradingResponse object containing the feedback and score.
     * */
    @PostMapping("/submit")
    public GradingResponse submitSolution(@RequestBody SubmissionRequest submission)
            throws IOException, InterruptedException {

        // Remove trailing numbers from the title to avoid model confusion
        String title = submission.learningMaterial().getTitle();
        if (title.matches(".*\\d+$")) title = title.replaceAll("\\d+$", "").trim();

        GradingResponse gradingResponse = ollamaClient.solutionRequest(submission.getProblem(),
                submission.solution(), title);

        // Update the LearningMaterial's assessment item with the new submission
        AssessmentItem problem = submission.learningMaterial().getAssessmentItem();
        problem.submitSolution(gradingResponse.grade(),
                submission.solution(),
                submission.studentId(),
                gradingResponse.feedback());

        // Update the LearningMaterial in the database with the new submission, only if it was already in the database
        if (dataController.exists(submission.learningMaterial().getUuid()))
            dataController.save(submission.learningMaterial());

        return gradingResponse;
    }

    /**
     * This method is used to solve a problem using the Ollama API.
     * @param request The request containing the LearningMaterial, studentId, and language.
     * TODO: this method should be hooked up to database to retrieve student concepts AND default desired language
     */
    @PostMapping("/solve")
    public String solveProblem(@RequestBody SolveRequest request)
            throws IOException, InterruptedException {
        log.info("Solving problem for student ID: " + request.studentId() + " in language: " + request.language());
        // If in database, check there. If not, check the LearningMaterial object in the request
        if(dataController.getLearningMaterial(request.learningMaterial().getUuid()).orElse(request.learningMaterial()).getAssessmentItem().hasStudentSubmitted(request.studentId()))
            return ollamaClient.problemSolverHelper(request.learningMaterial(), request.language());
        else return "You need to attempt the problem first.";
    }

    /**
     * This method is used to approve a problem by an instructor.
     * It checks if the instructor's credentials match, and if so, approves the problem.
     * @param request The request containing the instructor's "alleged" username, password, and problem ID.
     * @return A message indicating whether the approval was successful or not.
     */
    @PostMapping("/approve")
    public String approve(@RequestBody ApproveRequest request) {
        log.info("Logging in instructor: " + request.username());
        if(dataController.matches(request.username(), request.password())) {
            log.info("Instructor " + request.username() + " logged in successfully.");
            dataController.approveProblem(request.problemId());
            return String.format("Problem %s approved successfully by instructor %s.", request.problemId(), request.username());
        } else {
            log.warning("Instructor " + request.username() + " failed to log in.");
            return "Instructor login failed. Please check your credentials.";
        }
    }
}