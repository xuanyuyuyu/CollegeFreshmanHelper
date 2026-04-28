import http from './http'

export function fetchAdminUsers(params) {
  return http.get('/api/admin/users/page', { params })
}

export function createAdminUser(payload) {
  return http.post('/api/admin/users/admins', payload)
}

export function updateAdminUserStatus(userId, status) {
  return http.post(`/api/admin/users/${userId}/status`, null, { params: { status } })
}

export function banUser(userId, payload) {
  return http.post(`/api/admin/users/${userId}/ban`, payload)
}

export function unbanUser(userId) {
  return http.post(`/api/admin/users/${userId}/unban`)
}

export function fetchAdminPosts(params) {
  return http.get('/api/admin/posts/page', { params })
}

export function deleteAdminPost(postId, payload) {
  return http.post(`/api/admin/posts/${postId}/delete`, payload)
}

export function updateAdminPostVisibility(postId, payload) {
  return http.post(`/api/admin/posts/${postId}/visibility`, payload)
}

export function fetchAdminReplies(params) {
  return http.get('/api/admin/replies/page', { params })
}

export function deleteAdminReply(replyId, payload) {
  return http.post(`/api/admin/replies/${replyId}/delete`, payload)
}

export function updateAdminReplyVisibility(replyId, payload) {
  return http.post(`/api/admin/replies/${replyId}/visibility`, payload)
}

export function fetchTitles() {
  return http.get('/api/admin/titles')
}

export function fetchGrantedTitles(params) {
  return http.get('/api/admin/titles/grants/page', { params })
}

export function grantTitle(payload) {
  return http.post('/api/admin/titles/grant', payload)
}

export function revokeTitle(userTitleId, payload) {
  return http.post(`/api/admin/titles/grants/${userTitleId}/revoke`, payload)
}

export function fetchKnowledgeList(params) {
  return http.get('/api/admin/knowledge/page', { params })
}

export function createKnowledge(payload) {
  return http.post('/api/admin/knowledge', payload)
}

export function updateKnowledgeStatus(knowledgeId, payload) {
  return http.post(`/api/admin/knowledge/${knowledgeId}/status`, payload)
}

export function fetchAdminLogs(params) {
  return http.get('/api/admin/logs/page', { params })
}
