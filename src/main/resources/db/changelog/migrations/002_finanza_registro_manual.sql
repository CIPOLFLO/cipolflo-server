--liquibase formatted sql

--changeset cipolflo:002-finanza-registro-manual
ALTER TABLE public.finanza
    ADD COLUMN pago_cuota_id bigint;

CREATE INDEX idx_finanza_pago_cuota
    ON public.finanza USING btree (pago_cuota_id);

ALTER TABLE public.finanza
    ALTER COLUMN procedencia SET NOT NULL;

--rollback DROP INDEX IF EXISTS public.idx_finanza_pago_cuota;
--rollback ALTER TABLE public.finanza ALTER COLUMN procedencia DROP NOT NULL;
--rollback ALTER TABLE public.finanza DROP COLUMN IF EXISTS pago_cuota_id;