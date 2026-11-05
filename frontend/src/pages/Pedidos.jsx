import { useEffect, useState } from 'react'
import { api } from '../services/api.js'
import { useAuth } from '../context/AuthContext.jsx'
import { Alerta, Campo, CarroImagem, Modal, StatusBadge, Vazio, formatar } from '../components/ui.jsx'

const hoje = () => new Date().toISOString().slice(0, 10)

export default function Pedidos() {
  const { isAgente, isCliente } = useAuth()
  const [pedidos, setPedidos] = useState([])
  const [automoveis, setAutomoveis] = useState([])
  const [filtro, setFiltro] = useState('TODOS')
  const [erro, setErro] = useState('')
  const [sucesso, setSucesso] = useState('')
  const [modal, setModal] = useState(null) // { tipo: 'form'|'avaliar'|'contrato'|'detalhe', pedido }

  async function carregar() {
    try {
      const [p, a] = await Promise.all([api.get('/api/pedidos'), api.get('/api/automoveis')])
      setPedidos(p)
      setAutomoveis(a)
    } catch (e) {
      setErro(e.message)
    }
  }
  useEffect(() => { carregar() }, [])

  function concluir(msg) {
    setModal(null)
    setSucesso(msg)
    setErro('')
    carregar()
  }

  async function cancelar(p) {
    if (!confirm(`Cancelar o pedido #${p.id}?`)) return
    try {
      await api.patch(`/api/pedidos/${p.id}/cancelar`)
      concluir(`Pedido #${p.id} cancelado.`)
    } catch (e) {
      setErro(e.message)
    }
  }

  const visiveis = filtro === 'TODOS' ? pedidos : pedidos.filter((p) => p.status === filtro)

  return (
    <>
      <div className="cabecalho-pagina">
        <div>
          <h1>{isAgente ? 'Pedidos de aluguel' : 'Meus pedidos'}</h1>
          <p className="subtitulo">
            {isAgente ? 'Modifique, avalie e execute contratos.' : 'Introduza, modifique, consulte e cancele seus pedidos.'}
          </p>
        </div>
        {isCliente && (
          <button className="btn primario" onClick={() => setModal({ tipo: 'form' })}>+ Novo pedido</button>
        )}
      </div>

      <Alerta>{erro}</Alerta>
      <Alerta tipo="sucesso">{sucesso}</Alerta>

      <div className="filtros">
        {['TODOS', 'PENDENTE', 'APROVADO', 'CONTRATADO', 'REPROVADO', 'CANCELADO'].map((s) => (
          <button key={s} className={filtro === s ? 'ativo' : ''} onClick={() => setFiltro(s)}>
            {s === 'TODOS' ? 'Todos' : s.charAt(0) + s.slice(1).toLowerCase()}
          </button>
        ))}
      </div>

      {visiveis.length === 0 ? (
        <Vazio>Nenhum pedido encontrado.</Vazio>
      ) : (
        <div className="cartao sem-padding">
          <table>
            <thead>
              <tr>
                <th>#</th>
                {isAgente && <th>Cliente</th>}
                <th>Automóvel</th>
                <th>Período</th>
                <th>Crédito</th>
                <th>Status</th>
                <th className="acoes">Ações</th>
              </tr>
            </thead>
            <tbody>
              {visiveis.map((p) => (
                <tr key={p.id}>
                  <td>{p.id}</td>
                  {isAgente && <td>{p.cliente.nome}<br /><small>Renda: {formatar.moeda(p.cliente.rendaTotal)}</small></td>}
                  <td>
                    <div className="celula-carro">
                      <CarroImagem automovel={p.automovel} className="mini" />
                      <div>{p.automovel.marca} {p.automovel.modelo}<br /><small>{p.automovel.placa}</small></div>
                    </div>
                  </td>
                  <td>{formatar.data(p.dataInicio)} a {formatar.data(p.dataFim)}<br /><small>{p.quantidadeDias} dia(s)</small></td>
                  <td>{p.solicitaCredito ? 'Sim' : 'Não'}</td>
                  <td><StatusBadge status={p.status} /></td>
                  <td className="acoes">
                    <button className="btn link" onClick={() => setModal({ tipo: 'detalhe', pedido: p })}>Ver</button>
                    {isCliente && p.status === 'PENDENTE' && (
                      <button className="btn link" onClick={() => setModal({ tipo: 'form', pedido: p })}>Modificar</button>
                    )}
                    {isCliente && ['PENDENTE', 'APROVADO'].includes(p.status) && (
                      <button className="btn link perigo" onClick={() => cancelar(p)}>Cancelar</button>
                    )}
                    {isAgente && ['PENDENTE', 'APROVADO'].includes(p.status) && (
                      <button className="btn link" onClick={() => setModal({ tipo: 'form', pedido: p })}>Modificar</button>
                    )}
                    {isAgente && p.status === 'PENDENTE' && (
                      <button className="btn link" onClick={() => setModal({ tipo: 'avaliar', pedido: p })}>Avaliar</button>
                    )}
                    {isAgente && p.status === 'APROVADO' && (
                      <button className="btn link sucesso" onClick={() => setModal({ tipo: 'contrato', pedido: p })}>Executar contrato</button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {modal?.tipo === 'form' && (
        <PedidoForm pedido={modal.pedido} automoveis={automoveis} onFechar={() => setModal(null)}
                    onSalvo={(p) => concluir(modal.pedido ? `Pedido #${p.id} modificado.` : `Pedido #${p.id} criado com status Pendente.`)} />
      )}
      {modal?.tipo === 'avaliar' && (
        <AvaliacaoForm pedido={modal.pedido} onFechar={() => setModal(null)}
                       onSalvo={(p) => concluir(`Pedido #${p.id} ${p.status === 'APROVADO' ? 'aprovado' : 'reprovado'}.`)} />
      )}
      {modal?.tipo === 'contrato' && (
        <ContratoForm pedido={modal.pedido} onFechar={() => setModal(null)}
                      onSalvo={(c) => concluir(`Contrato #${c.id} executado para o pedido #${c.pedidoId}.`)} />
      )}
      {modal?.tipo === 'detalhe' && <DetalhePedido pedido={modal.pedido} onFechar={() => setModal(null)} />}
    </>
  )
}

function PedidoForm({ pedido, automoveis, onFechar, onSalvo }) {
  const [form, setForm] = useState({
    automovelId: pedido?.automovel.id ?? '',
    dataInicio: pedido?.dataInicio ?? '',
    dataFim: pedido?.dataFim ?? '',
    observacoes: pedido?.observacoes ?? '',
    solicitaCredito: pedido?.solicitaCredito ?? false,
  })
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)

  async function salvar(e) {
    e.preventDefault()
    setEnviando(true)
    setErro('')
    try {
      const corpo = { ...form, automovelId: Number(form.automovelId) }
      const salvo = pedido ? await api.put(`/api/pedidos/${pedido.id}`, corpo) : await api.post('/api/pedidos', corpo)
      onSalvo(salvo)
    } catch (err) {
      setErro(err.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <Modal titulo={pedido ? `Modificar pedido #${pedido.id}` : 'Novo pedido de aluguel'} onFechar={onFechar}>
      <form onSubmit={salvar}>
        <Alerta>{erro}</Alerta>
        <span className="rotulo">Automóvel</span>
        <div className="escolha-carros" role="radiogroup" aria-label="Automóvel">
          {automoveis.map((a) => (
            <label key={a.id} className={`escolha-carro ${String(form.automovelId) === String(a.id) ? 'selecionado' : ''}`}>
              <input type="radio" name="automovel" required value={a.id}
                     checked={String(form.automovelId) === String(a.id)}
                     onChange={(e) => setForm({ ...form, automovelId: e.target.value })} />
              <CarroImagem automovel={a} />
              <strong>{a.marca} {a.modelo}</strong>
              <small>{a.ano} · {a.placa}</small>
            </label>
          ))}
        </div>
        <div className="grade">
          <Campo label="Data de início">
            <input type="date" required min={pedido ? undefined : hoje()} value={form.dataInicio}
                   onChange={(e) => setForm({ ...form, dataInicio: e.target.value })} />
          </Campo>
          <Campo label="Data de fim">
            <input type="date" required min={form.dataInicio || hoje()} value={form.dataFim}
                   onChange={(e) => setForm({ ...form, dataFim: e.target.value })} />
          </Campo>
        </div>
        <Campo label="Observações">
          <textarea rows={3} maxLength={500} value={form.observacoes}
                    onChange={(e) => setForm({ ...form, observacoes: e.target.value })} />
        </Campo>
        <label className="checkbox">
          <input type="checkbox" checked={form.solicitaCredito}
                 onChange={(e) => setForm({ ...form, solicitaCredito: e.target.checked })} />
          Desejo associar um contrato de crédito (concedido por um banco agente)
        </label>
        <div className="acoes-form">
          <button type="button" className="btn secundario" onClick={onFechar}>Voltar</button>
          <button className="btn primario" disabled={enviando}>{enviando ? 'Salvando...' : 'Salvar'}</button>
        </div>
      </form>
    </Modal>
  )
}

function AvaliacaoForm({ pedido, onFechar, onSalvo }) {
  const [parecer, setParecer] = useState('')
  const [erro, setErro] = useState('')

  async function avaliar(aprovado) {
    if (!parecer.trim()) {
      setErro('Informe o parecer financeiro.')
      return
    }
    try {
      onSalvo(await api.patch(`/api/pedidos/${pedido.id}/avaliar`, { aprovado, parecer }))
    } catch (err) {
      setErro(err.message)
    }
  }

  return (
    <Modal titulo={`Análise financeira · Pedido #${pedido.id}`} onFechar={onFechar}>
      <Alerta>{erro}</Alerta>
      <CarroImagem automovel={pedido.automovel} className="destaque" />
      <dl className="detalhes">
        <dt>Cliente</dt><dd>{pedido.cliente.nome} ({formatar.cpf(pedido.cliente.cpf)})</dd>
        <dt>Profissão</dt><dd>{pedido.cliente.profissao || '—'}</dd>
        <dt>Renda total</dt><dd>{formatar.moeda(pedido.cliente.rendaTotal)}</dd>
        <dt>Automóvel</dt><dd>{pedido.automovel.marca} {pedido.automovel.modelo} ({pedido.automovel.ano})</dd>
        <dt>Período</dt><dd>{formatar.data(pedido.dataInicio)} a {formatar.data(pedido.dataFim)} ({pedido.quantidadeDias} dias)</dd>
        <dt>Crédito</dt><dd>{pedido.solicitaCredito ? 'Solicitado' : 'Não solicitado'}</dd>
      </dl>
      <Campo label="Parecer">
        <textarea rows={3} maxLength={500} value={parecer} onChange={(e) => setParecer(e.target.value)} />
      </Campo>
      <div className="acoes-form">
        <button className="btn perigo" onClick={() => avaliar(false)}>Parecer negativo</button>
        <button className="btn sucesso" onClick={() => avaliar(true)}>Parecer positivo</button>
      </div>
    </Modal>
  )
}

function ContratoForm({ pedido, onFechar, onSalvo }) {
  const { isBanco } = useAuth()
  const [tipoPropriedade, setTipoPropriedade] = useState('CLIENTE')
  const [comCredito, setComCredito] = useState(isBanco && pedido.solicitaCredito)
  const [credito, setCredito] = useState({ valor: '', taxaJurosMensal: '1.5', numeroParcelas: '12' })
  const [erro, setErro] = useState('')

  async function executar(e) {
    e.preventDefault()
    try {
      const corpo = {
        tipoPropriedade,
        credito: comCredito
          ? { valor: Number(credito.valor), taxaJurosMensal: Number(credito.taxaJurosMensal), numeroParcelas: Number(credito.numeroParcelas) }
          : null,
      }
      onSalvo(await api.post(`/api/pedidos/${pedido.id}/contrato`, corpo))
    } catch (err) {
      setErro(err.message)
    }
  }

  return (
    <Modal titulo={`Executar contrato · Pedido #${pedido.id}`} onFechar={onFechar}>
      <form onSubmit={executar}>
        <Alerta>{erro}</Alerta>
        <Campo label="Registrar automóvel como propriedade de">
          <select value={tipoPropriedade} onChange={(e) => setTipoPropriedade(e.target.value)}>
            <option value="CLIENTE">Cliente ({pedido.cliente.nome})</option>
            <option value="EMPRESA" disabled={isBanco}>Empresa (este agente)</option>
            <option value="BANCO" disabled={!isBanco}>Banco (este agente)</option>
          </select>
        </Campo>
        {isBanco ? (
          <label className="checkbox">
            <input type="checkbox" checked={comCredito} onChange={(e) => setComCredito(e.target.checked)} />
            Associar contrato de crédito concedido por este banco
          </label>
        ) : (
          pedido.solicitaCredito && (
            <Alerta tipo="info">O cliente solicitou crédito, mas apenas bancos agentes podem concedê-lo.</Alerta>
          )
        )}
        {comCredito && (
          <div className="grade tres">
            <Campo label="Valor (R$)">
              <input type="number" min="1" step="0.01" required value={credito.valor}
                     onChange={(e) => setCredito({ ...credito, valor: e.target.value })} />
            </Campo>
            <Campo label="Juros (% a.m.)">
              <input type="number" min="0" max="100" step="0.01" required value={credito.taxaJurosMensal}
                     onChange={(e) => setCredito({ ...credito, taxaJurosMensal: e.target.value })} />
            </Campo>
            <Campo label="Parcelas">
              <input type="number" min="1" max="120" required value={credito.numeroParcelas}
                     onChange={(e) => setCredito({ ...credito, numeroParcelas: e.target.value })} />
            </Campo>
          </div>
        )}
        <div className="acoes-form">
          <button type="button" className="btn secundario" onClick={onFechar}>Voltar</button>
          <button className="btn sucesso">Executar contrato</button>
        </div>
      </form>
    </Modal>
  )
}

function DetalhePedido({ pedido: p, onFechar }) {
  return (
    <Modal titulo={`Pedido #${p.id}`} onFechar={onFechar}>
      <CarroImagem automovel={p.automovel} className="destaque" />
      <dl className="detalhes">
        <dt>Status</dt><dd><StatusBadge status={p.status} /></dd>
        <dt>Cliente</dt><dd>{p.cliente.nome}</dd>
        <dt>Automóvel</dt><dd>{p.automovel.marca} {p.automovel.modelo} ({p.automovel.ano}) · {p.automovel.placa}</dd>
        <dt>Período</dt><dd>{formatar.data(p.dataInicio)} a {formatar.data(p.dataFim)} ({p.quantidadeDias} dias)</dd>
        <dt>Criado em</dt><dd>{formatar.dataHora(p.dataCriacao)}</dd>
        <dt>Crédito</dt><dd>{p.solicitaCredito ? 'Solicitado' : 'Não solicitado'}</dd>
        <dt>Observações</dt><dd>{p.observacoes || '—'}</dd>
        <dt>Avaliado por</dt><dd>{p.agenteAvaliador || '—'}</dd>
        <dt>Parecer</dt><dd>{p.parecer || '—'}</dd>
        <dt>Avaliado em</dt><dd>{formatar.dataHora(p.dataAvaliacao)}</dd>
        {p.contratoId && (<><dt>Contrato</dt><dd>#{p.contratoId}</dd></>)}
      </dl>
    </Modal>
  )
}
