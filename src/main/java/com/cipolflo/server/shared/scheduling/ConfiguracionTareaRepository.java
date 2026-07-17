package com.cipolflo.server.shared.scheduling;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfiguracionTareaRepository extends JpaRepository<ConfiguracionTarea, ClaveConfiguracionTarea> {
}
