import { createContext, useContext, useEffect, useState } from 'react'

const THEME_KEY = 'alugacar.tema'
const ThemeContext = createContext(null)

function temaInicial() {
  const salvo = localStorage.getItem(THEME_KEY)
  if (salvo === 'claro' || salvo === 'escuro') return salvo
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'escuro' : 'claro'
}

/** Modo claro/escuro: aplicado via atributo data-tema no <html> e salvo no navegador. */
export function ThemeProvider({ children }) {
  const [tema, setTema] = useState(temaInicial)

  useEffect(() => {
    document.documentElement.dataset.tema = tema
    localStorage.setItem(THEME_KEY, tema)
  }, [tema])

  const alternar = () => setTema((t) => (t === 'escuro' ? 'claro' : 'escuro'))
  return <ThemeContext.Provider value={{ tema, alternar }}>{children}</ThemeContext.Provider>
}

export const useTheme = () => useContext(ThemeContext)
