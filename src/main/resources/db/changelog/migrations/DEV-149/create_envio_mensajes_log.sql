--liquibase formatted sql

--changeset cipolflo:DEV-149-create-envio-mensajes-log
CREATE TABLE public.envio_mensajes_log (
                                            id bigserial PRIMARY KEY,
                                            canal varchar(20) NOT NULL,
                                            destinatario_id varchar(64) NOT NULL,
                                            tipo_evento varchar(50) NOT NULL,
                                            estado varchar(20) NOT NULL,
                                            error text,
                                            created_at timestamp with time zone,
                                            updated_at timestamp with time zone,
                                            created_by varchar(255),
                                            updated_by varchar(255)
);
--rollback DROP TABLE IF EXISTS public.envio_mensajes_log;

--changeset cipolflo:DEV-149-index-envio-mensajes-log-created-at
CREATE INDEX idx_envio_mensajes_log_created_at ON public.envio_mensajes_log (created_at);
--rollback DROP INDEX IF EXISTS public.idx_envio_mensajes_log_created_at;
