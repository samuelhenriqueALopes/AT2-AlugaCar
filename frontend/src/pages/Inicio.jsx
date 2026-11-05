import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../services/api.js'
import { useAuth } from '../context/AuthContext.jsx'
import { Alerta, CarroImagem, StatusBadge, formatar } from '../components/ui.jsx'

const STATUS = ['PENDENTE', 'APROVADO', 'CONTRATADO', 'REPROVADO', 'CANCELADO']

export default function Inicio() {
  const { usuario, isAgente } = useAuth()
  const [pedidos, setPedidos] = useState([])
  const [erro, setErro] = useState('')

  useEffect(() => {
    api.get('/api/pedidos').then(setPedidos).catch((e) => setErro(e.message))
  }, [])

  const contagem = Object.fromEntries(STATUS.map((s) => [s, pedidos.filter((p) => p.status === s).length]))
  const recentes = pedidos.slice(0, 5)

  return (
    <>
      <div className="cabecalho-pagina">
        <div>
          <h1>Olá, {isAgente ? usuario.nome : usuario.nome.split(' ')[0]}!</h1>
          <p className="subtitulo">
            {isAgente
              ? 'Analise financeiramente os pedidos e execute os contratos aprovados.'
              : 'Acompanhe o status dos seus pedidos de aluguel.'}
          </p>
        </div>
        <Link className="btn primario" to="/pedidos">{isAgente ? 'Avaliar pedidos' : 'Novo pedido'}</Link>
      </div>
      <Alerta>{erro}</Alerta>

      <div className="indicadores">
        {STATUS.map((s) => (
          <div className="cartao indicador" key={s}>
            <StatusBadge status={s} />
            <strong>{contagem[s]}</strong>
          </div>
        ))}
      </div>

      <div className="cartao">
        <h3>Pedidos recentes</h3>
        {recentes.length === 0 ? (
          <p className="subtitulo">Nenhum pedido ainda.</p>
        ) : (
          <table>
            <thead>
              <tr><th>#</th>{isAgente && <th>Cliente</th>}<th>Automóvel</th><th>Período</th><th>Status</th></tr>
            </thead>
            <tbody>
              {recentes.map((p) => (
                <tr key={p.id}>
                  <td>{p.id}</td>
                  {isAgente && <td>{p.cliente.nome}</td>}
                  <td>
                    <div className="celula-carro">
                      <CarroImagem automovel={p.automovel} className="mini" />
                      <span>{p.automovel.marca} {p.automovel.modelo}</span>
                    </div>
                  </td>
                  <td>{formatar.data(p.dataInicio)} a {formatar.data(p.dataFim)}</td>
                  <td><StatusBadge status={p.status} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </>
  )
}
