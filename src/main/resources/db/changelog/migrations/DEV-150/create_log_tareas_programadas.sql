--liquibase formatted sql

--changeset cipolflo:DEV-150-create-log-tareas-programadas
CREATE TABLE public.log_tareas_programadas (
                                               id bigserial PRIMARY KEY,
                                               tarea varchar(50) NOT NULL,
                                               estado varchar(20) NOT NULL,
                                               inicio timestamp with time zone NOT NULL,
                                               fin timestamp with time zone NOT NULL,
                                               duracion_ms bigint NOT NULL,
                                               resumen text,
                                               error text,
                                               created_at timestamp with time zone,
                                               updated_at timestamp with time zone,
                                               created_by varchar(255),
                                               updated_by varchar(255)
);
--rollback DROP TABLE IF EXISTS public.log_tareas_programadas;

--changeset cipolflo:DEV-150-index-log-tareas-programadas-tarea-inicio
CREATE INDEX idx_log_tareas_programadas_tarea_inicio ON public.log_tareas_programadas (tarea, inicio);
--rollback DROP INDEX IF EXISTS public.idx_log_tareas_programadas_tarea_inicio;
