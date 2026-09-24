export const PHONE_RE = /^1[3-9]\d{9}$/
export const USERNAME_RE = /^[A-Za-z0-9_]{4,20}$/
export const PASSWORD_RE = /^(?=.*[A-Za-z])(?=.*\d).{8,32}$/
export const SMS_RE = /^\d{4,8}$/
export const NAME_RE = /^[\u4e00-\u9fa5A-Za-z·]{2,20}$/

export function required(value: unknown, label: string) {
  if (value == null || String(value).trim() === '') return `请填写${label}`
  return ''
}

export function minLen(value: unknown, min: number, label: string) {
  const text = String(value ?? '').trim()
  if (text.length < min) return `${label}至少 ${min} 个字`
  return ''
}

export function maxLen(value: unknown, max: number, label: string) {
  const text = String(value ?? '').trim()
  if (text.length > max) return `${label}不能超过 ${max} 个字`
  return ''
}

export function phone(value: unknown, label = '手机号', optional = false) {
  const text = String(value ?? '').trim()
  if (!text) return optional ? '' : `请填写${label}`
  return PHONE_RE.test(text) ? '' : `${label}格式不正确`
}

export function username(value: unknown) {
  const text = String(value ?? '').trim()
  if (!text) return '请填写登录名'
  return USERNAME_RE.test(text) ? '' : '登录名 4-20 位，仅字母、数字或下划线'
}

export function password(value: unknown, label = '密码') {
  const text = String(value ?? '')
  if (!text) return `请填写${label}`
  return PASSWORD_RE.test(text) ? '' : `${label}至少 8 位，且同时包含字母和数字`
}

export function smsCode(value: unknown) {
  const text = String(value ?? '').trim()
  if (!text) return '请填写验证码'
  return SMS_RE.test(text) ? '' : '验证码为 4-8 位数字'
}

export function personName(value: unknown, label: string, optional = true) {
  const text = String(value ?? '').trim()
  if (!text) return optional ? '' : `请填写${label}`
  return NAME_RE.test(text) ? '' : `${label} 2-20 位，仅中文或字母`
}

export function numberRange(value: unknown, min: number, max: number, label: string, integer = false) {
  const num = Number(value)
  if (!Number.isFinite(num)) return `${label}必须是数字`
  if (integer && !Number.isInteger(num)) return `${label}必须是整数`
  if (num < min || num > max) return `${label}需在 ${min} ~ ${max} 之间`
  return ''
}

export function dateOrder(start?: string, end?: string, startLabel = '开始时间', endLabel = '结束时间') {
  if (!start || !end) return ''
  return new Date(start).getTime() >= new Date(end).getTime() ? `${endLabel}必须晚于${startLabel}` : ''
}

export function firstError(errors: Record<string, string>) {
  return Object.values(errors).find(Boolean) || ''
}

export function inputClass(error?: string) {
  return error ? 'admin-input is-error' : 'admin-input'
}

export function selectClass(error?: string) {
  return error ? 'admin-select is-error' : 'admin-select'
}

export function textareaClass(error?: string) {
  return error ? 'admin-textarea is-error' : 'admin-textarea'
}
