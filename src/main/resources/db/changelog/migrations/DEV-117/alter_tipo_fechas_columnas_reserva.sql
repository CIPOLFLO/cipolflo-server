--liquibase formatted sql

--changeset cipolflo:DEV-117-alter-tipo-fechas-columnas-reserva
-- Las reservas se manejan por día (hora local de Uruguay), no por timestamp.
-- Se convierte el timestamp guardado al día correspondiente en hora de Uruguay.
ALTER TABLE public.reserva
    ALTER COLUMN fecha_entrada TYPE date
        USING (fecha_entrada AT TIME ZONE 'America/Montevideo')::date,
    ALTER COLUMN fecha_salida TYPE date
        USING (fecha_salida AT TIME ZONE 'America/Montevideo')::date;

--rollback ALTER TABLE public.reserva ALTER COLUMN fecha_entrada TYPE timestamp with time zone USING fecha_entrada::timestamp with time zone, ALTER COLUMN fecha_salida TYPE timestamp with time zone USING fecha_salida::timestamp with time zone;
