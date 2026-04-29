package com.edusphere.course.service.impl;

import com.edusphere.course.client.EnrollmentClient;
import com.edusphere.course.dto.CourseResourceDTO;
import com.edusphere.course.dto.CourseResourceRequest;
import com.edusphere.course.entity.Course;
import com.edusphere.course.entity.CourseResource;
import com.edusphere.course.exception.InvalidOperationException;
import com.edusphere.course.exception.ResourceNotFoundException;
import com.edusphere.course.exception.UnauthorizedActionException;
import com.edusphere.course.repository.CourseRepository;
import com.edusphere.course.repository.CourseResourceRepository;
import com.edusphere.course.security.JwtUtil;
import com.edusphere.course.service.CourseResourceService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseResourceServiceImpl implements CourseResourceService {

    private final CourseRepository courseRepository;
    private final CourseResourceRepository courseResourceRepository;
    private final EnrollmentClient enrollmentClient;
    private final JwtUtil jwtUtil;

    @Override
    public CourseResourceDTO addResource(Long courseId,
                                         CourseResourceRequest request,
                                         HttpServletRequest httpRequest) {

        String token = httpRequest.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);

        if (!role.equals("ADMIN") && !role.equals("TEACHER")) {
            throw new UnauthorizedActionException(
                    "Only ADMIN or TEACHER can add course resources"
            );
        }

        if (role.equals("TEACHER") &&
                !enrollmentClient.isUserEnrolled(userId, courseId, "TEACHER", token)) {
            throw new InvalidOperationException(
                    "Teacher is not enrolled in this course"
            );
        }

        Course course = courseRepository.findByIdAndIsDeletedFalse(courseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Course not found with id: " + courseId)
                );

        CourseResource resource = CourseResource.builder()
                .title(request.getTitle())
                .url(request.getUrl())
                .course(course)
                .createdBy(userId)
                .build();

        return mapToDTO(courseResourceRepository.save(resource));
    }

    @Override
    public List<CourseResourceDTO> getResourcesByCourse(Long courseId,
                                                        HttpServletRequest httpRequest) {

        String token = httpRequest.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);

        if (!role.equals("ADMIN") &&
                !enrollmentClient.isUserEnrolled(userId, courseId, role, token)) {
            throw new UnauthorizedActionException("User not enrolled in this course");
        }

        Course course = courseRepository.findByIdAndIsDeletedFalse(courseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Course not found with id: " + courseId)
                );

        return courseResourceRepository.findByCourseAndIsDeletedFalse(course)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public void deleteResource(Long courseId,
                               Long resourceId,
                               HttpServletRequest httpRequest) {

        String token = httpRequest.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);

        if (!role.equals("ADMIN") && !role.equals("TEACHER")) {
            throw new UnauthorizedActionException(
                    "Only ADMIN or TEACHER can delete resources"
            );
        }

        CourseResource resource = courseResourceRepository.findByIdAndIsDeletedFalse(resourceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Resource not found")
                );

        if (!resource.getCourse().getId().equals(courseId)) {
            throw new InvalidOperationException(
                    "Resource does not belong to this course"
            );
        }

        resource.setDeleted(true);
        resource.setDeletedAt(LocalDateTime.now());
        courseResourceRepository.save(resource);
    }

    private CourseResourceDTO mapToDTO(CourseResource resource) {
        return new CourseResourceDTO(
                resource.getId(),
                resource.getTitle(),
                resource.getUrl(),
                resource.getCourse().getId(),
                resource.getCreatedBy(),
                resource.getCreatedAt(),
                resource.getUpdatedAt()
        );
    }
}
