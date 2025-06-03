package Repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LearningMaterialTagRepo extends JpaRepository<Object, String> {
    /**
     * Retrieves all learning material tags from the database.
     * Each tag is represented as an array of objects containing the learning material UUID and the tag.
     *
     * @return a list of object arrays, where each array contains the learning material UUID and its associated tag.
     */
    @Query(value = "SELECT LEARNING_MATERIAL_UUID, TAG FROM LEARNING_MATERIAL_TAGS", nativeQuery = true)
    List<Object[]> findAllMaterialTags();
}
