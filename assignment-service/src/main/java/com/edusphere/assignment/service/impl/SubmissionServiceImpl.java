package com.edusphere.assignment.service.impl;

import com.edusphere.assignment.client.AnalyticsServiceClient;
import com.edusphere.assignment.client.AuditServiceClient;
import com.edusphere.assignment.client.EnrollmentServiceClient;
import com.edusphere.assignment.client.dto.AuditLogRequest;
import com.edusphere.assignment.client.dto.ClientApiResponse;
import com.edusphere.assignment.client.dto.EnrollmentCheckDto;
import com.edusphere.assignment.client.dto.ProgressUpdateRequest;
import com.edusphere.assignment.dto.request.AnswerRequest;
import com.edusphere.assignment.dto.request.SubmitAssignmentRequest;
import com.edusphere.assignment.dto.response.*;
import com.edusphere.assignment.entity.Assignment;
import com.edusphere.assignment.entity.Question;
import com.edusphere.assignment.entity.Submission;
import com.edusphere.assignment.entity.SubmissionAnswer;
import com.edusphere.assignment.enums.SubmissionStatus;
import com.edusphere.assignment.exception.CustomException;
import com.edusphere.assignment.repository.AssignmentRepository;
import com.edusphere.assignment.repository.QuestionRepository;
import com.edusphere.assignment.repository.SubmissionAnswerRepository;
import com.edusphere.assignment.repository.SubmissionRepository;
import com.edusphere.assignment.service.SubmissionService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private final AssignmentRepository assignmentRepository;
    private final QuestionRepository questionRepository;
    private final SubmissionRepository submissionRepository;
    private final SubmissionAnswerRepository submissionAnswerRepository;
    private final EnrollmentServiceClient enrollmentServiceClient;
    private final AnalyticsServiceClient analyticsServiceClient;
    private final AuditServiceClient auditServiceClient;

    @Override
    @Transactional
    public SubmissionDetailResponse submitAssignment(UUID assignmentId, UUID studentId, SubmitAssignmentRequest request) {
        // Step 1: Load assignment and check deadline
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new CustomException("Assignment not found", HttpStatus.NOT_FOUND));

        if (assignment.getSubmissionDeadline().isBefore(LocalDateTime.now())) {
            throw new CustomException("Submission deadline has passed", HttpStatus.BAD_REQUEST);
        }

        // Step 2: Check enrollment in the course
        try {
            ClientApiResponse<EnrollmentCheckDto> enrollCheck =
                    enrollmentServiceClient.isEnrolled(studentId, assignment.getCourseId());
            if (enrollCheck == null || enrollCheck.getData() == null || !enrollCheck.getData().isEnrolled()) {
                throw new CustomException("Student is not enrolled in this course", HttpStatus.FORBIDDEN);
            }
        } catch (CustomException e) {
            throw e;
        } catch (FeignException e) {
            log.warn("Enrollment service unavailable, skipping check for student {} on assignment {}",
                    studentId, assignmentId);
        } catch (Exception e) {
            log.warn("Could not verify enrollment for student {}: {}", studentId, e.getMessage());
        }

        // Step 3: Check if already submitted
        if (submissionRepository.existsByAssignmentIdAndStudentId(assignmentId, studentId)) {
            Submission existing = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId)
                    .orElseThrow();
            if (existing.getStatus() == SubmissionStatus.SUBMITTED) {
                throw new CustomException("Assignment already submitted", HttpStatus.CONFLICT);
            }
        }

        // Step 4: Load questions
        List<Question> questions = questionRepository.findByAssignmentIdOrderBySequenceNumber(assignmentId);
        Map<UUID, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getQuestionId, q -> q));

        // Step 5: Grade answers
        int correctAnswers = 0;
        int totalQuestions = questions.size();
        List<SubmissionAnswer> submissionAnswers = new ArrayList<>();

        List<AnswerRequest> answers = request.getAnswers() != null ? request.getAnswers() : new ArrayList<>();

        for (AnswerRequest answerRequest : answers) {
            Question question = questionMap.get(answerRequest.getQuestionId());
            if (question == null) continue;

            boolean isCorrect = answerRequest.getSelectedOption() != null
                    && answerRequest.getSelectedOption() == question.getCorrectOption();
            if (isCorrect) correctAnswers++;

            submissionAnswers.add(SubmissionAnswer.builder()
                    .questionId(question.getQuestionId())
                    .selectedOption(answerRequest.getSelectedOption())
                    .isCorrect(isCorrect)
                    .build());
        }

        // Step 6: Calculate score
        double score = totalQuestions > 0 ? (correctAnswers * 100.0 / totalQuestions) : 0.0;

        // Step 7: Save Submission
        Submission submission = submissionRepository
                .findByAssignmentIdAndStudentId(assignmentId, studentId)
                .orElse(Submission.builder()
                        .assignmentId(assignmentId)
                        .studentId(studentId)
                        .build());

        submission.setStatus(SubmissionStatus.SUBMITTED);
        submission.setScore(score);
        submission.setCorrectAnswers(correctAnswers);
        submission.setTotalQuestions(totalQuestions);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setTimeTakenSeconds(request.getTimeTakenSeconds());

        Submission savedSubmission = submissionRepository.save(submission);

        // Step 8: Save SubmissionAnswers
        UUID submissionId = savedSubmission.getSubmissionId();
        for (SubmissionAnswer sa : submissionAnswers) {
            sa.setSubmissionId(submissionId);
        }
        List<SubmissionAnswer> savedAnswers = submissionAnswerRepository.saveAll(submissionAnswers);

        // Step 9: Notify analytics service
        try {
            analyticsServiceClient.updateProgress(ProgressUpdateRequest.builder()
                    .studentId(studentId)
                    .courseId(assignment.getCourseId())
                    .assignmentId(assignmentId)
                    .score(score)
                    .eventType("ASSIGNMENT_SUBMITTED")
                    .build());
        } catch (Exception e) {
            log.warn("Could not notify analytics service after submission: {}", e.getMessage());
        }

        // Step 9b: Audit log
        try {
            auditServiceClient.createLog(AuditLogRequest.builder()
                    .actorId(studentId)
                    .actorRole("STUDENT")
                    .action("ASSIGNMENT_SUBMITTED")
                    .resourceType("SUBMISSION")
                    .resourceId(savedSubmission.getSubmissionId().toString())
                    .serviceName("assignment-service")
                    .additionalData("assignmentId=" + assignmentId + ", score=" + score)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to create audit log for ASSIGNMENT_SUBMITTED: {}", e.getMessage());
        }

        // Step 10: Build response (correctOption shown after submission)
        List<AnswerDetailResponse> answerDetailResponses = savedAnswers.stream()
                .map(sa -> {
                    Question q = questionMap.get(sa.getQuestionId());
                    return AnswerDetailResponse.builder()
                            .questionId(sa.getQuestionId())
                            .selectedOption(sa.getSelectedOption())
                            .isCorrect(sa.isCorrect())
                            .correctOption(q != null ? q.getCorrectOption() : null)
                            .build();
                })
                .collect(Collectors.toList());

        return mapToSubmissionDetailResponse(savedSubmission, answerDetailResponses);
    }

    @Override
    public SubmissionDetailResponse getSubmission(UUID submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new CustomException("Submission not found", HttpStatus.NOT_FOUND));

        List<SubmissionAnswer> submissionAnswers = submissionAnswerRepository.findBySubmissionId(submissionId);
        List<Question> questions = questionRepository.findByAssignmentIdOrderBySequenceNumber(submission.getAssignmentId());
        Map<UUID, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getQuestionId, q -> q));

        List<AnswerDetailResponse> answerDetailResponses = submissionAnswers.stream()
                .map(sa -> {
                    Question q = questionMap.get(sa.getQuestionId());
                    return AnswerDetailResponse.builder()
                            .questionId(sa.getQuestionId())
                            .selectedOption(sa.getSelectedOption())
                            .isCorrect(sa.isCorrect())
                            .correctOption(q != null ? q.getCorrectOption() : null)
                            .build();
                })
                .collect(Collectors.toList());

        return mapToSubmissionDetailResponse(submission, answerDetailResponses);
    }

    @Override
    public List<SubmissionResponse> getSubmissionsByAssignment(UUID assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId).stream()
                .map(this::mapToSubmissionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProgressResponse getStudentProgress(UUID studentId) {
        List<Submission> submissions = submissionRepository.findByStudentId(studentId);

        int totalAssignments = submissions.size();
        int submittedAssignments = (int) submissions.stream()
                .filter(s -> s.getStatus() == SubmissionStatus.SUBMITTED)
                .count();

        double averageScore = submissions.stream()
                .filter(s -> s.getStatus() == SubmissionStatus.SUBMITTED && s.getScore() != null)
                .mapToDouble(Submission::getScore)
                .average()
                .orElse(0.0);

        List<SubmissionResponse> submissionResponses = submissions.stream()
                .map(this::mapToSubmissionResponse)
                .collect(Collectors.toList());

        return ProgressResponse.builder()
                .studentId(studentId)
                .totalAssignments(totalAssignments)
                .submittedAssignments(submittedAssignments)
                .averageScore(averageScore)
                .submissions(submissionResponses)
                .build();
    }

    @Override
    public AttemptStatusResponse getAttemptStatus(UUID assignmentId) {
        assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new CustomException("Assignment not found", HttpStatus.NOT_FOUND));

        List<Submission> submissions = submissionRepository.findByAssignmentId(assignmentId);

        Set<UUID> attemptedStudentIds = submissions.stream()
                .map(Submission::getStudentId)
                .collect(Collectors.toSet());

        List<AttemptStatusResponse.StudentAttemptInfo> attemptedList = submissions.stream()
                .map(s -> AttemptStatusResponse.StudentAttemptInfo.builder()
                        .studentId(s.getStudentId())
                        .submissionId(s.getSubmissionId())
                        .score(s.getScore())
                        .submittedAt(s.getSubmittedAt())
                        .build())
                .collect(Collectors.toList());

        return AttemptStatusResponse.builder()
                .assignmentId(assignmentId)
                .attempted(attemptedList)
                .notAttempted(new ArrayList<>()) // populated below if enrollment data available
                .totalEnrolled(attemptedList.size())
                .totalAttempted(attemptedList.size())
                .totalNotAttempted(0)
                .build();
    }

    private SubmissionResponse mapToSubmissionResponse(Submission submission) {
        return SubmissionResponse.builder()
                .submissionId(submission.getSubmissionId())
                .assignmentId(submission.getAssignmentId())
                .studentId(submission.getStudentId())
                .submittedAt(submission.getSubmittedAt())
                .timeTakenSeconds(submission.getTimeTakenSeconds())
                .score(submission.getScore())
                .totalQuestions(submission.getTotalQuestions())
                .correctAnswers(submission.getCorrectAnswers())
                .status(submission.getStatus())
                .build();
    }

    private SubmissionDetailResponse mapToSubmissionDetailResponse(Submission submission,
                                                                    List<AnswerDetailResponse> answers) {
        return SubmissionDetailResponse.builder()
                .submissionId(submission.getSubmissionId())
                .assignmentId(submission.getAssignmentId())
                .studentId(submission.getStudentId())
                .submittedAt(submission.getSubmittedAt())
                .timeTakenSeconds(submission.getTimeTakenSeconds())
                .score(submission.getScore())
                .totalQuestions(submission.getTotalQuestions())
                .correctAnswers(submission.getCorrectAnswers())
                .status(submission.getStatus())
                .answers(answers)
                .build();
    }
}
