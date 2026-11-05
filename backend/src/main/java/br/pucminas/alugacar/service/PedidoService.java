package br.pucminas.alugacar.service;

import br.pucminas.alugacar.dto.PedidoDTOs.AvaliacaoRequest;
import br.pucminas.alugacar.dto.PedidoDTOs.PedidoRequest;
import br.pucminas.alugacar.dto.PedidoDTOs.PedidoResponse;
import br.pucminas.alugacar.exception.AcessoNegadoException;
import br.pucminas.alugacar.exception.RecursoNaoEncontradoException;
import br.pucminas.alugacar.exception.RegraNegocioException;
import br.pucminas.alugacar.model.Agente;
import br.pucminas.alugacar.model.Automovel;
import br.pucminas.alugacar.model.Cliente;
import br.pucminas.alugacar.model.Contrato;
import br.pucminas.alugacar.model.PedidoAluguel;
import br.pucminas.alugacar.model.enums.StatusPedido;
import br.pucminas.alugacar.repository.AgenteRepository;
import br.pucminas.alugacar.repository.ClienteRepository;
import br.pucminas.alugacar.repository.ContratoRepository;
import br.pucminas.alugacar.repository.PedidoAluguelRepository;
import br.pucminas.alugacar.security.UsuarioLogado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Gestao de pedidos de aluguel.
 * Clientes: introduzir, modificar, consultar e cancelar.
 * Agentes: consultar, modificar e avaliar (parecer financeiro).
 */
@Service
public class PedidoService {

    private final PedidoAluguelRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final AgenteRepository agenteRepository;
    private final ContratoRepository contratoRepository;
    private final AutomovelService automovelService;

    public PedidoService(PedidoAluguelRepository pedidoRepository, ClienteRepository clienteRepository,
                         AgenteRepository agenteRepository, ContratoRepository contratoRepository,
                         AutomovelService automovelService) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.agenteRepository = agenteRepository;
        this.contratoRepository = contratoRepository;
        this.automovelService = automovelService;
    }

    @Transactional
    public PedidoResponse criar(PedidoRequest req, UsuarioLogado logado) {
        Cliente cliente = clienteRepository.findById(logado.id())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", logado.id()));
        if (req.dataInicio().isBefore(LocalDate.now())) {
            throw new RegraNegocioException("A data de inicio nao pode estar no passado");
        }
        Automovel automovel = automovelDisponivel(req);
        PedidoAluguel pedido = new PedidoAluguel(cliente, automovel, req.dataInicio(), req.dataFim(),
                Normalizador.texto(req.observacoes()), req.querCredito());
        return paraResposta(pedidoRepository.save(pedido));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listar(UsuarioLogado logado) {
        List<PedidoAluguel> pedidos = logado.isAgente()
                ? pedidoRepository.findAllByOrderByDataCriacaoDesc()
                : pedidoRepository.findByClienteIdOrderByDataCriacaoDesc(logado.id());
        return pedidos.stream().map(this::paraResposta).toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscar(Long id, UsuarioLogado logado) {
        return paraResposta(obterComAcesso(id, logado));
    }

    @Transactional
    public PedidoResponse modificar(Long id, PedidoRequest req, UsuarioLogado logado) {
        PedidoAluguel pedido = obterComAcesso(id, logado);
        Automovel automovel = automovelDisponivel(req);
        String obs = Normalizador.texto(req.observacoes());
        if (logado.isAgente()) {
            pedido.modificarPeloAgente(automovel, req.dataInicio(), req.dataFim(), obs, req.querCredito());
        } else {
            pedido.modificarPeloCliente(automovel, req.dataInicio(), req.dataFim(), obs, req.querCredito());
        }
        return paraResposta(pedido);
    }

    @Transactional
    public PedidoResponse cancelar(Long id, UsuarioLogado logado) {
        PedidoAluguel pedido = obterComAcesso(id, logado);
        pedido.cancelar();
        return paraResposta(pedido);
    }

    @Transactional
    public PedidoResponse avaliar(Long id, AvaliacaoRequest req, UsuarioLogado logado) {
        PedidoAluguel pedido = obterComAcesso(id, logado);
        Agente agente = agenteRepository.findById(logado.id())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agente", logado.id()));
        pedido.avaliar(agente, req.aprovado(), req.parecer().trim());
        return paraResposta(pedido);
    }

    PedidoAluguel obterComAcesso(Long id, UsuarioLogado logado) {
        PedidoAluguel pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", id));
        if (!logado.isAgente() && !pedido.pertenceA(logado.id())) {
            throw new AcessoNegadoException("Este pedido pertence a outro cliente");
        }
        return pedido;
    }

    private Automovel automovelDisponivel(PedidoRequest req) {
        Automovel automovel = automovelService.obter(req.automovelId());
        if (req.dataInicio() != null && req.dataFim() != null && pedidoRepository.existeConflito(
                automovel.getId(), req.dataInicio(), req.dataFim(), StatusPedido.CONTRATADO)) {
            throw new RegraNegocioException("Automovel ja possui contrato no periodo informado");
        }
        return automovel;
    }

    private PedidoResponse paraResposta(PedidoAluguel p) {
        Long contratoId = p.getStatus() == StatusPedido.CONTRATADO
                ? contratoRepository.findByPedidoId(p.getId()).map(Contrato::getId).orElse(null)
                : null;
        return PedidoResponse.de(p, contratoId);
    }
}
