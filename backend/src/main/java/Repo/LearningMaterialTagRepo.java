package Repo;

import Classroom.LearningMaterialTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningMaterialTagRepo extends JpaRepository<LearningMaterialTag, Long> {
    /**
     * Retrieves all learning material tags from the database.
     * Each tag is represented as an array of objects containing the learning material UUID and the tag.
     *
     * @return a list of object arrays, where each array contains the learning material UUID and its associated tag.
     */
    List<LearningMaterialTag> findByLearningMaterialUuid(String learningMaterialUuid);
}

