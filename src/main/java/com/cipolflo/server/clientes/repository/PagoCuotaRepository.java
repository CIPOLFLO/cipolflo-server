package com.cipolflo.server.clientes.repository;

import com.cipolflo.server.clientes.domain.PagoCuota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PagoCuotaRepository extends JpaRepository<PagoCuota, Long> {

    boolean existsBySocioIdAndAnioAndMes(
            Long socioId,
            Integer anio,
            Integer mes
    );

    List<PagoCuota> findBySocioId(Long socioId);

    Optional<PagoCuota> findTopBySocioIdOrderByAnioDescMesDesc(Long socioId);
}

