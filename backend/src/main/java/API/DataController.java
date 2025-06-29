package API;

import API.Request.ProblemRequest;
import Classroom.Instructor;
import Classroom.LearningMaterial;
import Repo.InstructorRepo;
import Repo.LearningMaterialRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;

@Service
public class DataController {
    @Autowired
    private LearningMaterialRepo learningMaterialRepo;

    @Autowired
    private InstructorRepo instructorRepo;

    private static final Logger log = LoggerFactory.getLogger(DataController.class);

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public record MatchingLM(LearningMaterial learningMaterial, int matchingTopics) {
    }

    /**
     * This method retrieves an unsolved matching problem from the database.
     * It checks for a LearningMaterial that is answerable, has an assessment item and excludes those with excluded topics.
     * If such a LearningMaterial exists, it returns it; otherwise, it returns null.
     * It prioritizes approved problems, then those that also contain additional topics.
     *
     * @param request The ProblemRequest object containing the criteria for the matching problem.
     * @return a LearningMaterial object representing the unsolved matching problem or null if none exists.
     */
    public LearningMaterial unsolvedMatchingProblem(ProblemRequest request) {
        log.info("Searching for unsolved matching problem for topic: " + request.topic());
        List<LearningMaterial> approvedMaterials = learningMaterialRepo.findByApproved(true);
        log.info("Found " + approvedMaterials.size() + " approved problems");
        List<MatchingLM> sortedMaterials = new LinkedList<>();

        for (LearningMaterial material : approvedMaterials) {
            String[] words = material.getTitle().split(" ");
            String topic = String.join(" ", Arrays.copyOf(words, words.length - 1));
            if (material.isAnswerable() && material.getAssessmentItem() != null && topic.equals(request.topic())) {
                String[] materialTopics = material.getTags().toArray(new String[0]);
                int matchingTopics = countIntersection(materialTopics, request.additionalTopics());
                // Exclude materials that have topics in the excludedTopics list
                boolean hasExcludedTopics = Arrays.stream(request.excludedTopics())
                        .anyMatch(excludedTopic -> Arrays.asList(materialTopics).contains(excludedTopic));
                boolean hasSubmittedSolution = material.getAssessmentItem().hasStudentSubmitted(request.studentId());
                if (!hasExcludedTopics && !hasSubmittedSolution)
                    sortedMaterials.add(new MatchingLM(material, matchingTopics));
            }
        }

        sortedMaterials.sort(Comparator.comparingInt(MatchingLM::matchingTopics).reversed());
        return sortedMaterials.isEmpty() ? null : sortedMaterials.get(0).learningMaterial();
    }

    public static int countIntersection(String[] a, String[] b) {
        Set<String> setA = new HashSet<>(List.of(a));
        Set<String> setB = new HashSet<>(List.of(b));
        setA.retainAll(setB);
        return setA.size();
    }

    /**
     * This method saves a LearningMaterial to the database.
     * @param learningMaterial the LearningMaterial object to save
     * @return whether the LearningMaterial already existed in the database (update) or not (insert).
     */
    public boolean save(LearningMaterial learningMaterial) {
        boolean exists = learningMaterialRepo.existsById(learningMaterial.getUuid());
        learningMaterialRepo.save(learningMaterial);
        return exists;
    }

    public boolean exists(String uuid) {
        return learningMaterialRepo.existsById(uuid);
    }

    public Optional<LearningMaterial> getLearningMaterial(String uuid) {
        return learningMaterialRepo.findById(uuid);
    }

    /**
     * This method returns the latest occurrence of a title in the LearningMaterial repository.
     * @param title The prefix of the title to search for.
     * @return the latest occurrence number of the title, or 0 if no occurrences are found.
     */
    public int findLatestTitleOccurrence(String title) {
        List<LearningMaterial> materials = learningMaterialRepo.findByTitleStartingWith(title);
        int max = 0;
        for (LearningMaterial material : materials) {
            String[] parts = material.getTitle().split(" ");
            if (parts.length > 1 && parts[0].equals(title)) {
                int occurrence = Integer.parseInt(parts[1]);
                if (occurrence > max) max = occurrence;
            }
        }
        return max;
    }

    /**
     * This method registers a new instructor by saving their username and encoded password hash to the database.
     * @param username The username of the instructor.
     * @param rawPassword The raw password to be encoded and stored.
     * @return The newly created Instructor object.
     */
    public Instructor register(String username, String rawPassword) {
        Instructor instructor = new Instructor(username, encoder.encode(rawPassword));
        instructorRepo.save(instructor);
        return instructor;
    }

    /**
     * This method checks if the provided username and raw password match an existing instructor's credentials.
     * @param username The username of the instructor.
     * @param rawPassword The raw password to check against the stored password hash.
     * @return true if the credentials exist in the database, false otherwise.
     */
    public boolean matches(String username, String rawPassword) {
        Instructor instructor = instructorRepo.findByUsername(username);
        if (instructor == null) {
            log.warn("Instructor with username {} not found", username);
            return false;
        }
        return encoder.matches(rawPassword, instructor.getPasswordHash());
    }

    /**
     * This method approves a problem by its ID.
     * It retrieves the LearningMaterial from the database, approves it, and saves it back.
     * If the LearningMaterial is not found, it logs a warning.
     * @param problemId The ID of the problem to approve.
     * @return true if the problem was successfully approved, false if it was not found.
     */
    public boolean approveProblem(String problemId) {
        Optional<LearningMaterial> material = learningMaterialRepo.findById(problemId);
        if (material.isPresent()) {
            LearningMaterial lm = material.get();
            log.info("Before approve(): " + lm.isApproved());
            lm.approve();
            log.info("After approve(): " + lm.isApproved());
            learningMaterialRepo.save(lm);
            log.info("Problem with ID {} approved", problemId);
            return true;
        } else {
            log.warn("Problem with ID {} not found", problemId);
            return false;
        }
    }
}
