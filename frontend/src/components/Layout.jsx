import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import ThemeToggle from './ThemeToggle.jsx'

export default function Layout() {
  const { usuario, isAgente, isCliente, logout } = useAuth()
  const navigate = useNavigate()

  function sair() {
    logout()
    navigate('/login')
  }

  const papel = isAgente ? `Agente · ${usuario.tipoAgente === 'BANCO' ? 'Banco' : 'Empresa'}` : 'Cliente'

  return (
    <div className="app">
      <header className="topo">
        <NavLink to="/" className="marca">
          <img className="logo" src="/logo-icone.png" alt="" width="60" height="60" />
          <span>AlugaCar</span>
        </NavLink>
        <nav>
          <NavLink to="/" end>Início</NavLink>
          <NavLink to="/pedidos">{isAgente ? 'Pedidos' : 'Meus pedidos'}</NavLink>
          <NavLink to="/automoveis">Automóveis</NavLink>
          <NavLink to="/contratos">Contratos</NavLink>
          {isAgente && <NavLink to="/clientes">Clientes</NavLink>}
          {isCliente && <NavLink to="/perfil">Meu cadastro</NavLink>}
        </nav>
        <div className="usuario">
          <div>
            <strong>{usuario.nome}</strong>
            <small>{papel}</small>
          </div>
          <ThemeToggle />
          <button className="btn secundario pequeno" onClick={sair}>Sair</button>
        </div>
      </header>
      <main className="conteudo">
        <Outlet />
      </main>
    </div>
  )
}
