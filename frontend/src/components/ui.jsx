// Componentes de interface reutilizaveis.

export function Modal({ titulo, onFechar, children, largo }) {
  return (
    <div className="modal-fundo" onMouseDown={(e) => e.target === e.currentTarget && onFechar()}>
      <div className={`modal ${largo ? 'largo' : ''}`} role="dialog" aria-modal="true">
        <div className="modal-cabecalho">
          <h3>{titulo}</h3>
          <button className="fechar" onClick={onFechar} aria-label="Fechar">×</button>
        </div>
        {children}
      </div>
    </div>
  )
}

export function Alerta({ tipo = 'erro', children }) {
  if (!children) return null
  return <div className={`alerta ${tipo}`}>{children}</div>
}

export function Campo({ label, erro, children }) {
  return (
    <label className="campo">
      <span>{label}</span>
      {children}
      {erro && <small className="erro-campo">{erro}</small>}
    </label>
  )
}

const ROTULOS_STATUS = {
  PENDENTE: 'Pendente',
  APROVADO: 'Aprovado',
  REPROVADO: 'Reprovado',
  CANCELADO: 'Cancelado',
  CONTRATADO: 'Contratado',
}

export function StatusBadge({ status }) {
  return <span className={`badge ${status.toLowerCase()}`}>{ROTULOS_STATUS[status] ?? status}</span>
}

/** Imagem do automovel; sem imagem, mostra o icone da logo. */
export function CarroImagem({ automovel, className = '' }) {
  const nome = automovel ? `${automovel.marca} ${automovel.modelo}` : ''
  if (!automovel?.imagem) {
    return (
      <div className={`carro-img sem-imagem ${className}`} aria-label={nome}>
        <img className="logo" src="/logo-icone.png" alt="" />
      </div>
    )
  }
  return <img className={`carro-img ${className}`} src={automovel.imagem} alt={nome} loading="lazy" />
}

/** Le um arquivo de imagem e reduz para no maximo 960px (WebP), retornando uma data URL. */
export function lerImagemReduzida(arquivo, maxLado = 960) {
  return new Promise((resolve, reject) => {
    if (!arquivo.type.startsWith('image/')) return reject(new Error('Selecione um arquivo de imagem.'))
    const url = URL.createObjectURL(arquivo)
    const img = new Image()
    img.onload = () => {
      const escala = Math.min(1, maxLado / Math.max(img.width, img.height))
      const canvas = document.createElement('canvas')
      canvas.width = Math.round(img.width * escala)
      canvas.height = Math.round(img.height * escala)
      canvas.getContext('2d').drawImage(img, 0, 0, canvas.width, canvas.height)
      URL.revokeObjectURL(url)
      resolve(canvas.toDataURL('image/webp', 0.85))
    }
    img.onerror = () => { URL.revokeObjectURL(url); reject(new Error('Não foi possível ler a imagem.')) }
    img.src = url
  })
}

export function Vazio({ children }) {
  return <div className="vazio">{children}</div>
}

export const formatar = {
  data: (d) => (d ? new Date(d + (d.length === 10 ? 'T00:00:00' : '')).toLocaleDateString('pt-BR') : '—'),
  dataHora: (d) => (d ? new Date(d).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' }) : '—'),
  moeda: (v) => (v == null ? '—' : Number(v).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })),
  cpf: (c) => (c?.length === 11 ? c.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4') : c),
  cnpj: (c) => (c?.length === 14 ? c.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, '$1.$2.$3/$4-$5') : c),
}
