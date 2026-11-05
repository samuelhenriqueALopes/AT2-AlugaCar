package br.pucminas.alugacar.service;

import br.pucminas.alugacar.dto.AutomovelDTOs.AutomovelRequest;
import br.pucminas.alugacar.dto.AutomovelDTOs.AutomovelResponse;
import br.pucminas.alugacar.exception.RecursoNaoEncontradoException;
import br.pucminas.alugacar.exception.RegraNegocioException;
import br.pucminas.alugacar.model.Automovel;
import br.pucminas.alugacar.repository.AutomovelRepository;
import br.pucminas.alugacar.repository.PedidoAluguelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Gestao de automoveis (restrita a agentes, consulta liberada a todos os autenticados). */
@Service
public class AutomovelService {

    private final AutomovelRepository automovelRepository;
    private final PedidoAluguelRepository pedidoRepository;

    public AutomovelService(AutomovelRepository automovelRepository, PedidoAluguelRepository pedidoRepository) {
        this.automovelRepository = automovelRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional(readOnly = true)
    public List<AutomovelResponse> listar() {
        return automovelRepository.findAllByOrderByMarcaAscModeloAsc().stream().map(AutomovelResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public AutomovelResponse buscar(Long id) {
        return AutomovelResponse.de(obter(id));
    }

    @Transactional
    public AutomovelResponse criar(AutomovelRequest req) {
        String placa = Normalizador.placa(req.placa());
        String matricula = req.matricula().trim();
        if (automovelRepository.existsByPlaca(placa)) throw new RegraNegocioException("Placa ja cadastrada");
        if (automovelRepository.existsByMatricula(matricula)) throw new RegraNegocioException("Matricula ja cadastrada");
        Automovel a = new Automovel(matricula, req.ano(), req.marca().trim(), req.modelo().trim(), placa,
                Normalizador.texto(req.imagem()));
        return AutomovelResponse.de(automovelRepository.save(a));
    }

    @Transactional
    public AutomovelResponse atualizar(Long id, AutomovelRequest req) {
        Automovel a = obter(id);
        String placa = Normalizador.placa(req.placa());
        String matricula = req.matricula().trim();
        if (automovelRepository.existsByPlacaAndIdNot(placa, id)) throw new RegraNegocioException("Placa ja cadastrada");
        if (automovelRepository.existsByMatriculaAndIdNot(matricula, id)) {
            throw new RegraNegocioException("Matricula ja cadastrada");
        }
        a.setMatricula(matricula);
        a.setAno(req.ano());
        a.setMarca(req.marca().trim());
        a.setModelo(req.modelo().trim());
        a.setPlaca(placa);
        a.setImagem(Normalizador.texto(req.imagem()));
        return AutomovelResponse.de(a);
    }

    @Transactional
    public void excluir(Long id) {
        Automovel a = obter(id);
        if (pedidoRepository.existsByAutomovelId(id)) {
            throw new RegraNegocioException("Automovel possui pedidos de aluguel e nao pode ser excluido");
        }
        automovelRepository.delete(a);
    }

    Automovel obter(Long id) {
        return automovelRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Automovel", id));
    }
}
