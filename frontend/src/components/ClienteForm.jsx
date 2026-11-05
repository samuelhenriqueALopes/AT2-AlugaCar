import { useState } from 'react'
import { Campo } from './ui.jsx'

const MAX_RENDIMENTOS = 3
const VAZIO = {
  nome: '', login: '', senha: '', email: '', rg: '', cpf: '', endereco: '', profissao: '', rendimentos: [],
}

/** Formulario de dados do contratante: identificacao, profissao e ate 3 rendimentos. */
export default function ClienteForm({ inicial, edicao = false, erros = {}, enviando, rotuloBotao, onSubmit, children }) {
  const [form, setForm] = useState(() => ({ ...VAZIO, ...inicial, senha: '' }))

  const set = (campo) => (e) => setForm({ ...form, [campo]: e.target.value })

  function setRendimento(i, campo, valor) {
    const rendimentos = form.rendimentos.map((r, idx) => (idx === i ? { ...r, [campo]: valor } : r))
    setForm({ ...form, rendimentos })
  }

  function enviar(e) {
    e.preventDefault()
    onSubmit({
      ...form,
      rendimentos: form.rendimentos
        .filter((r) => r.entidadeEmpregadora?.trim())
        .map((r) => ({ entidadeEmpregadora: r.entidadeEmpregadora, valor: Number(r.valor || 0) })),
    })
  }

  return (
    <form onSubmit={enviar}>
      <h4 className="secao">Acesso</h4>
      <div className="grade">
        <Campo label="Usuário" erro={erros.login}>
          <input required minLength={3} value={form.login} onChange={set('login')} disabled={edicao} />
        </Campo>
        <Campo label={edicao ? 'Nova senha (opcional)' : 'Senha'} erro={erros.senha}>
          <input type="password" required={!edicao} minLength={6} value={form.senha} onChange={set('senha')} />
        </Campo>
      </div>

      <h4 className="secao">Identificação</h4>
      <div className="grade">
        <Campo label="Nome completo" erro={erros.nome}>
          <input required value={form.nome} onChange={set('nome')} />
        </Campo>
        <Campo label="E-mail" erro={erros.email}>
          <input type="email" value={form.email ?? ''} onChange={set('email')} />
        </Campo>
        <Campo label="RG" erro={erros.rg}>
          <input required value={form.rg} onChange={set('rg')} />
        </Campo>
        <Campo label="CPF" erro={erros.cpf}>
          <input required placeholder="000.000.000-00" value={form.cpf} onChange={set('cpf')} />
        </Campo>
      </div>
      <Campo label="Endereço" erro={erros.endereco}>
        <input required value={form.endereco} onChange={set('endereco')} />
      </Campo>
      <Campo label="Profissão" erro={erros.profissao}>
        <input value={form.profissao ?? ''} onChange={set('profissao')} />
      </Campo>

      <h4 className="secao">
        Rendimentos auferidos <small>({form.rendimentos.length}/{MAX_RENDIMENTOS})</small>
      </h4>
      {form.rendimentos.map((r, i) => (
        <div className="linha-rendimento" key={i}>
          <input placeholder="Entidade empregadora" required value={r.entidadeEmpregadora}
                 onChange={(e) => setRendimento(i, 'entidadeEmpregadora', e.target.value)} />
          <input type="number" min="0" step="0.01" placeholder="Valor (R$)" required value={r.valor}
                 onChange={(e) => setRendimento(i, 'valor', e.target.value)} />
          <button type="button" className="btn perigo pequeno"
                  onClick={() => setForm({ ...form, rendimentos: form.rendimentos.filter((_, idx) => idx !== i) })}>
            Remover
          </button>
        </div>
      ))}
      {form.rendimentos.length < MAX_RENDIMENTOS && (
        <button type="button" className="btn secundario pequeno"
                onClick={() => setForm({ ...form, rendimentos: [...form.rendimentos, { entidadeEmpregadora: '', valor: '' }] })}>
          + Adicionar rendimento
        </button>
      )}
      {erros.rendimentos && <small className="erro-campo">{erros.rendimentos}</small>}

      <div className="acoes-form">
        {children}
        <button className="btn primario" disabled={enviando}>{enviando ? 'Salvando...' : rotuloBotao}</button>
      </div>
    </form>
  )
}
