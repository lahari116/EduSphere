package com.edusphere.assignment.repository;
 
import com.edusphere.assignment.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
 
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
 
    List<Assignment> findByCourseId(Long courseId);
}