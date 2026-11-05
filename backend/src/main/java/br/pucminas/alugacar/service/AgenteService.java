package br.pucminas.alugacar.service;

import br.pucminas.alugacar.dto.AgenteDTOs.AgenteRequest;
import br.pucminas.alugacar.dto.AgenteDTOs.AgenteResponse;
import br.pucminas.alugacar.exception.RegraNegocioException;
import br.pucminas.alugacar.model.Agente;
import br.pucminas.alugacar.model.enums.TipoAgente;
import br.pucminas.alugacar.repository.AgenteRepository;
import br.pucminas.alugacar.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Cadastro e consulta de agentes (empresas e bancos). */
@Service
public class AgenteService {

    private final AgenteRepository agenteRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AgenteService(AgenteRepository agenteRepository, UsuarioRepository usuarioRepository,
                         PasswordEncoder passwordEncoder) {
        this.agenteRepository = agenteRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AgenteResponse criar(AgenteRequest req) {
        String login = req.login().trim();
        String cnpj = Normalizador.apenasDigitos(req.cnpj());
        if (usuarioRepository.existsByLogin(login)) throw new RegraNegocioException("Usuario ja cadastrado");
        if (agenteRepository.existsByCnpj(cnpj)) throw new RegraNegocioException("CNPJ ja cadastrado");
        Agente agente = new Agente(req.nome().trim(), login, passwordEncoder.encode(req.senha()),
                Normalizador.texto(req.email()), cnpj, req.tipo());
        return AgenteResponse.de(agenteRepository.save(agente));
    }

    @Transactional(readOnly = true)
    public List<AgenteResponse> listar(TipoAgente tipo) {
        var agentes = tipo == null ? agenteRepository.findAllByOrderByNomeAsc()
                : agenteRepository.findByTipoOrderByNomeAsc(tipo);
        return agentes.stream().map(AgenteResponse::de).toList();
    }
}
