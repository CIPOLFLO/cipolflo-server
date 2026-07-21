package com.cipolflo.server.clientes.repository;

import com.cipolflo.server.clientes.domain.PagoCuota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PagoCuotaRepository extends JpaRepository<PagoCuota, Long> {

    Optional<PagoCuota> findTopBySocioIdOrderByAnioDescMesDesc(Long socioId);

    List<PagoCuota> findBySocioIdAndIdIn(Long socioId, Collection<Long> ids);

}
