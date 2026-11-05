package br.pucminas.alugacar.repository;

import br.pucminas.alugacar.model.PedidoAluguel;
import br.pucminas.alugacar.model.enums.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PedidoAluguelRepository extends JpaRepository<PedidoAluguel, Long> {

    List<PedidoAluguel> findByClienteIdOrderByDataCriacaoDesc(Long clienteId);

    List<PedidoAluguel> findAllByOrderByDataCriacaoDesc();

    List<PedidoAluguel> findByClienteId(Long clienteId);

    boolean existsByAutomovelId(Long automovelId);

    /** Verifica se o automovel ja esta contratado em um periodo que se sobrepoe ao informado. */
    @Query("""
            select count(p) > 0 from PedidoAluguel p
            where p.automovel.id = :automovelId
              and p.status = :status
              and p.dataInicio <= :fim and p.dataFim >= :inicio
            """)
    boolean existeConflito(@Param("automovelId") Long automovelId,
                           @Param("inicio") LocalDate inicio,
                           @Param("fim") LocalDate fim,
                           @Param("status") StatusPedido status);
}
