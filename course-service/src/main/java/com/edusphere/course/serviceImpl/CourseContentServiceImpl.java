package com.edusphere.course.serviceImpl;

import com.edusphere.course.client.EnrollmentServiceClient;
import com.edusphere.course.client.dto.ClientApiResponse;
import com.edusphere.course.client.dto.EnrollmentCheckDto;
import com.edusphere.course.dto.request.AddContentRequest;
import com.edusphere.course.dto.response.CourseContentResponse;
import com.edusphere.course.entity.CourseContent;
import com.edusphere.course.exception.CustomException;
import com.edusphere.course.repository.CourseContentRepository;
import com.edusphere.course.service.CourseContentService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseContentServiceImpl implements com.edusphere.course.service.CourseContentService {

    private final CourseContentRepository contentRepository;
    private final EnrollmentServiceClient enrollmentServiceClient;

    @Override
    @Transactional
    public CourseContentResponse addContent(UUID courseId, AddContentRequest request, UUID instructorId) {
        // Verify instructor is enrolled in this course before allowing content upload
        try {
            ClientApiResponse<EnrollmentCheckDto> enrollCheck =
                    enrollmentServiceClient.isEnrolled(instructorId, courseId);
            if (enrollCheck == null || enrollCheck.getData() == null || !enrollCheck.getData().isEnrolled()) {
                throw new CustomException(
                        "Instructor is not enrolled in this course. Enroll first to upload content.",
                        HttpStatus.FORBIDDEN);
            }
        } catch (CustomException e) {
            throw e;
        } catch (FeignException e) {
            log.warn("Enrollment service unavailable, skipping enrollment check for instructor {} on course {}",
                    instructorId, courseId);
        } catch (Exception e) {
            log.warn("Could not verify instructor enrollment for course {}: {}", courseId, e.getMessage());
        }

        CourseContent content = CourseContent.builder()
                .courseId(courseId).title(request.getTitle())
                .contentType(request.getContentType())
                .filePathOrUrl(request.getFilePathOrUrl())
                .body(request.getBody())
                .addedBy(instructorId)
                .sequenceNumber(request.getSequenceNumber())
                .build();
        return toResponse(contentRepository.save(content));
    }

    @Override
    public List<CourseContentResponse> listContent(UUID courseId) {
        return contentRepository.findByCourseIdAndDeletedFalseOrderBySequenceNumber(courseId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CourseContentResponse updateContent(UUID contentId, AddContentRequest request) {
        CourseContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new CustomException("Content not found", HttpStatus.NOT_FOUND));
        if (request.getTitle() != null) content.setTitle(request.getTitle());
        if (request.getFilePathOrUrl() != null) content.setFilePathOrUrl(request.getFilePathOrUrl());
        if (request.getBody() != null) content.setBody(request.getBody());
        if (request.getSequenceNumber() > 0) content.setSequenceNumber(request.getSequenceNumber());
        return toResponse(contentRepository.save(content));
    }

    @Override
    @Transactional
    public void deleteContent(UUID contentId) {
        CourseContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new CustomException("Content not found", HttpStatus.NOT_FOUND));
        content.setDeleted(true);
        contentRepository.save(content);
    }

    private CourseContentResponse toResponse(CourseContent c) {
        return CourseContentResponse.builder()
                .contentId(c.getContentId()).courseId(c.getCourseId())
                .title(c.getTitle()).contentType(c.getContentType())
                .filePathOrUrl(c.getFilePathOrUrl()).body(c.getBody())
                .addedBy(c.getAddedBy()).sequenceNumber(c.getSequenceNumber()).build();
    }
}
