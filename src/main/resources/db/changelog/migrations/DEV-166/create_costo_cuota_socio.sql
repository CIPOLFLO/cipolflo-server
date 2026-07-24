--liquibase formatted sql

--changeset cipolflo:DEV-166-create-costo-cuota-socio
CREATE TABLE public.costo_cuota_socio (
    id bigint PRIMARY KEY,
    monto numeric(12,2) NOT NULL,
    created_at timestamp with time zone,
    updated_at timestamp with time zone,
    created_by character varying(255),
    updated_by character varying(255)
);
--rollback DROP TABLE IF EXISTS public.costo_cuota_socio;

--changeset cipolflo:DEV-166-seed-costo-cuota-socio
INSERT INTO public.costo_cuota_socio (id, monto) VALUES (1, 200.00);
--rollback DELETE FROM public.costo_cuota_socio WHERE id = 1;
