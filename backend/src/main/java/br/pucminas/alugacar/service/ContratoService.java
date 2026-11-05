package br.pucminas.alugacar.service;

import br.pucminas.alugacar.dto.ContratoDTOs.ContratoRequest;
import br.pucminas.alugacar.dto.ContratoDTOs.ContratoResponse;
import br.pucminas.alugacar.dto.ContratoDTOs.CreditoRequest;
import br.pucminas.alugacar.exception.AcessoNegadoException;
import br.pucminas.alugacar.exception.RecursoNaoEncontradoException;
import br.pucminas.alugacar.exception.RegraNegocioException;
import br.pucminas.alugacar.model.Agente;
import br.pucminas.alugacar.model.enums.StatusPedido;
import br.pucminas.alugacar.repository.PedidoAluguelRepository;
import br.pucminas.alugacar.model.Contrato;
import br.pucminas.alugacar.model.ContratoCredito;
import br.pucminas.alugacar.model.PedidoAluguel;
import br.pucminas.alugacar.repository.AgenteRepository;
import br.pucminas.alugacar.repository.ContratoRepository;
import br.pucminas.alugacar.security.UsuarioLogado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Execucao e consulta de contratos de aluguel (e de credito associado). */
@Service
public class ContratoService {

    private final ContratoRepository contratoRepository;
    private final AgenteRepository agenteRepository;
    private final PedidoAluguelRepository pedidoRepository;
    private final PedidoService pedidoService;

    public ContratoService(ContratoRepository contratoRepository, AgenteRepository agenteRepository,
                           PedidoAluguelRepository pedidoRepository, PedidoService pedidoService) {
        this.contratoRepository = contratoRepository;
        this.agenteRepository = agenteRepository;
        this.pedidoRepository = pedidoRepository;
        this.pedidoService = pedidoService;
    }

    /** Pedido com parecer positivo e colocado a consideracao do agente para execucao do contrato. */
    @Transactional
    public ContratoResponse executar(Long pedidoId, ContratoRequest req, UsuarioLogado logado) {
        PedidoAluguel pedido = pedidoService.obterComAcesso(pedidoId, logado);
        Agente agente = agenteRepository.findById(logado.id())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agente", logado.id()));
        if (pedidoRepository.existeConflito(pedido.getAutomovel().getId(), pedido.getDataInicio(),
                pedido.getDataFim(), StatusPedido.CONTRATADO)) {
            throw new RegraNegocioException("Automovel ja possui contrato no periodo deste pedido");
        }
        CreditoRequest c = req.credito();
        ContratoCredito credito = c == null ? null
                : new ContratoCredito(agente, c.valor(), c.taxaJurosMensal(), c.numeroParcelas());
        Contrato contrato = Contrato.executar(pedido, agente, req.tipoPropriedade(), credito);
        return ContratoResponse.de(contratoRepository.save(contrato));
    }

    @Transactional(readOnly = true)
    public List<ContratoResponse> listar(UsuarioLogado logado) {
        List<Contrato> contratos = logado.isAgente()
                ? contratoRepository.findAllByOrderByDataAssinaturaDesc()
                : contratoRepository.findByPedidoClienteIdOrderByDataAssinaturaDesc(logado.id());
        return contratos.stream().map(ContratoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ContratoResponse buscar(Long id, UsuarioLogado logado) {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Contrato", id));
        if (!logado.isAgente() && !contrato.getPedido().pertenceA(logado.id())) {
            throw new AcessoNegadoException("Este contrato pertence a outro cliente");
        }
        return ContratoResponse.de(contrato);
    }
}
