import http from './http'

export function register(payload) {
  return http.post('/api/auth/register', payload)
}

export function login(payload) {
  return http.post('/api/auth/login', payload)
}

export function fetchCurrentUser() {
  return http.get('/api/auth/me')
}

export function logout() {
  return http.post('/api/auth/logout')
}
