package com.edusphere.course.service;

import com.edusphere.course.dto.request.AddContentRequest;
import com.edusphere.course.dto.response.CourseContentResponse;

import java.util.List;
import java.util.UUID;

public interface CourseContentService {
    CourseContentResponse addContent(UUID courseId, AddContentRequest request, UUID instructorId);
    List<CourseContentResponse> listContent(UUID courseId);
    CourseContentResponse updateContent(UUID contentId, AddContentRequest request);
    void deleteContent(UUID contentId);
}
