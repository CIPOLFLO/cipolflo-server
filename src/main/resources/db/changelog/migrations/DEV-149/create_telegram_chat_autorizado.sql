--liquibase formatted sql

--changeset cipolflo:DEV-149-create-telegram-chat-autorizado
CREATE TABLE public.telegram_chat_autorizado (
                                                  id bigserial PRIMARY KEY,
                                                  chat_id bigint NOT NULL,
                                                  alias varchar(255) NOT NULL,
                                                  activo boolean NOT NULL DEFAULT true,
                                                  recibe_notificaciones boolean NOT NULL DEFAULT true,
                                                  created_at timestamp with time zone,
                                                  updated_at timestamp with time zone,
                                                  created_by varchar(255),
                                                  updated_by varchar(255)
);
--rollback DROP TABLE IF EXISTS public.telegram_chat_autorizado;

--changeset cipolflo:DEV-149-index-telegram-chat-autorizado-chat-id
CREATE UNIQUE INDEX idx_telegram_chat_autorizado_chat_id ON public.telegram_chat_autorizado (chat_id);
--rollback DROP INDEX IF EXISTS public.idx_telegram_chat_autorizado_chat_id;
