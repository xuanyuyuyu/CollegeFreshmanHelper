import http from './http'

export function fetchMySummary() {
  return http.get('/api/users/me/summary')
}

export function fetchMyPosts(params) {
  return http.get('/api/users/me/posts', { params })
}

export function fetchMyReplies(params) {
  return http.get('/api/users/me/replies', { params })
}

export function fetchMyLikes() {
  return http.get('/api/users/me/likes')
}

export function fetchMyLikeDetails(params) {
  return http.get('/api/users/me/likes/details', { params })
}

export function fetchMyLikedItems(params) {
  return http.get('/api/users/me/liked-items', { params })
}

export function updateMyProfile(payload) {
  return http.put('/api/users/me/profile', payload)
}

export function updateMyAvatar(payload) {
  return http.put('/api/users/me/avatar', payload)
}
