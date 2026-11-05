import { useEffect, useState } from 'react'
import { api } from '../services/api.js'
import { Alerta, CarroImagem, Vazio, formatar } from '../components/ui.jsx'

const PROPRIEDADE = { CLIENTE: 'Cliente', EMPRESA: 'Empresa', BANCO: 'Banco' }

export default function Contratos() {
  const [contratos, setContratos] = useState([])
  const [erro, setErro] = useState('')

  useEffect(() => {
    api.get('/api/contratos').then(setContratos).catch((e) => setErro(e.message))
  }, [])

  return (
    <>
      <div className="cabecalho-pagina">
        <div>
          <h1>Contratos</h1>
          <p className="subtitulo">Contratos de aluguel executados e créditos associados.</p>
        </div>
      </div>
      <Alerta>{erro}</Alerta>
      {contratos.length === 0 ? (
        <Vazio>Nenhum contrato executado.</Vazio>
      ) : (
        <div className="lista-contratos">
          {contratos.map((c) => (
            <div className="cartao" key={c.id}>
              <div className="contrato-topo">
                <div className="celula-carro">
                  <CarroImagem automovel={c.automovel} className="medio" />
                  <div>
                    <h3>Contrato #{c.id}</h3>
                    <small>{c.automovel.marca} {c.automovel.modelo}</small>
                  </div>
                </div>
                <span className="badge contratado">Pedido #{c.pedidoId}</span>
              </div>
              <dl className="detalhes">
                <dt>Cliente</dt><dd>{c.cliente} ({formatar.cpf(c.clienteCpf)})</dd>
                <dt>Automóvel</dt><dd>{c.automovel.marca} {c.automovel.modelo} · {c.automovel.placa}</dd>
                <dt>Período</dt><dd>{formatar.data(c.dataInicio)} a {formatar.data(c.dataFim)}</dd>
                <dt>Agente</dt><dd>{c.agente} ({c.tipoAgente === 'BANCO' ? 'Banco' : 'Empresa'})</dd>
                <dt>Propriedade</dt><dd>{PROPRIEDADE[c.tipoPropriedade]}</dd>
                <dt>Assinado em</dt><dd>{formatar.dataHora(c.dataAssinatura)}</dd>
              </dl>
              {c.credito && (
                <div className="credito">
                  <strong>Contrato de crédito · {c.credito.banco}</strong>
                  <span>{formatar.moeda(c.credito.valor)} em {c.credito.numeroParcelas}x de {formatar.moeda(c.credito.valorParcela)} ({c.credito.taxaJurosMensal}% a.m.)</span>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </>
  )
}
