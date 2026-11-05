import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../services/api.js'
import { useAuth } from '../context/AuthContext.jsx'
import ClienteForm from '../components/ClienteForm.jsx'
import { Alerta } from '../components/ui.jsx'

/** CRUD do proprio cadastro do cliente: consultar, alterar e excluir. */
export default function Perfil() {
  const { usuario, atualizarUsuario, logout } = useAuth()
  const navigate = useNavigate()
  const [cliente, setCliente] = useState(null)
  const [erro, setErro] = useState('')
  const [erros, setErros] = useState({})
  const [sucesso, setSucesso] = useState('')
  const [enviando, setEnviando] = useState(false)

  useEffect(() => {
    api.get(`/api/clientes/${usuario.id}`).then(setCliente).catch((e) => setErro(e.message))
  }, [usuario.id])

  async function salvar(dados) {
    setEnviando(true)
    setErro('')
    setErros({})
    setSucesso('')
    try {
      const atualizado = await api.put(`/api/clientes/${usuario.id}`, dados)
      setCliente(atualizado)
      atualizarUsuario({ ...usuario, nome: atualizado.nome, email: atualizado.email })
      setSucesso('Cadastro atualizado com sucesso.')
    } catch (err) {
      setErro(err.message)
      setErros(err.campos || {})
    } finally {
      setEnviando(false)
    }
  }

  async function excluir() {
    if (!confirm('Excluir definitivamente sua conta? Seus pedidos também serão removidos.')) return
    try {
      await api.delete(`/api/clientes/${usuario.id}`)
      logout()
      navigate('/login')
    } catch (err) {
      setErro(err.message)
    }
  }

  return (
    <>
      <div className="cabecalho-pagina">
        <div>
          <h1>Meu cadastro</h1>
          <p className="subtitulo">Dados de identificação, profissão e rendimentos (máximo 3).</p>
        </div>
      </div>
      <Alerta>{erro}</Alerta>
      <Alerta tipo="sucesso">{sucesso}</Alerta>
      {cliente && (
        <div className="cartao">
          <ClienteForm key={cliente.id} inicial={cliente} edicao erros={erros} enviando={enviando}
                       rotuloBotao="Salvar alterações" onSubmit={salvar}>
            <button type="button" className="btn perigo" onClick={excluir}>Excluir conta</button>
            <span className="espaco" />
          </ClienteForm>
        </div>
      )}
    </>
  )
}
