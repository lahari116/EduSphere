package com.edusphere.course.controller;

import com.edusphere.course.dto.CourseDTO;
import com.edusphere.course.dto.CourseResourceDTO;
import com.edusphere.course.dto.CourseResourceRequest;
import com.edusphere.course.dto.DepartmentDTO;
import com.edusphere.course.service.CourseResourceService;
import com.edusphere.course.service.CourseService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Validated
public class CourseController {

    private final CourseService courseService;
    private final CourseResourceService courseResourceService;

    // ✅ Delete Course Resource
    @DeleteMapping("/{courseId}/resources/{resourceId}")
    public ResponseEntity<String> deleteResource(
            @PathVariable Long courseId,
            @PathVariable Long resourceId,
            HttpServletRequest httpRequest
    ) {
        courseResourceService.deleteResource(courseId, resourceId, httpRequest);
        return ResponseEntity.ok("Resource deleted successfully");
    }

    // ✅ Add Resource to Course
    @PostMapping("/courses/{courseId}/resources")
    public CourseResourceDTO addResource(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseResourceRequest request,
            HttpServletRequest httpRequest
    ) {
        return courseResourceService.addResource(courseId, request, httpRequest);
    }

    // ✅ Get Course Resources
    @GetMapping("/courses/{courseId}/resources")
    public List<CourseResourceDTO> getResources(
            @PathVariable Long courseId,
            HttpServletRequest httpRequest
    ) {
        return courseResourceService.getResourcesByCourse(courseId, httpRequest);
    }

    // ✅ Get Courses by Department
    @GetMapping("/departments/{departmentId}/courses")
    public List<CourseDTO> getCoursesByDepartment(
            @PathVariable Long departmentId
    ) {
        return courseService.getCoursesByDepartment(departmentId);
    }

    // ✅ Get Departments by Course
    @GetMapping("/{courseId}/departments")
    public List<DepartmentDTO> getDepartmentsByCourse(
            @PathVariable Long courseId
    ) {
        return courseService.getDepartmentsByCourse(courseId);
    }

    // ✅ Delete Course
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok("Course deleted successfully");
    }

    // ✅ Update Course (VALIDATION ENABLED)
    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseDTO dto
    ) {
        return ResponseEntity.ok(courseService.updateCourse(id, dto));
    }

    // ✅ Create Course (VALIDATION ENABLED)
    @PostMapping
    public CourseDTO createCourse(
            @Valid @RequestBody CourseDTO dto,
            HttpServletRequest request
    ) {
        return courseService.createCourse(dto, request);
    }

    // ✅ Get All Courses
    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    // ✅ Get Course by ID
    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    // ✅ Assign Course to Department
    @PostMapping("/{courseId}/departments/{departmentId}")
    public ResponseEntity<String> assignCourseToDepartment(
            @PathVariable Long courseId,
            @PathVariable Long departmentId
    ) {
        courseService.assignCourseToDepartment(courseId, departmentId);
        return ResponseEntity.ok("Course assigned to department successfully");
    }
}
