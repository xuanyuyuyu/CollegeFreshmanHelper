export function titleBadgeClass(title) {
  if (!title) {
    return 'bg-slate-100 text-slate-400'
  }
  if (title === '校园百晓生') {
    return 'bg-violet-100 text-violet-700'
  }
  if (title === '官方认证学长') {
    return 'bg-amber-100 text-amber-700'
  }
  if (title === '热心答主' || title === '高赞答主' || title === '知识共建者') {
    return 'bg-sky-100 text-sky-700'
  }
  return 'bg-slate-100 text-slate-500'
}
