import { useEffect, useState } from 'react'
import { api } from '../services/api.js'
import { Alerta, Modal, Vazio, formatar } from '../components/ui.jsx'

/** Consulta de clientes (agentes) para apoiar a analise financeira. */
export default function Clientes() {
  const [clientes, setClientes] = useState([])
  const [busca, setBusca] = useState('')
  const [selecionado, setSelecionado] = useState(null)
  const [erro, setErro] = useState('')

  useEffect(() => {
    api.get('/api/clientes').then(setClientes).catch((e) => setErro(e.message))
  }, [])

  const termo = busca.toLowerCase()
  const filtrados = clientes.filter((c) => c.nome.toLowerCase().includes(termo) || c.cpf.includes(termo.replace(/\D/g, '') || '§'))

  return (
    <>
      <div className="cabecalho-pagina">
        <div>
          <h1>Clientes</h1>
          <p className="subtitulo">Contratantes cadastrados e seus rendimentos.</p>
        </div>
        <input className="busca" placeholder="Buscar por nome ou CPF" value={busca} onChange={(e) => setBusca(e.target.value)} />
      </div>
      <Alerta>{erro}</Alerta>
      {filtrados.length === 0 ? (
        <Vazio>Nenhum cliente encontrado.</Vazio>
      ) : (
        <div className="cartao sem-padding">
          <table>
            <thead><tr><th>Nome</th><th>CPF</th><th>Profissão</th><th>Renda total</th><th className="acoes">Ações</th></tr></thead>
            <tbody>
              {filtrados.map((c) => (
                <tr key={c.id}>
                  <td>{c.nome}</td>
                  <td>{formatar.cpf(c.cpf)}</td>
                  <td>{c.profissao || '—'}</td>
                  <td>{formatar.moeda(c.rendaTotal)}</td>
                  <td className="acoes"><button className="btn link" onClick={() => setSelecionado(c)}>Ver</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      {selecionado && (
        <Modal titulo={selecionado.nome} onFechar={() => setSelecionado(null)}>
          <dl className="detalhes">
            <dt>Usuário</dt><dd>{selecionado.login}</dd>
            <dt>E-mail</dt><dd>{selecionado.email || '—'}</dd>
            <dt>RG</dt><dd>{selecionado.rg}</dd>
            <dt>CPF</dt><dd>{formatar.cpf(selecionado.cpf)}</dd>
            <dt>Endereço</dt><dd>{selecionado.endereco}</dd>
            <dt>Profissão</dt><dd>{selecionado.profissao || '—'}</dd>
          </dl>
          <h4 className="secao">Rendimentos</h4>
          {selecionado.rendimentos.length === 0 ? <p className="subtitulo">Nenhum rendimento informado.</p> : (
            <ul className="rendimentos">
              {selecionado.rendimentos.map((r, i) => (
                <li key={i}><span>{r.entidadeEmpregadora}</span><strong>{formatar.moeda(r.valor)}</strong></li>
              ))}
              <li className="total"><span>Total</span><strong>{formatar.moeda(selecionado.rendaTotal)}</strong></li>
            </ul>
          )}
        </Modal>
      )}
    </>
  )
}
