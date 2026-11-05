package br.pucminas.alugacar.repository;

import br.pucminas.alugacar.model.Automovel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AutomovelRepository extends JpaRepository<Automovel, Long> {
    boolean existsByPlaca(String placa);

    boolean existsByMatricula(String matricula);

    boolean existsByPlacaAndIdNot(String placa, Long id);

    boolean existsByMatriculaAndIdNot(String matricula, Long id);

    List<Automovel> findAllByOrderByMarcaAscModeloAsc();
}
