import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from './context/AuthContext.jsx'
import Layout from './components/Layout.jsx'
import Login from './pages/Login.jsx'
import Cadastro from './pages/Cadastro.jsx'
import Inicio from './pages/Inicio.jsx'
import Pedidos from './pages/Pedidos.jsx'
import Automoveis from './pages/Automoveis.jsx'
import Contratos from './pages/Contratos.jsx'
import Clientes from './pages/Clientes.jsx'
import Perfil from './pages/Perfil.jsx'

/** Rotas protegidas: so acessiveis apos login (e, opcionalmente, para um papel especifico). */
function Protegida({ children, papel }) {
  const { usuario, carregando } = useAuth()
  if (carregando) return <div className="tela-carregando">Carregando...</div>
  if (!usuario) return <Navigate to="/login" replace />
  if (papel && usuario.papel !== papel) return <Navigate to="/" replace />
  return children
}

export default function App() {
  const { usuario } = useAuth()
  return (
    <Routes>
      <Route path="/login" element={usuario ? <Navigate to="/" /> : <Login />} />
      <Route path="/cadastro" element={usuario ? <Navigate to="/" /> : <Cadastro />} />
      <Route element={<Protegida><Layout /></Protegida>}>
        <Route index element={<Inicio />} />
        <Route path="pedidos" element={<Pedidos />} />
        <Route path="automoveis" element={<Automoveis />} />
        <Route path="contratos" element={<Contratos />} />
        <Route path="clientes" element={<Protegida papel="AGENTE"><Clientes /></Protegida>} />
        <Route path="perfil" element={<Protegida papel="CLIENTE"><Perfil /></Protegida>} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
