import http from './http'

export function askAssistant(payload) {
  return http.post('/api/assistant/ask', payload)
}

export async function askAssistantStream(payload, options = {}) {
  const headers = {
    'Content-Type': 'application/json'
  }
  const token = localStorage.getItem('cfh_token')
  if (token) {
    headers.Authorization = token.startsWith('Bearer ') ? token : `Bearer ${token}`
  }

  const response = await fetch('/api/assistant/ask/stream', {
    method: 'POST',
    headers,
    body: JSON.stringify(payload),
    signal: options.signal
  })

  if (!response.ok) {
    throw new Error((await response.text()) || '请求失败')
  }
  if (!response.body) {
    throw new Error('浏览器不支持流式响应')
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  while (true) {
    const { done, value } = await reader.read()
    buffer += decoder.decode(value || new Uint8Array(), { stream: !done })

    let lineBreakIndex = buffer.indexOf('\n')
    while (lineBreakIndex >= 0) {
      const line = buffer.slice(0, lineBreakIndex).trim()
      buffer = buffer.slice(lineBreakIndex + 1)
      if (line) {
        const chunk = JSON.parse(line)
        if (chunk.type === 'error') {
          throw new Error(chunk.message || '流式回答失败')
        }
        options.onChunk?.(chunk)
      }
      lineBreakIndex = buffer.indexOf('\n')
    }

    if (done) {
      const lastLine = buffer.trim()
      if (lastLine) {
        const chunk = JSON.parse(lastLine)
        if (chunk.type === 'error') {
          throw new Error(chunk.message || '流式回答失败')
        }
        options.onChunk?.(chunk)
      }
      break
    }
  }
}
