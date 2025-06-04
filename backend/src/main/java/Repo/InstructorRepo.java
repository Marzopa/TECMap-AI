package Repo;

import Classroom.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstructorRepo extends JpaRepository<Instructor, String> {
    Instructor findByUsername(String username);
}

