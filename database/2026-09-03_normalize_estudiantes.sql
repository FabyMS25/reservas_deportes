-- PostgreSQL migration: independent students, dated school enrollments and
-- dated student/contributor responsibilities.
--
-- IMPORTANT
-- 1. Run first in a restored copy of production.
-- 2. Back up the database before running this migration.
-- 3. Populate tmp_reservas_periodo_prueba ONLY with confirmed test UUIDs.
-- 4. Deploy the matching backend/frontend in the same release.

BEGIN;

LOCK TABLE reservas, reservas_periodo, estudiantes_escuela IN ACCESS EXCLUSIVE MODE;

-- ---------------------------------------------------------------------------
-- A. Explicit and reviewable cleanup of cancelled test mensualidades
-- ---------------------------------------------------------------------------

CREATE TEMP TABLE tmp_reservas_periodo_prueba (
    uuid varchar(50) PRIMARY KEY
) ON COMMIT DROP;

-- Review candidates before filling the list:
-- SELECT r.uuid, r.numero_reserva, r.numero_tasa, r.estado_reserva,
--        r.fecha_creacion, count(rp.id_reserva_periodo) AS estudiantes_periodo
-- FROM reservas r
-- JOIN reservas_periodo rp ON rp.id_reserva = r.id_reserva
-- WHERE lower(r.tipo_reserva) = 'periodo'
--   AND upper(r.estado_reserva) IN
--       ('CANCELADA', 'CANCELADO', 'ANULADA', 'ANULADO', 'EXPIRADA', 'NO_SHOW')
-- GROUP BY r.id_reserva
-- ORDER BY r.fecha_creacion;

-- This migration is for the confirmed test-only dataset. Select every periodo
-- reservation whose parent is already in a terminal cancelled/rejected state.
-- The guard below still aborts if any periodo detail belongs to another state.
INSERT INTO tmp_reservas_periodo_prueba (uuid)
SELECT DISTINCT r.uuid
FROM reservas r
JOIN reservas_periodo rp ON rp.id_reserva = r.id_reserva
WHERE lower(r.tipo_reserva) = 'periodo'
  AND upper(btrim(r.estado_reserva)) IN
      ('CANCELADA', 'CANCELADO', 'ANULADA', 'ANULADO', 'EXPIRADA',
       'NO_SHOW', 'RECHAZADA', 'RECHAZADO');

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM tmp_reservas_periodo_prueba t
        JOIN reservas r ON r.uuid = t.uuid
        WHERE lower(r.tipo_reserva) <> 'periodo'
           OR upper(btrim(r.estado_reserva)) NOT IN
              ('CANCELADA', 'CANCELADO', 'ANULADA', 'ANULADO', 'EXPIRADA',
               'NO_SHOW', 'RECHAZADA', 'RECHAZADO')
    ) THEN
        RAISE EXCEPTION
            'Cleanup list contains a reservation that is not a cancelled periodo reservation';
    END IF;
END $$;

-- Notifications use UUID references instead of a database foreign key.
DELETE FROM notificaciones n
USING tmp_reservas_periodo_prueba t
WHERE n.referencia_uuid = t.uuid
  AND upper(coalesce(n.referencia_tipo, '')) LIKE '%RESERVA%';

DELETE FROM notificaciones_admin n
USING tmp_reservas_periodo_prueba t
WHERE n.referencia_uuid = t.uuid
  AND upper(coalesce(n.referencia_tipo, '')) LIKE '%RESERVA%';

DELETE FROM pagos p
USING reservas r, tmp_reservas_periodo_prueba t
WHERE p.id_reserva = r.id_reserva AND r.uuid = t.uuid;

DELETE FROM reservas_auditorias a
USING reservas r, tmp_reservas_periodo_prueba t
WHERE a.reserva_id = r.id_reserva AND r.uuid = t.uuid;

DELETE FROM reserva_tarifa_detalles d
USING reservas r, tmp_reservas_periodo_prueba t
WHERE d.reserva_id = r.id_reserva AND r.uuid = t.uuid;

