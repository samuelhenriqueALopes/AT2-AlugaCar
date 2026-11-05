package br.pucminas.alugacar.repository;

import br.pucminas.alugacar.model.Agente;
import br.pucminas.alugacar.model.enums.TipoAgente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgenteRepository extends JpaRepository<Agente, Long> {
    boolean existsByCnpj(String cnpj);

    List<Agente> findAllByOrderByNomeAsc();

    List<Agente> findByTipoOrderByNomeAsc(TipoAgente tipo);
}
