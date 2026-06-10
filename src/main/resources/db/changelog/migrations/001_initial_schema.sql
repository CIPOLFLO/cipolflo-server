--liquibase formatted sql

--changeset cipolflo:001-create-tables
CREATE TABLE public.cliente (
    id bigserial NOT NULL,
    tipo character varying(31) NOT NULL,
    cedula character varying(20) NOT NULL,
    nombre_completo character varying(150) NOT NULL,
    telefono character varying(50) NOT NULL,
    mail character varying(100),
    notas text,
    numero_socio integer,
    fecha_nacimiento date,
    estado character varying(50),
    pais character varying(100),
    departamento character varying(100),
    ciudad character varying(100),
    direccion character varying(150),
    fecha_ingreso date,
    fecha_ultimo_pago date,
    metodo_cobro character varying(50),
    meses_sin_pagar integer,
    created_at timestamp with time zone,
    updated_at timestamp with time zone,
    created_by character varying(255),
    updated_by character varying(255)
);

CREATE TABLE public.pago_cuota (
    id bigserial NOT NULL,
    socio_id bigint NOT NULL,
    fecha timestamp with time zone NOT NULL,
    importe numeric(19,2) NOT NULL,
    forma_pago character varying(50) NOT NULL,
    cantidad_meses integer NOT NULL,
    created_at timestamp with time zone,
    updated_at timestamp with time zone,
    created_by character varying(255),
    updated_by character varying(255)
);

CREATE TABLE public.servicio (
    id bigserial NOT NULL,
    nombre character varying(150) NOT NULL,
    procedencia character varying(50) NOT NULL,
    precio_particular numeric(19,2) NOT NULL,
    precio_socio numeric(19,2) NOT NULL,
    modalidad_precio character varying(50) NOT NULL,
    capacidad integer,
    cantidad integer,
    habilitado boolean NOT NULL DEFAULT true,
    created_at timestamp with time zone,
    updated_at timestamp with time zone,
    created_by character varying(255),
    updated_by character varying(255)
);

CREATE TABLE public.reserva (
    id bigserial NOT NULL,
    cliente_id bigint NOT NULL,
    servicio_id bigint NOT NULL,
    estado character varying(50) NOT NULL,
    procedencia character varying(50) NOT NULL,
    fecha_entrada timestamp with time zone NOT NULL,
    fecha_salida timestamp with time zone NOT NULL,
    importe numeric(19,2),
    cantidad_personas integer,
    cantidad_menores integer,
    pago boolean NOT NULL DEFAULT false,
    forma_pago character varying(50),
    documentacion boolean NOT NULL DEFAULT false,
    notas text,
    created_at timestamp with time zone,
    updated_at timestamp with time zone,
    created_by character varying(255),
    updated_by character varying(255)
);

CREATE TABLE public.finanza (
    id bigserial NOT NULL,
    tipo character varying(31) NOT NULL,
    fecha date NOT NULL,
    importe numeric(19,2) NOT NULL,
    concepto_de_pago character varying(150) NOT NULL,
    forma_de_pago character varying(50) NOT NULL,
    notas text,
    procedencia character varying(50),
    reserva_id bigint,
    created_at timestamp with time zone,
    updated_at timestamp with time zone,
    created_by character varying(255),
    updated_by character varying(255)
);

--rollback DROP TABLE public.finanza;
--rollback DROP TABLE public.reserva;
--rollback DROP TABLE public.servicio;
--rollback DROP TABLE public.pago_cuota;
--rollback DROP TABLE public.cliente;

--changeset cipolflo:001-create-constraints
ALTER TABLE ONLY public.cliente ADD CONSTRAINT cliente_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.cliente ADD CONSTRAINT cliente_cedula_key UNIQUE (cedula);
ALTER TABLE ONLY public.pago_cuota ADD CONSTRAINT pago_cuota_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.servicio ADD CONSTRAINT servicio_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.reserva ADD CONSTRAINT reserva_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.finanza ADD CONSTRAINT finanza_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.pago_cuota
    ADD CONSTRAINT pago_cuota_socio_id_fkey
    FOREIGN KEY (socio_id) REFERENCES public.cliente(id) ON DELETE RESTRICT;

ALTER TABLE ONLY public.reserva
    ADD CONSTRAINT reserva_cliente_id_fkey
    FOREIGN KEY (cliente_id) REFERENCES public.cliente(id) ON DELETE RESTRICT;

ALTER TABLE ONLY public.reserva
    ADD CONSTRAINT reserva_servicio_id_fkey
    FOREIGN KEY (servicio_id) REFERENCES public.servicio(id) ON DELETE RESTRICT;

--rollback ALTER TABLE ONLY public.reserva DROP CONSTRAINT reserva_servicio_id_fkey;
--rollback ALTER TABLE ONLY public.reserva DROP CONSTRAINT reserva_cliente_id_fkey;
--rollback ALTER TABLE ONLY public.pago_cuota DROP CONSTRAINT pago_cuota_socio_id_fkey;
--rollback ALTER TABLE ONLY public.finanza DROP CONSTRAINT finanza_pkey;
--rollback ALTER TABLE ONLY public.reserva DROP CONSTRAINT reserva_pkey;
--rollback ALTER TABLE ONLY public.servicio DROP CONSTRAINT servicio_pkey;
--rollback ALTER TABLE ONLY public.pago_cuota DROP CONSTRAINT pago_cuota_pkey;
--rollback ALTER TABLE ONLY public.cliente DROP CONSTRAINT cliente_cedula_key;
--rollback ALTER TABLE ONLY public.cliente DROP CONSTRAINT cliente_pkey;

--changeset cipolflo:001-create-indexes
CREATE INDEX idx_reserva_cliente ON public.reserva USING btree (cliente_id);
CREATE INDEX idx_reserva_servicio ON public.reserva USING btree (servicio_id);
CREATE INDEX idx_pago_cuota_socio ON public.pago_cuota USING btree (socio_id);
CREATE INDEX idx_finanza_tipo ON public.finanza USING btree (tipo);
CREATE INDEX idx_finanza_reserva ON public.finanza USING btree (reserva_id);
CREATE INDEX idx_cliente_tipo ON public.cliente USING btree (tipo);

--rollback DROP INDEX public.idx_cliente_tipo;
--rollback DROP INDEX public.idx_finanza_reserva;
--rollback DROP INDEX public.idx_finanza_tipo;
--rollback DROP INDEX public.idx_pago_cuota_socio;
--rollback DROP INDEX public.idx_reserva_servicio;
--rollback DROP INDEX public.idx_reserva_cliente;
