// Cliente HTTP do back-end Spring Boot (envia o token JWT em todas as requisicoes).
const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'
const TOKEN_KEY = 'alugacar.token'

export const tokenStore = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (t) => localStorage.setItem(TOKEN_KEY, t),
  clear: () => localStorage.removeItem(TOKEN_KEY),
}

let onUnauthorized = () => {}
export const setUnauthorizedHandler = (fn) => { onUnauthorized = fn }

async function request(method, path, body) {
  const headers = { 'Content-Type': 'application/json' }
  const token = tokenStore.get()
  if (token) headers.Authorization = `Bearer ${token}`

  let res
  try {
    res = await fetch(`${BASE_URL}${path}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    })
  } catch {
    throw new Error('Nao foi possivel conectar ao servidor. Verifique se o back-end esta em execucao.')
  }

  if (res.status === 401 && token) onUnauthorized()
  if (res.status === 204) return null

  const text = await res.text()
  const data = text ? JSON.parse(text) : null
  if (!res.ok) {
    const err = new Error(data?.mensagem || `Erro ${res.status}`)
    err.status = res.status
    err.campos = data?.campos || {}
    throw err
  }
  return data
}

export const api = {
  get: (p) => request('GET', p),
  post: (p, b) => request('POST', p, b ?? {}),
  put: (p, b) => request('PUT', p, b),
  patch: (p, b) => request('PATCH', p, b ?? {}),
  delete: (p) => request('DELETE', p),
}
