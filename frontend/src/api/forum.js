import http from './http'

export function fetchPostPage(params) {
  return http.get('/api/posts/page', { params })
}

export function fetchPostDetail(postId, params) {
  return http.get(`/api/posts/${postId}`, { params })
}

export function createPost(payload) {
  return http.post('/api/posts', payload)
}

export function likePost(postId) {
  return http.post(`/api/posts/${postId}/like`)
}

export function unlikePost(postId) {
  return http.delete(`/api/posts/${postId}/like`)
}

export function fetchPostReplies(postId) {
  return http.get(`/api/posts/${postId}/replies`)
}

export function createReply(postId, payload) {
  return http.post(`/api/posts/${postId}/replies`, payload)
}

export function likeReply(postId, replyId) {
  return http.post(`/api/posts/${postId}/replies/${replyId}/like`)
}

export function unlikeReply(postId, replyId) {
  return http.delete(`/api/posts/${postId}/replies/${replyId}/like`)
}
