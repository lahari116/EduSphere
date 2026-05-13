package com.edusphere.course.serviceImpl;

import com.edusphere.course.dto.request.CreateCourseRequest;
import com.edusphere.course.dto.request.UpdateCourseRequest;
import com.edusphere.course.dto.response.CourseResponse;
import com.edusphere.course.entity.Course;
import com.edusphere.course.entity.CourseDepartment;
import com.edusphere.course.exception.CustomException;
import com.edusphere.course.repository.CourseDepartmentRepository;
import com.edusphere.course.repository.CourseRepository;
import com.edusphere.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseDepartmentRepository courseDepartmentRepository;

    @Override
    @Transactional
    public CourseResponse createCourse(CreateCourseRequest request, UUID adminId) {
        if (courseRepository.findByCourseCodeAndDeletedFalse(request.getCourseCode()).isPresent()) {
            throw new CustomException("Course code already exists: " + request.getCourseCode(), HttpStatus.CONFLICT);
        }
        Course course = Course.builder()
                .courseName(request.getCourseName())
                .courseCode(request.getCourseCode().toUpperCase())
                .description(request.getDescription())
                .enrollmentDeadline(request.getEnrollmentDeadline())
                .completionDeadline(request.getCompletionDeadline())
                .createdByAdmin(adminId)
                .isActive(true)
                .build();
        Course saved = courseRepository.save(course);

        if (request.getDepartmentIds() != null) {
            request.getDepartmentIds().forEach(deptId -> {
                CourseDepartment cd = CourseDepartment.builder()
                        .courseId(saved.getCourseId()).deptId(deptId).build();
                courseDepartmentRepository.save(cd);
            });
        }
        return buildResponse(saved);
    }

    @Override
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAllByDeletedFalseAndIsActiveTrue().stream()
                .map(this::buildResponse).collect(Collectors.toList());
    }

    @Override
    public CourseResponse getCourseById(UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CustomException("Course not found", HttpStatus.NOT_FOUND));
        return buildResponse(course);
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(UUID courseId, UpdateCourseRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CustomException("Course not found", HttpStatus.NOT_FOUND));
        if (request.getCourseName() != null) course.setCourseName(request.getCourseName());
        if (request.getDescription() != null) course.setDescription(request.getDescription());
        if (request.getEnrollmentDeadline() != null) course.setEnrollmentDeadline(request.getEnrollmentDeadline());
        if (request.getCompletionDeadline() != null) course.setCompletionDeadline(request.getCompletionDeadline());
        if (request.getIsActive() != null) course.setActive(request.getIsActive());
        return buildResponse(courseRepository.save(course));
    }

    @Override
    @Transactional
    public void deleteCourse(UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CustomException("Course not found", HttpStatus.NOT_FOUND));
        course.setDeleted(true);
        courseRepository.save(course);
    }

    private CourseResponse buildResponse(Course c) {
        List<UUID> deptIds = courseDepartmentRepository.findByCourseId(c.getCourseId())
                .stream().map(CourseDepartment::getDeptId).collect(Collectors.toList());
        return CourseResponse.builder()
                .courseId(c.getCourseId()).courseName(c.getCourseName())
                .courseCode(c.getCourseCode()).description(c.getDescription())
                .enrollmentDeadline(c.getEnrollmentDeadline())
                .completionDeadline(c.getCompletionDeadline())
                .isActive(c.isActive()).departmentIds(deptIds).build();
    }
}