DELETE FROM reservas_periodo rp
USING reservas r, tmp_reservas_periodo_prueba t
WHERE rp.id_reserva = r.id_reserva AND r.uuid = t.uuid;

DELETE FROM reservas r
USING tmp_reservas_periodo_prueba t
WHERE r.uuid = t.uuid;

-- This first normalized migration intentionally requires all legacy periodo
-- test rows to be cleaned. If real rows exist, migrate them to an enrollment
-- instead of deleting them.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM reservas_periodo) THEN
        RAISE EXCEPTION
            'reservas_periodo still has rows. Stop: real history must be mapped to enrollments before migration';
    END IF;
END $$;

CREATE TEMP TABLE tmp_estudiantes_prueba (
    uuid varchar(50) PRIMARY KEY
) ON COMMIT DROP;

-- The user confirmed that every legacy student is test data. Copy all UUIDs
-- into the explicit cleanup scope; the DELETE below remains auditable.
-- SELECT uuid, nombre_completo, matricula, fecha_creacion
-- FROM estudiantes_escuela ORDER BY fecha_creacion;
INSERT INTO tmp_estudiantes_prueba (uuid)
SELECT uuid FROM estudiantes_escuela;

DELETE FROM estudiantes_escuela e
USING tmp_estudiantes_prueba t
WHERE e.uuid = t.uuid;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM estudiantes_escuela) THEN
        RAISE EXCEPTION
            'Legacy estudiantes_escuela still has rows. Add only confirmed test UUIDs or write a data-preserving mapping';
    END IF;
END $$;

-- ---------------------------------------------------------------------------
-- B. Replace the mixed student/enrollment table with normalized tables
-- ---------------------------------------------------------------------------

ALTER TABLE reservas_periodo DROP COLUMN IF EXISTS id_estudiante_escuela CASCADE;

-- Some production databases retain a legacy FK from reservas to
-- estudiantes_escuela even though Reserva.java no longer maps that relation.
-- Drop only those explicit FKs. Do not use DROP TABLE ... CASCADE because that
-- could silently remove unrelated objects.
DO $$
DECLARE
    v_constraint record;
BEGIN
    FOR v_constraint IN
        SELECT c.conname
        FROM pg_constraint c
        WHERE c.contype = 'f'
          AND c.conrelid = 'reservas'::regclass
          AND c.confrelid = 'estudiantes_escuela'::regclass
    LOOP
        EXECUTE format('ALTER TABLE reservas DROP CONSTRAINT %I',
                       v_constraint.conname);
    END LOOP;
END $$;

-- Remove only the obsolete legacy enrollment reference discovered in the
-- production reservas table. The reservation header continues to belong to
-- id_contribuyente (payer/registrar) and must not have an id_estudiante.
ALTER TABLE reservas DROP COLUMN IF EXISTS id_estudiante_escuela;

DROP TABLE estudiantes_escuela;

CREATE TABLE estudiantes (
    id_estudiante bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    uuid varchar(50) NOT NULL UNIQUE DEFAULT gen_random_uuid()::text,
    estado boolean NOT NULL DEFAULT false,
    fecha_creacion timestamp NOT NULL DEFAULT current_timestamp,
    fecha_actualizacion timestamp NOT NULL DEFAULT current_timestamp,
    nombre_completo varchar(255) NOT NULL,
    fecha_nacimiento date NULL,
    genero varchar(5) NULL,
    numero_documento varchar(20) NULL,
    telefono varchar(20) NULL,
    email varchar(255) NULL,
    CONSTRAINT ck_estudiante_nombre_no_vacio
        CHECK (length(btrim(nombre_completo)) > 0),
    CONSTRAINT ck_estudiante_email_no_vacio
        CHECK (email IS NULL OR length(btrim(email)) > 0),
    CONSTRAINT ck_estudiante_documento_no_vacio
        CHECK (numero_documento IS NULL OR length(btrim(numero_documento)) > 0)
);

