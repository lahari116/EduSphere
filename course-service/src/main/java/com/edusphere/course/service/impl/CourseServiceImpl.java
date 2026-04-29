package com.edusphere.course.service.impl;

import com.edusphere.course.client.IdentityServiceClient;
import com.edusphere.course.dto.CourseDTO;
import com.edusphere.course.dto.DepartmentDTO;
import com.edusphere.course.entity.Course;
import com.edusphere.course.entity.CourseDepartment;
import com.edusphere.course.entity.Department;
import com.edusphere.course.exception.DuplicateResourceException;
import com.edusphere.course.exception.ResourceNotFoundException;
import com.edusphere.course.exception.UnauthorizedActionException;
import com.edusphere.course.repository.CourseDepartmentRepository;
import com.edusphere.course.repository.CourseRepository;
import com.edusphere.course.repository.DepartmentRepository;
import com.edusphere.course.security.JwtUtil;
import com.edusphere.course.service.CourseService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseDepartmentRepository courseDepartmentRepository;
    private final IdentityServiceClient identityClient;
    private final JwtUtil jwtUtil;

    @Override
    public CourseDTO createCourse(CourseDTO dto, HttpServletRequest request) {

        String token = request.getHeader("Authorization").substring(7);

        Long userId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);

        if (!role.equals("ADMIN") && !role.equals("TEACHER")) {
            throw new UnauthorizedActionException(
                    "Only ADMIN or TEACHER can create courses"
            );
        }

        Course course = Course.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .createdBy(userId)
                .build();

        Course saved = courseRepository.save(course);

        return mapToDTO(saved);
    }

    @Override
    public CourseDTO updateCourse(Long id, CourseDTO dto) {

        Course course = courseRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Course not found with id: " + id)
                );

        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());

        return mapToDTO(courseRepository.save(course));
    }

    @Override
    public void deleteCourse(Long id) {

        Course course = courseRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Course not found with id: " + id)
                );

        course.setDeleted(true);
        course.setDeletedAt(LocalDateTime.now());

        courseRepository.save(course);
    }

    @Override
    public CourseDTO getCourseById(Long id) {

        Course course = courseRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Course not found with id: " + id)
                );

        return mapToDTO(course);
    }

    @Override
    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseDTO> getCoursesByDepartment(Long departmentId) {

        Department department = departmentRepository.findByIdAndIsDeletedFalse(departmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Department not found with id: " + departmentId)
                );

        return courseDepartmentRepository.findByDepartmentAndIsDeletedFalse(department)
                .stream()
                .map(mapping -> mapToDTO(mapping.getCourse()))
                .toList();
    }

    @Override
    public List<DepartmentDTO> getDepartmentsByCourse(Long courseId) {

        Course course = courseRepository.findByIdAndIsDeletedFalse(courseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Course not found with id: " + courseId)
                );

        return courseDepartmentRepository.findByCourseAndIsDeletedFalse(course)
                .stream()
                .map(mapping -> {
                    Department d = mapping.getDepartment();
                    return new DepartmentDTO(
                            d.getId(),
                            d.getName(),
                            d.getCreatedBy(),
                            d.getCreatedAt(),
                            d.getUpdatedAt()
                    );
                })
                .toList();
    }

    @Override
    public void assignCourseToDepartment(Long courseId, Long departmentId) {

        Course course = courseRepository.findByIdAndIsDeletedFalse(courseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Course not found with id: " + courseId)
                );

        Department department = departmentRepository.findByIdAndIsDeletedFalse(departmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Department not found with id: " + departmentId)
                );

        if (courseDepartmentRepository.existsByCourseAndDepartment(course, department)) {
            throw new DuplicateResourceException(
                    "Course already assigned to this department"
            );
        }

        CourseDepartment mapping = CourseDepartment.builder()
                .course(course)
                .department(department)
                .build();

        courseDepartmentRepository.save(mapping);
    }

    private CourseDTO mapToDTO(Course course) {
        return new CourseDTO(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getCreatedBy(),
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
    }
}