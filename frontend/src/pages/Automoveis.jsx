import { useEffect, useRef, useState } from 'react'
import { api } from '../services/api.js'
import { useAuth } from '../context/AuthContext.jsx'
import { Alerta, Campo, CarroImagem, Modal, Vazio, lerImagemReduzida } from '../components/ui.jsx'

const ROTULO_PROPRIETARIO = { CLIENTE: 'Cliente', EMPRESA: 'Empresa', BANCO: 'Banco' }

export default function Automoveis() {
  const { isAgente } = useAuth()
  const [automoveis, setAutomoveis] = useState([])
  const [erro, setErro] = useState('')
  const [editando, setEditando] = useState(null) // null | {} (novo) | automovel

  const carregar = () => api.get('/api/automoveis').then(setAutomoveis).catch((e) => setErro(e.message))
  useEffect(() => { carregar() }, [])

  async function excluir(a) {
    if (!confirm(`Excluir ${a.marca} ${a.modelo} (${a.placa})?`)) return
    try {
      await api.delete(`/api/automoveis/${a.id}`)
      carregar()
    } catch (e) {
      setErro(e.message)
    }
  }

  return (
    <>
      <div className="cabecalho-pagina">
        <div>
          <h1>Automóveis</h1>
          <p className="subtitulo">Matrícula, ano, marca, modelo, placa e proprietário registrado.</p>
        </div>
        {isAgente && <button className="btn primario" onClick={() => setEditando({})}>+ Novo automóvel</button>}
      </div>
      <Alerta>{erro}</Alerta>

      {automoveis.length === 0 ? (
        <Vazio>Nenhum automóvel cadastrado.</Vazio>
      ) : (
        <div className="grade-carros">
          {automoveis.map((a) => (
            <article className="cartao carro-card" key={a.id}>
              <CarroImagem automovel={a} />
              <div className="carro-info">
                <div className="carro-titulo">
                  <h3>{a.marca} {a.modelo}</h3>
                  <span className="ano">{a.ano}</span>
                </div>
                <dl className="detalhes compacto">
                  <dt>Placa</dt><dd><code>{a.placa}</code></dd>
                  <dt>Matrícula</dt><dd>{a.matricula}</dd>
                  <dt>Proprietário</dt>
                  <dd>{a.proprietarioNome ? `${a.proprietarioNome} (${ROTULO_PROPRIETARIO[a.proprietarioTipo]})` : '—'}</dd>
                </dl>
                {isAgente && (
                  <div className="carro-acoes">
                    <button className="btn secundario pequeno" onClick={() => setEditando(a)}>Editar</button>
                    <button className="btn perigo pequeno" onClick={() => excluir(a)}>Excluir</button>
                  </div>
                )}
              </div>
            </article>
          ))}
        </div>
      )}

      {editando && (
        <AutomovelForm automovel={editando} onFechar={() => setEditando(null)}
                       onSalvo={() => { setEditando(null); carregar() }} />
      )}
    </>
  )
}

function AutomovelForm({ automovel, onFechar, onSalvo }) {
  const novo = !automovel.id
  const [form, setForm] = useState({
    matricula: automovel.matricula ?? '', ano: automovel.ano ?? new Date().getFullYear(),
    marca: automovel.marca ?? '', modelo: automovel.modelo ?? '', placa: automovel.placa ?? '',
    imagem: automovel.imagem ?? '',
  })
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)
  const arquivoRef = useRef(null)
  const set = (c) => (e) => setForm({ ...form, [c]: e.target.value })

  async function escolherImagem(e) {
    const arquivo = e.target.files?.[0]
    e.target.value = ''
    if (!arquivo) return
    try {
      setForm({ ...form, imagem: await lerImagemReduzida(arquivo) })
    } catch (err) {
      setErro(err.message)
    }
  }

  async function salvar(e) {
    e.preventDefault()
    setEnviando(true)
    try {
      const corpo = { ...form, ano: Number(form.ano), imagem: form.imagem || null }
      if (novo) await api.post('/api/automoveis', corpo)
      else await api.put(`/api/automoveis/${automovel.id}`, corpo)
      onSalvo()
    } catch (err) {
      setErro(err.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <Modal titulo={novo ? 'Novo automóvel' : 'Editar automóvel'} onFechar={onFechar}>
      <form onSubmit={salvar}>
        <Alerta>{erro}</Alerta>
        <div className="upload-imagem">
          <CarroImagem automovel={{ ...form, imagem: form.imagem }} />
          <div className="upload-acoes">
            <input ref={arquivoRef} type="file" accept="image/*" hidden onChange={escolherImagem} />
            <button type="button" className="btn secundario pequeno" onClick={() => arquivoRef.current.click()}>
              {form.imagem ? 'Trocar imagem' : 'Adicionar imagem'}
            </button>
            {form.imagem && (
              <button type="button" className="btn perigo pequeno" onClick={() => setForm({ ...form, imagem: '' })}>
                Remover
              </button>
            )}
            <small>PNG, JPG ou WebP. A imagem é reduzida automaticamente.</small>
          </div>
        </div>
        <div className="grade">
          <Campo label="Matrícula"><input required value={form.matricula} onChange={set('matricula')} /></Campo>
          <Campo label="Ano"><input type="number" min="1950" max="2100" required value={form.ano} onChange={set('ano')} /></Campo>
          <Campo label="Marca"><input required value={form.marca} onChange={set('marca')} /></Campo>
          <Campo label="Modelo"><input required value={form.modelo} onChange={set('modelo')} /></Campo>
          <Campo label="Placa"><input required placeholder="ABC1D23" value={form.placa} onChange={set('placa')} /></Campo>
        </div>
        <div className="acoes-form">
          <button type="button" className="btn secundario" onClick={onFechar}>Voltar</button>
          <button className="btn primario" disabled={enviando}>{enviando ? 'Salvando...' : 'Salvar'}</button>
        </div>
      </form>
    </Modal>
  )
}
