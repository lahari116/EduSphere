import api from './api'

export const adminService = {
  // User management
  getUsers: (params) => api.get('/admin/users', { params }),
  getUsersSilent: () => api.get('/admin/users', { silentError: true }),
  deleteUser: (id) => api.delete(`/admin/users/${id}`),
  uploadUsers: (file) => {
    const fd = new FormData()
    fd.append('file', file)
    // axios 1.x: if Content-Type is 'application/json' it JSON-serialises FormData.
    // Use transformRequest to delete the header so the browser sets
    // "multipart/form-data; boundary=..." automatically.
    return api.post('/admin/users/upload', fd, {
      transformRequest: (data, headers) => {
        headers.delete('Content-Type')
        return data
      },
    })
  },

  // User lookup by student/employee ID (accessible to coordinators via users endpoint)
  getUserByEmpId: (studentOrEmployeeId) =>
    api.get('/users/lookup', { params: { studentOrEmployeeId }, silentError: true }),

  // Enrollment count for admin dashboard
  getTotalEnrollmentCount: () => api.get('/enrollments/count', { silentError: true }),

  // Audit logs
  getAuditLogs: (params) => api.get('/audit/logs/all', { params }),
  exportAuditLogs: (params) => api.post('/audit/logs/export', params),
}
