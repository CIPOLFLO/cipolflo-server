--liquibase formatted sql

--changeset cipolflo:DEV-162-add-plazo-confirmacion-to-reserva
ALTER TABLE public.reserva
    ADD COLUMN plazo_confirmacion varchar(32) NULL,
    ADD COLUMN fecha_limite_confirmacion timestamp NULL;

CREATE INDEX idx_reserva_cancelacion_automatica
    ON public.reserva (estado, fecha_limite_confirmacion);

--rollback DROP INDEX IF EXISTS public.idx_reserva_cancelacion_automatica;
--rollback ALTER TABLE public.reserva DROP COLUMN fecha_limite_confirmacion, DROP COLUMN plazo_confirmacion;

--changeset cipolflo:DEV-162-drop-fecha-limite-pago-from-reserva
ALTER TABLE public.reserva DROP COLUMN fecha_limite_pago;

--rollback ALTER TABLE public.reserva ADD COLUMN fecha_limite_pago timestamp NULL;