import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { Alerta, Campo } from '../components/ui.jsx'
import ThemeToggle from '../components/ThemeToggle.jsx'

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ login: '', senha: '' })
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)

  async function entrar(e) {
    e.preventDefault()
    setErro('')
    setEnviando(true)
    try {
      await login(form.login, form.senha)
      navigate('/')
    } catch (err) {
      setErro(err.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="pagina-auth">
      <ThemeToggle flutuante />
      <form className="cartao auth" onSubmit={entrar}>
        <img className="logo logo-auth" src="/logo.png" alt="AlugaCar" />
        <h1 className="sr-only">AlugaCar</h1>
        <p className="subtitulo">Gestão de aluguéis de automóveis</p>
        <Alerta>{erro}</Alerta>
        <Campo label="Usuário">
          <input autoFocus required value={form.login}
                 onChange={(e) => setForm({ ...form, login: e.target.value })} />
        </Campo>
        <Campo label="Senha">
          <input type="password" required value={form.senha}
                 onChange={(e) => setForm({ ...form, senha: e.target.value })} />
        </Campo>
        <button className="btn primario bloco" disabled={enviando}>
          {enviando ? 'Entrando...' : 'Entrar'}
        </button>
        <p className="rodape-auth">
          Ainda não tem cadastro? <Link to="/cadastro">Cadastre-se</Link>
        </p>
        <div className="dica">
          <strong>Usuários de demonstração</strong> (senha <code>123456</code>):<br />
          <code>cliente</code> · <code>locadora</code> (empresa) · <code>banco</code>
        </div>
      </form>
    </div>
  )
}
