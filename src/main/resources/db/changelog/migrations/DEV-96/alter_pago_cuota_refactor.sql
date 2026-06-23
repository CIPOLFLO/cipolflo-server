--liquibase formatted sql

--changeset cipolflo:DEV-96-refactor-pago-cuota

ALTER TABLE public.pago_cuota
    RENAME COLUMN fecha TO fecha_pago;

ALTER TABLE public.pago_cuota
    RENAME COLUMN forma_pago TO metodo_cobro;

ALTER TABLE public.pago_cuota
DROP COLUMN cantidad_meses;

ALTER TABLE public.pago_cuota
    ADD COLUMN anio integer;

ALTER TABLE public.pago_cuota
    ADD COLUMN mes integer;

ALTER TABLE public.pago_cuota
    ADD COLUMN observaciones text;

ALTER TABLE public.pago_cuota
    ADD CONSTRAINT uk_pago_cuota_socio_periodo
        UNIQUE (socio_id, anio, mes);

--rollback ALTER TABLE public.pago_cuota DROP CONSTRAINT uk_pago_cuota_socio_periodo;
--rollback ALTER TABLE public.pago_cuota DROP COLUMN observaciones;
--rollback ALTER TABLE public.pago_cuota DROP COLUMN mes;
--rollback ALTER TABLE public.pago_cuota DROP COLUMN anio;
--rollback ALTER TABLE public.pago_cuota ADD COLUMN cantidad_meses integer;
--rollback ALTER TABLE public.pago_cuota RENAME COLUMN metodo_cobro TO forma_pago;
--rollback ALTER TABLE public.pago_cuota RENAME COLUMN fecha_pago TO fecha;