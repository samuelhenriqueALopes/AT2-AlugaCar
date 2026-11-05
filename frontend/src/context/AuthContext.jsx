import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import { api, setUnauthorizedHandler, tokenStore } from '../services/api.js'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(null)
  const [carregando, setCarregando] = useState(true)

  const logout = useCallback(() => {
    tokenStore.clear()
    setUsuario(null)
  }, [])

  useEffect(() => {
    setUnauthorizedHandler(logout)
    if (!tokenStore.get()) {
      setCarregando(false)
      return
    }
    api.get('/api/auth/me')
      .then(setUsuario)
      .catch(logout)
      .finally(() => setCarregando(false))
  }, [logout])

  async function login(login, senha) {
    const { token, usuario } = await api.post('/api/auth/login', { login, senha })
    tokenStore.set(token)
    setUsuario(usuario)
    return usuario
  }

  const value = {
    usuario,
    carregando,
    login,
    logout,
    atualizarUsuario: setUsuario,
    isCliente: usuario?.papel === 'CLIENTE',
    isAgente: usuario?.papel === 'AGENTE',
    isBanco: usuario?.tipoAgente === 'BANCO',
  }
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export const useAuth = () => useContext(AuthContext)