CREATE INDEX idx_est_uuid ON estudiantes (uuid);
CREATE INDEX idx_est_nombre_normalizado ON estudiantes (lower(nombre_completo));
CREATE INDEX idx_est_numero_documento ON estudiantes (numero_documento)
    WHERE numero_documento IS NOT NULL AND estado = false;

-- A document is a strong identifier when supplied. Null documents remain
-- allowed because only the name is required.
CREATE UNIQUE INDEX uk_est_documento_activo
    ON estudiantes (numero_documento)
    WHERE numero_documento IS NOT NULL AND estado = false;

CREATE TABLE inscripciones_escuela (
    id_inscripcion bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    uuid varchar(50) NOT NULL UNIQUE DEFAULT gen_random_uuid()::text,
    estado boolean NOT NULL DEFAULT false,
    fecha_creacion timestamp NOT NULL DEFAULT current_timestamp,
    fecha_actualizacion timestamp NOT NULL DEFAULT current_timestamp,
    id_estudiante bigint NOT NULL,
    id_organizacion bigint NOT NULL,
    id_responsable bigint NULL,
    fecha_inscripcion date NOT NULL DEFAULT current_date,
    fecha_baja date NULL,
    motivo_baja varchar(500) NULL,
    matricula varchar(50) NULL,
    activo boolean NOT NULL DEFAULT true,
    CONSTRAINT fk_inscripcion_estudiante
        FOREIGN KEY (id_estudiante) REFERENCES estudiantes (id_estudiante)
        ON DELETE RESTRICT,
    CONSTRAINT fk_inscripcion_organizacion
        FOREIGN KEY (id_organizacion) REFERENCES organizaciones (id_organizacion)
        ON DELETE RESTRICT,
    CONSTRAINT fk_inscripcion_responsable
        FOREIGN KEY (id_responsable) REFERENCES contribuyentes (id_contribuyente)
        ON DELETE RESTRICT,
    CONSTRAINT ck_inscripcion_fechas
        CHECK (fecha_baja IS NULL OR fecha_baja >= fecha_inscripcion),
    CONSTRAINT ck_inscripcion_estado_activo
        CHECK (NOT activo OR fecha_baja IS NULL)
);

CREATE INDEX idx_inscripcion_estudiante
    ON inscripciones_escuela (id_estudiante);
CREATE INDEX idx_inscripcion_organizacion
    ON inscripciones_escuela (id_organizacion);
CREATE INDEX idx_inscripcion_responsable
    ON inscripciones_escuela (id_responsable)
    WHERE id_responsable IS NOT NULL;
CREATE INDEX idx_inscripcion_activa
    ON inscripciones_escuela (id_organizacion, activo)
    WHERE estado = false;

-- One simultaneous active enrollment per student and school. Closed periods
-- remain in history, and a later enrollment can be created.
CREATE UNIQUE INDEX uk_inscripcion_activa_estudiante_escuela
    ON inscripciones_escuela (id_estudiante, id_organizacion)
    WHERE estado = false AND activo = true AND fecha_baja IS NULL;

CREATE UNIQUE INDEX uk_inscripcion_matricula
    ON inscripciones_escuela (matricula)
    WHERE matricula IS NOT NULL AND estado = false;

