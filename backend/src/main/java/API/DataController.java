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
        Set<String> additionalTopics = Set.of(request.additionalTopics());

        List<Object[]> tagData = tagRepo.findAllMaterialTags();

        Map<String, List<String>> tagMap = new HashMap<>();
        for (Object[] row : tagData) {
            String uuid = (String) row[0];
            String tag = (String) row[1];
            tagMap.computeIfAbsent(uuid, k -> new ArrayList<>()).add(tag);
        }

        return approvedMaterials.isEmpty() ? null : approvedMaterials.get(0);
    }
}
