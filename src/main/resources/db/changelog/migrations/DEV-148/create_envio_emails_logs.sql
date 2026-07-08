--liquibase formatted sql

--changeset cipolflo:DEV-148-create-envio-emails-logs
CREATE TABLE public.envio_emails_logs (
                                          id bigserial PRIMARY KEY,
                                          destinatario varchar(255) NOT NULL,
                                          asunto varchar(255) NOT NULL,
                                          tipo_evento varchar(50) NOT NULL,
                                          referencia_id bigint,
                                          estado varchar(20) NOT NULL,
                                          error text,
                                          created_at timestamp with time zone,
                                          updated_at timestamp with time zone,
                                          created_by varchar(255),
                                          updated_by varchar(255)
);
--rollback DROP TABLE IF EXISTS public.envio_emails_logs;

--changeset cipolflo:DEV-148-index-envio-emails-logs-created-at
CREATE INDEX idx_envio_emails_logs_created_at ON public.envio_emails_logs (created_at);
--rollback DROP INDEX IF EXISTS public.idx_envio_emails_logs_created_at;
