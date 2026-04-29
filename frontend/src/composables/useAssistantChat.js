import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { askAssistantStream } from '../api/assistant'

export function useAssistantChat() {
  const form = reactive({
    question: ''
  })
  const loading = ref(false)
  const result = ref(null)
  const lastQuestion = ref('')
  let activeController = null

  async function submitQuestion() {
    const question = String(form.question || '').trim()
    if (!question) {
      ElMessage.warning('请先输入问题')
      return
    }
    if (activeController) {
      activeController.abort()
    }

    activeController = new AbortController()
    loading.value = true
    lastQuestion.value = question
    result.value = {
      question,
      answer: '',
      matchedCount: 0,
      fallback: false,
      sources: []
    }

    try {
      await askAssistantStream({
        question
      }, {
        signal: activeController.signal,
        onChunk(chunk) {
          if (chunk.type === 'meta') {
            result.value = {
              question: chunk.question || question,
              answer: result.value?.answer || '',
              matchedCount: chunk.matchedCount || 0,
              fallback: Boolean(chunk.fallback),
              sources: chunk.sources || []
            }
            return
          }
          if (chunk.type === 'delta') {
            result.value = {
              ...(result.value || {}),
              answer: `${result.value?.answer || ''}${chunk.content || ''}`
            }
            return
          }
          if (chunk.type === 'done') {
            result.value = {
              question: chunk.question || question,
              answer: chunk.answer || result.value?.answer || '',
              matchedCount: typeof chunk.matchedCount === 'number' ? chunk.matchedCount : (result.value?.matchedCount || 0),
              fallback: typeof chunk.fallback === 'boolean' ? chunk.fallback : Boolean(result.value?.fallback),
              sources: chunk.sources || result.value?.sources || []
            }
          }
        }
      })
    } catch (error) {
      if (error.name !== 'AbortError') {
        ElMessage.error(error.message)
      }
    } finally {
      activeController = null
      loading.value = false
    }
  }

  function fillExample(question) {
    form.question = question
  }

  return {
    form,
    loading,
    result,
    lastQuestion,
    submitQuestion,
    fillExample
  }
}
