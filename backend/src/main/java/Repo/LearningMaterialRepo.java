package Repo;

import Classroom.LearningMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * LearningMaterialRepo is a repository interface for managing LearningMaterial entities.
 * It extends JpaRepository to provide CRUD operations and custom query methods.
 * The Primary key type is String, representing the UUID of the LearningMaterial.
 */
@Repository
public interface LearningMaterialRepo extends JpaRepository<LearningMaterial, String>{
//    List<LearningMaterial> findByStudentId(String studentId);
//    List<LearningMaterial> findByDayNumber(int dayNumber);
//    List<LearningMaterial> findByCourseId(String courseId);
}