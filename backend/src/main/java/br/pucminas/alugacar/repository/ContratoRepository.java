package br.pucminas.alugacar.repository;

import br.pucminas.alugacar.model.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    Optional<Contrato> findByPedidoId(Long pedidoId);

    List<Contrato> findByPedidoClienteIdOrderByDataAssinaturaDesc(Long clienteId);

    List<Contrato> findAllByOrderByDataAssinaturaDesc();

    boolean existsByPedidoClienteId(Long clienteId);
}
