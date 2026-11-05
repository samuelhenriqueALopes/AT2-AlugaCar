package br.pucminas.alugacar.repository;

import br.pucminas.alugacar.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByCpf(String cpf);

    boolean existsByRg(String rg);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    boolean existsByRgAndIdNot(String rg, Long id);

    List<Cliente> findAllByOrderByNomeAsc();
}
