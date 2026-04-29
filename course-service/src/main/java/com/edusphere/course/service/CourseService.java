package com.edusphere.course.service;
 
import com.edusphere.course.dto.CourseDTO;
import com.edusphere.course.dto.DepartmentDTO;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
 
public interface CourseService {
 
    List<CourseDTO> getAllCourses();
 
    CourseDTO getCourseById(Long id);
 
    void assignCourseToDepartment(Long courseId, Long departmentId);

	CourseDTO updateCourse(Long id, CourseDTO dto);

	void deleteCourse(Long id);

	CourseDTO createCourse(CourseDTO dto, HttpServletRequest request);

	List<DepartmentDTO> getDepartmentsByCourse(Long courseId);

	List<CourseDTO> getCoursesByDepartment(Long id);
}