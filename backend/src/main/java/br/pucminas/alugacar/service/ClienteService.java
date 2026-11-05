package br.pucminas.alugacar.service;

import br.pucminas.alugacar.dto.ClienteDTOs.ClienteRequest;
import br.pucminas.alugacar.dto.ClienteDTOs.ClienteResponse;
import br.pucminas.alugacar.dto.ClienteDTOs.RendimentoDTO;
import br.pucminas.alugacar.exception.AcessoNegadoException;
import br.pucminas.alugacar.exception.RecursoNaoEncontradoException;
import br.pucminas.alugacar.exception.RegraNegocioException;
import br.pucminas.alugacar.model.Cliente;
import br.pucminas.alugacar.repository.ClienteRepository;
import br.pucminas.alugacar.repository.ContratoRepository;
import br.pucminas.alugacar.repository.PedidoAluguelRepository;
import br.pucminas.alugacar.repository.UsuarioRepository;
import br.pucminas.alugacar.security.UsuarioLogado;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** CRUD de cliente. */
@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final PedidoAluguelRepository pedidoRepository;
    private final ContratoRepository contratoRepository;
    private final PasswordEncoder passwordEncoder;

    public ClienteService(ClienteRepository clienteRepository, UsuarioRepository usuarioRepository,
                          PedidoAluguelRepository pedidoRepository, ContratoRepository contratoRepository,
                          PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.pedidoRepository = pedidoRepository;
        this.contratoRepository = contratoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ClienteResponse criar(ClienteRequest req) {
        String login = req.login().trim();
        String cpf = Normalizador.apenasDigitos(req.cpf());
        String rg = req.rg().trim();
        validarSenha(req.senha(), true);
        if (usuarioRepository.existsByLogin(login)) throw new RegraNegocioException("Usuario ja cadastrado");
        if (clienteRepository.existsByCpf(cpf)) throw new RegraNegocioException("CPF ja cadastrado");
        if (clienteRepository.existsByRg(rg)) throw new RegraNegocioException("RG ja cadastrado");

        Cliente cliente = new Cliente(req.nome().trim(), login, passwordEncoder.encode(req.senha()),
                Normalizador.texto(req.email()), rg, cpf, req.endereco().trim(), Normalizador.texto(req.profissao()));
        aplicarRendimentos(cliente, req.rendimentos());
        return ClienteResponse.de(clienteRepository.save(cliente));
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return clienteRepository.findAllByOrderByNomeAsc().stream().map(ClienteResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscar(Long id, UsuarioLogado logado) {
        verificarAcesso(id, logado, true);
        return ClienteResponse.de(obter(id));
    }

    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest req, UsuarioLogado logado) {
        verificarAcesso(id, logado, false);
        Cliente cliente = obter(id);
        String cpf = Normalizador.apenasDigitos(req.cpf());
        String rg = req.rg().trim();
        if (clienteRepository.existsByCpfAndIdNot(cpf, id)) throw new RegraNegocioException("CPF ja cadastrado");
        if (clienteRepository.existsByRgAndIdNot(rg, id)) throw new RegraNegocioException("RG ja cadastrado");

        cliente.setNome(req.nome().trim());
        cliente.setEmail(Normalizador.texto(req.email()));
        cliente.setCpf(cpf);
        cliente.setRg(rg);
        cliente.setEndereco(req.endereco().trim());
        cliente.setProfissao(Normalizador.texto(req.profissao()));
        if (req.senha() != null && !req.senha().isBlank()) {
            validarSenha(req.senha(), true);
            cliente.setSenha(passwordEncoder.encode(req.senha()));
        }
        cliente.limparRendimentos();
        clienteRepository.flush(); // remove os rendimentos antigos antes de inserir os novos
        aplicarRendimentos(cliente, req.rendimentos());
        return ClienteResponse.de(cliente);
    }

    @Transactional
    public void excluir(Long id, UsuarioLogado logado) {
        verificarAcesso(id, logado, false);
        Cliente cliente = obter(id);
        if (contratoRepository.existsByPedidoClienteId(id)) {
            throw new RegraNegocioException("Cliente possui contratos executados e nao pode ser excluido");
        }
        pedidoRepository.deleteAll(pedidoRepository.findByClienteId(id));
        clienteRepository.delete(cliente);
    }

    private void aplicarRendimentos(Cliente cliente, List<RendimentoDTO> rendimentos) {
        if (rendimentos == null) return;
        if (rendimentos.size() > Cliente.MAX_RENDIMENTOS) {
            throw new RegraNegocioException("Maximo de " + Cliente.MAX_RENDIMENTOS + " rendimentos");
        }
        rendimentos.forEach(r -> cliente.adicionarRendimento(r.entidadeEmpregadora().trim(), r.valor()));
    }

    private void validarSenha(String senha, boolean obrigatoria) {
        if (obrigatoria && (senha == null || senha.length() < 6)) {
            throw new RegraNegocioException("A senha deve ter pelo menos 6 caracteres");
        }
    }

    /** Cliente so acessa o proprio cadastro; agentes podem consultar (mas nao alterar) clientes. */
    private void verificarAcesso(Long id, UsuarioLogado logado, boolean agentePodeLer) {
        boolean proprio = logado.isCliente() && logado.id().equals(id);
        if (!proprio && !(agentePodeLer && logado.isAgente())) {
            throw new AcessoNegadoException("Voce nao tem permissao para acessar este cliente");
        }
    }

    private Cliente obter(Long id) {
        return clienteRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
    }
}
