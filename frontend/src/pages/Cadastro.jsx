import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api } from '../services/api.js'
import { useAuth } from '../context/AuthContext.jsx'
import ClienteForm from '../components/ClienteForm.jsx'
import { Alerta, Campo } from '../components/ui.jsx'
import ThemeToggle from '../components/ThemeToggle.jsx'

/** Cadastro previo obrigatorio: cliente (usuario individual) ou agente (empresa/banco). */
export default function Cadastro() {
  const [tipo, setTipo] = useState('CLIENTE')
  const [erro, setErro] = useState('')
  const [erros, setErros] = useState({})
  const [enviando, setEnviando] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()

  async function cadastrar(caminho, dados) {
    setErro('')
    setErros({})
    setEnviando(true)
    try {
      await api.post(caminho, dados)
      await login(dados.login, dados.senha)
      navigate('/')
    } catch (err) {
      setErro(err.message)
      setErros(err.campos || {})
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="pagina-auth">
      <ThemeToggle flutuante />
      <div className="cartao auth largo">
        <div className="marca grande">
          <img className="logo" src="/logo-icone.png" alt="" width="64" height="64" />
          <span>Criar conta</span>
        </div>
        <div className="abas">
          <button className={tipo === 'CLIENTE' ? 'ativa' : ''} onClick={() => setTipo('CLIENTE')}>Sou cliente</button>
          <button className={tipo === 'AGENTE' ? 'ativa' : ''} onClick={() => setTipo('AGENTE')}>Sou agente (empresa/banco)</button>
        </div>
        <Alerta>{erro}</Alerta>
        {tipo === 'CLIENTE' ? (
          <ClienteForm erros={erros} enviando={enviando} rotuloBotao="Cadastrar"
                       onSubmit={(d) => cadastrar('/api/clientes', d)} />
        ) : (
          <AgenteForm erros={erros} enviando={enviando} onSubmit={(d) => cadastrar('/api/agentes', d)} />
        )}
        <p className="rodape-auth">Já tem conta? <Link to="/login">Entrar</Link></p>
      </div>
    </div>
  )
}

function AgenteForm({ erros, enviando, onSubmit }) {
  const [form, setForm] = useState({ nome: '', login: '', senha: '', email: '', cnpj: '', tipo: 'EMPRESA' })
  const set = (c) => (e) => setForm({ ...form, [c]: e.target.value })
  return (
    <form onSubmit={(e) => { e.preventDefault(); onSubmit(form) }}>
      <div className="grade">
        <Campo label="Usuário" erro={erros.login}><input required minLength={3} value={form.login} onChange={set('login')} /></Campo>
        <Campo label="Senha" erro={erros.senha}><input type="password" required minLength={6} value={form.senha} onChange={set('senha')} /></Campo>
        <Campo label="Razão social" erro={erros.nome}><input required value={form.nome} onChange={set('nome')} /></Campo>
        <Campo label="E-mail" erro={erros.email}><input type="email" value={form.email} onChange={set('email')} /></Campo>
        <Campo label="CNPJ" erro={erros.cnpj}><input required placeholder="00.000.000/0000-00" value={form.cnpj} onChange={set('cnpj')} /></Campo>
        <Campo label="Tipo de agente" erro={erros.tipo}>
          <select value={form.tipo} onChange={set('tipo')}>
            <option value="EMPRESA">Empresa</option>
            <option value="BANCO">Banco</option>
          </select>
        </Campo>
      </div>
      <div className="acoes-form">
        <button className="btn primario" disabled={enviando}>{enviando ? 'Salvando...' : 'Cadastrar'}</button>
      </div>
    </form>
  )
}