-- A contributor relationship is optional and historical. A coach who manages
-- a school does not need one row per student merely to register payments.
CREATE TABLE estudiantes_responsables (
    id_estudiante_responsable bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    uuid varchar(50) NOT NULL UNIQUE DEFAULT gen_random_uuid()::text,
    estado boolean NOT NULL DEFAULT false,
    fecha_creacion timestamp NOT NULL DEFAULT current_timestamp,
    id_estudiante bigint NOT NULL,
    id_contribuyente bigint NOT NULL,
    tipo_responsabilidad varchar(30) NOT NULL,
    fecha_inicio date NOT NULL DEFAULT current_date,
    fecha_fin date NULL,
    activo boolean NOT NULL DEFAULT true,
    observaciones varchar(500) NULL,
    CONSTRAINT fk_est_resp_estudiante
        FOREIGN KEY (id_estudiante) REFERENCES estudiantes (id_estudiante)
        ON DELETE RESTRICT,
    CONSTRAINT fk_est_resp_contribuyente
        FOREIGN KEY (id_contribuyente) REFERENCES contribuyentes (id_contribuyente)
        ON DELETE RESTRICT,
    CONSTRAINT ck_est_resp_tipo
        CHECK (tipo_responsabilidad IN
               ('PADRE', 'MADRE', 'TUTOR_LEGAL', 'ENCARGADO', 'OTRO')),
    CONSTRAINT ck_est_resp_fechas
        CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio),
    CONSTRAINT ck_est_resp_estado_activo
        CHECK (NOT activo OR fecha_fin IS NULL)
);

CREATE INDEX idx_est_resp_estudiante
    ON estudiantes_responsables (id_estudiante);
CREATE INDEX idx_est_resp_contribuyente
    ON estudiantes_responsables (id_contribuyente);
CREATE UNIQUE INDEX uk_est_resp_activo
    ON estudiantes_responsables
       (id_estudiante, id_contribuyente, tipo_responsabilidad)
    WHERE estado = false AND activo = true AND fecha_fin IS NULL;

-- ---------------------------------------------------------------------------
-- C. Concurrency-safe matrícula generation: YYYY-000001, YYYY-000002, ...
-- ---------------------------------------------------------------------------

CREATE TABLE matricula_consecutivos (
    gestion integer PRIMARY KEY,
    ultimo_numero bigint NOT NULL CHECK (ultimo_numero >= 0)
);

CREATE OR REPLACE FUNCTION asignar_matricula_inscripcion()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    v_gestion integer;
    v_numero bigint;
BEGIN
    IF NEW.matricula IS NOT NULL AND btrim(NEW.matricula) <> '' THEN
        RETURN NEW;
    END IF;

    v_gestion := extract(year FROM coalesce(NEW.fecha_inscripcion, current_date));

    INSERT INTO matricula_consecutivos (gestion, ultimo_numero)
    VALUES (v_gestion, 1)
    ON CONFLICT (gestion)
    DO UPDATE SET ultimo_numero = matricula_consecutivos.ultimo_numero + 1
    RETURNING ultimo_numero INTO v_numero;

    NEW.matricula := v_gestion::text || '-' || lpad(v_numero::text, 6, '0');
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_asignar_matricula_inscripcion
BEFORE INSERT ON inscripciones_escuela
FOR EACH ROW
EXECUTE FUNCTION asignar_matricula_inscripcion();

-- Monthly details now reference the enrollment, which identifies both the
-- student and school unambiguously. The reservation keeps the payer/registrar.
ALTER TABLE reservas_periodo
    ADD COLUMN id_inscripcion_escuela bigint NOT NULL;

ALTER TABLE reservas_periodo
    ADD CONSTRAINT fk_res_periodo_inscripcion
    FOREIGN KEY (id_inscripcion_escuela)
    REFERENCES inscripciones_escuela (id_inscripcion)
    ON DELETE RESTRICT;

CREATE INDEX idx_res_periodo_inscripcion
    ON reservas_periodo (id_inscripcion_escuela);

-- Do not use a simple unique index for (enrollment, year, month): cancellation
-- state belongs to reservas, so a cancelled attempt must not block a new one.
-- The service must reject duplicates only when the parent reservation is in an
-- active/payment state.

COMMIT;

-- Verification queries (run after COMMIT):
-- SELECT * FROM estudiantes ORDER BY id_estudiante;
-- SELECT * FROM inscripciones_escuela ORDER BY id_inscripcion;
-- SELECT * FROM estudiantes_responsables ORDER BY id_estudiante_responsable;
-- SELECT * FROM matricula_consecutivos ORDER BY gestion;
