--liquibase formatted sql

--changeset cipolflo:DEV-178-backfill-tarifa-servicio

INSERT INTO tarifa_servicio (
    servicio_id,
    tipo_cliente,
    precio,
    modalidad_precio,
    antiguedad_minima,
    antiguedad_maxima
)
SELECT
    s.id,
    'PARTICULAR',
    s.precio_particular,
    s.modalidad_precio,
    NULL,
    NULL
FROM servicio s
WHERE NOT EXISTS (
    SELECT 1
    FROM tarifa_servicio ts
    WHERE ts.servicio_id = s.id
);

INSERT INTO tarifa_servicio (
    servicio_id,
    tipo_cliente,
    precio,
    modalidad_precio,
    antiguedad_minima,
    antiguedad_maxima
)
SELECT
    s.id,
    'SOCIO_COMUN',
    s.precio_socio,
    s.modalidad_precio,
    NULL,
    NULL
FROM servicio s
WHERE NOT EXISTS (
    SELECT 1
    FROM tarifa_servicio ts
    WHERE ts.servicio_id = s.id
);

--rollback DELETE FROM tarifa_servicio
WHERE tipo_cliente IN ('PARTICULAR','SOCIO_COMUN');