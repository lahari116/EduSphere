import api from './api'

export const departmentService = {
  getAll: () => api.get('/departments'),
  getById: (deptId) => api.get(`/departments/${deptId}`),
  create: (data) => api.post('/departments', data),
  getCoursesByDepartment: (deptId) => api.get(`/departments/${deptId}/courses`),
}
