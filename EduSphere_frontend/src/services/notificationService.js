import api from './api'

export const notificationService = {
  getAll: () => api.get('/notifications'),
  getUnreadCount: () => api.get('/notifications/count'),
  markRead: (id) => api.patch(`/notifications/${id}/read`),
  getPreferences: () => api.get('/notifications/preferences'),
  updatePreferences: (data) => api.post('/notifications/preferences', data),
}
