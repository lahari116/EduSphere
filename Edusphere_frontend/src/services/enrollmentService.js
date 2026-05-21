import api from './api'

export const enrollmentService = {
  // ADMIN/COORDINATOR: manual enroll
  enroll: (data) => api.post('/enrollments', data),

  // STUDENT/INSTRUCTOR: self-enroll — courseId is a query param not body
  selfEnroll: (courseId) => api.post('/enrollments/self', null, { params: { courseId } }),

  // ADMIN/COORDINATOR/INSTRUCTOR: get enrollments for a course
  getEnrollmentsByCourse: (courseId) => api.get('/enrollments', { params: { courseId } }),

  // Get enrollments for a specific student
  getStudentEnrollments: (studentId) => api.get(`/students/${studentId}/enrollments`),

  // Check enrollment status (internal use)
  checkEnrollment: (userId, courseId) =>
    api.get('/enrollments/check', { params: { userId, courseId }, silentError: true }),

  // ADMIN: unenroll
  unenroll: (enrollmentId) => api.delete(`/enrollments/${enrollmentId}`),
}
