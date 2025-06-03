package API;

import Classroom.LearningMaterial;
import Repo.LearningMaterialRepo;
import Repo.LearningMaterialTagRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DataController {
    @Autowired
    private LearningMaterialRepo learningMaterialRepo;

    @Autowired
    private LearningMaterialTagRepo tagRepo;

    public record MatchingLM(LearningMaterial learningMaterial, int matchingTopics) {
    }

    /**
     * This method retrieves an unsolved matching problem from the database.
     * It checks for a LearningMaterial that is answerable, has an assessment item and excludes those with excluded topics.
     * If such a LearningMaterial exists, it returns it; otherwise, it returns null.
     * It prioritizes approved problems, then those that also contain additional topics.
     * @param request The ProblemRequest object containing the criteria for the matching problem.
     * @return a LearningMaterial object representing the unsolved matching problem or null if none exists.
     */
    public LearningMaterial unsolvedMatchingProblem(ProblemRequest request){
        List<LearningMaterial> approvedMaterials = learningMaterialRepo.findByApproved(true);

        List<MatchingLM> sortedMaterials = new LinkedList<>();

        for(LearningMaterial material: approvedMaterials){
            if (material.isAnswerable() && material.getAssessmentItem() != null) {
                String[] materialTopics = tagRepo.findTagsByLearningMaterialUuid(material.getUuid()).toArray(new String[0]);
                int matchingTopics = countIntersection(materialTopics, request.additionalTopics());
                // Exclude materials that have topics in the excludedTopics list
                boolean hasExcludedTopics = Arrays.stream(request.excludedTopics())
                        .anyMatch(excludedTopic -> Arrays.asList(materialTopics).contains(excludedTopic));
                if (!hasExcludedTopics) sortedMaterials.add(new MatchingLM(material, matchingTopics));
            }
        }

        sortedMaterials.sort(Comparator.comparingInt(MatchingLM::matchingTopics).reversed());

        if (!sortedMaterials.isEmpty()) return sortedMaterials.get(0).learningMaterial();

        return null;
    }

    public static int countIntersection(String[] a, String[] b) {
        Set<String> setA = new HashSet<>(List.of(a));
        Set<String> setB = new HashSet<>(List.of(b));
        setA.retainAll(setB); // modifies setA to contain only elements also in setB
        return setA.size();
    }
}
