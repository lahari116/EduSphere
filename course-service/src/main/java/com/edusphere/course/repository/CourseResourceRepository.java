package com.edusphere.course.repository;
 
import com.edusphere.course.entity.CourseResource;
import com.edusphere.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
import java.util.Optional;
 
public interface CourseResourceRepository extends JpaRepository<CourseResource, Long> {
 
    List<CourseResource> findByCourseAndIsDeletedFalse(Course course);

	Optional<CourseResource> findByIdAndIsDeletedFalse(Long resourceId);
}