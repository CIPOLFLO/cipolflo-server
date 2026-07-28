--liquibase formatted sql

--changeset cipolflo:DEV-166-create-destinatario-notificacion-email
CREATE TABLE public.destinatario_notificacion_email (
                                                          id bigserial PRIMARY KEY,
                                                          email varchar(255) NOT NULL,
                                                          alias varchar(255) NOT NULL,
                                                          activo boolean NOT NULL DEFAULT true,
                                                          created_at timestamp with time zone,
                                                          updated_at timestamp with time zone,
                                                          created_by varchar(255),
                                                          updated_by varchar(255)
);
--rollback DROP TABLE IF EXISTS public.destinatario_notificacion_email;

--changeset cipolflo:DEV-166-index-destinatario-notificacion-email-email
CREATE UNIQUE INDEX idx_destinatario_notificacion_email_email ON public.destinatario_notificacion_email (email);
--rollback DROP INDEX IF EXISTS public.idx_destinatario_notificacion_email_email;
