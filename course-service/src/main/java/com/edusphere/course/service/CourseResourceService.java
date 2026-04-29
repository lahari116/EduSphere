package com.edusphere.course.service;

import java.util.List;

import com.edusphere.course.dto.CourseResourceDTO;
import com.edusphere.course.dto.CourseResourceRequest;

import jakarta.servlet.http.HttpServletRequest;

public interface CourseResourceService {
	 
    CourseResourceDTO addResource(Long courseId,
                                  CourseResourceRequest request,
                                  HttpServletRequest httpRequest);
 
    List<CourseResourceDTO> getResourcesByCourse(Long courseId,
                                                 HttpServletRequest httpRequest);
 
    void deleteResource(Long courseId,
                        Long resourceId,
                        HttpServletRequest httpRequest);
}