--liquibase formatted sql

--changeset cipolflo:DEV-177-create-tarifa-servicio
CREATE TABLE public.tarifa_servicio
(
    id                  BIGSERIAL PRIMARY KEY,
    servicio_id         BIGINT NOT NULL,
    tipo_cliente        CHARACTER VARYING(50) NOT NULL,
    precio              NUMERIC(12, 2) NOT NULL,
    modalidad_precio    CHARACTER VARYING(50) NOT NULL,
    antiguedad_minima   INTEGER,
    antiguedad_maxima   INTEGER,
    created_at          TIMESTAMP WITH TIME ZONE,
    updated_at          TIMESTAMP WITH TIME ZONE,
    created_by          CHARACTER VARYING(255),
    updated_by          CHARACTER VARYING(255),

    CONSTRAINT fk_tarifa_servicio_servicio
        FOREIGN KEY (servicio_id)
            REFERENCES public.servicio (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_tarifa_servicio_precio
        CHECK (precio > 0),

    CONSTRAINT chk_tarifa_servicio_antiguedad_minima
        CHECK (antiguedad_minima IS NULL OR antiguedad_minima >= 0),

    CONSTRAINT chk_tarifa_servicio_antiguedad_maxima
        CHECK (antiguedad_maxima IS NULL OR antiguedad_maxima >= 0),

    CONSTRAINT chk_tarifa_servicio_rango_antiguedad
        CHECK (
            antiguedad_minima IS NULL
                OR antiguedad_maxima IS NULL
                OR antiguedad_minima <= antiguedad_maxima
            )
);

CREATE INDEX idx_tarifa_servicio_servicio_id
    ON public.tarifa_servicio (servicio_id);

--rollback DROP TABLE IF EXISTS public.tarifa_servicio;