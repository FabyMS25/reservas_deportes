/*
  GAMQ Deportes - Ajuste correcto para Escuelas Municipales
  Objetivo:
  1) Una organización/escuela puede tener uno o más representantes/entrenadores.
  2) Un estudiante pertenece a una escuela por medio de inscripciones_escuela.
  3) La mensualidad se cobra por inscripción + año + mes, sin duplicar el mismo mes.
  4) Las reservas recurrentes siguen siendo solo para ocupar/bloquear el campo deportivo.

  Ejecutar en pgAdmin sobre la base correcta, por ejemplo:
  jdbc:postgresql://localhost:5432/db_deprotes

  Este script NO crea cursos, horarios de escuela ni nuevas tablas de cursos.
*/

BEGIN;

/* =========================================================
   0. Verificaciones informativas antes de tocar estructura
   ========================================================= */

-- Debe retornar 0 filas antes de crear el índice único de mensualidades.
-- Si retorna filas, primero hay que resolver duplicados manualmente.
SELECT
    rp.id_inscripcion_escuela,
    rp."año" AS anio,
    rp.mes,
    COUNT(*) AS cantidad
FROM public.reservas_periodo rp
WHERE rp.estado = false
GROUP BY rp.id_inscripcion_escuela, rp."año", rp.mes
HAVING COUNT(*) > 1;

-- Debe retornar 0 filas. Si retorna filas, hay mensualidades asociadas a una reserva
-- de otra escuela/organización.
SELECT
    rp.id_reserva_periodo,
    rp.id_reserva,
    rp.id_inscripcion_escuela,
    r.id_organizacion AS organizacion_reserva,
    ie.id_organizacion AS organizacion_inscripcion
FROM public.reservas_periodo rp
JOIN public.reservas r ON r.id_reserva = rp.id_reserva
JOIN public.inscripciones_escuela ie ON ie.id_inscripcion = rp.id_inscripcion_escuela
WHERE r.id_organizacion IS DISTINCT FROM ie.id_organizacion;

/* =========================================================
   1. Representantes múltiples por organización
   =========================================================
   Mantener organizaciones.id_representante como representante PRINCIPAL
   por compatibilidad con el backend y pantallas existentes.
   La tabla organizaciones_representantes guarda todos los representantes.
*/

CREATE TABLE IF NOT EXISTS public.organizaciones_representantes (
    id_organizacion BIGINT NOT NULL,
    id_contribuyente BIGINT NOT NULL
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'pk_organizaciones_representantes'
          AND conrelid = 'public.organizaciones_representantes'::regclass
    ) THEN
        ALTER TABLE public.organizaciones_representantes
            ADD CONSTRAINT pk_organizaciones_representantes
            PRIMARY KEY (id_organizacion, id_contribuyente);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_org_representantes_organizacion'
          AND conrelid = 'public.organizaciones_representantes'::regclass
    ) THEN
        ALTER TABLE public.organizaciones_representantes
            ADD CONSTRAINT fk_org_representantes_organizacion
            FOREIGN KEY (id_organizacion)
            REFERENCES public.organizaciones(id_organizacion)
            ON DELETE RESTRICT;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_org_representantes_contribuyente'
          AND conrelid = 'public.organizaciones_representantes'::regclass
    ) THEN
        ALTER TABLE public.organizaciones_representantes
            ADD CONSTRAINT fk_org_representantes_contribuyente
            FOREIGN KEY (id_contribuyente)
            REFERENCES public.contribuyentes(id_contribuyente)
            ON DELETE RESTRICT;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_org_representantes_contribuyente
    ON public.organizaciones_representantes (id_contribuyente);

CREATE INDEX IF NOT EXISTS idx_org_representantes_organizacion
    ON public.organizaciones_representantes (id_organizacion);

-- Migra el representante principal actual a la nueva tabla de muchos representantes.
INSERT INTO public.organizaciones_representantes (id_organizacion, id_contribuyente)
SELECT o.id_organizacion, o.id_representante
FROM public.organizaciones o
WHERE o.id_representante IS NOT NULL
ON CONFLICT (id_organizacion, id_contribuyente) DO NOTHING;

/* =========================================================
   2. Inscripción estudiante-escuela
   =========================================================
   Esta tabla es el centro correcto para decir:
   "este estudiante pertenece a esta escuela".
   id_responsable es opcional y representa al tutor/pagador preferente.
*/

-- Estas restricciones ya pueden existir; se crean solo si faltan.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'ck_inscripcion_fechas'
          AND conrelid = 'public.inscripciones_escuela'::regclass
    ) THEN
        ALTER TABLE public.inscripciones_escuela
            ADD CONSTRAINT ck_inscripcion_fechas
            CHECK (fecha_baja IS NULL OR fecha_baja >= fecha_inscripcion);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'ck_inscripcion_estado_activo'
          AND conrelid = 'public.inscripciones_escuela'::regclass
    ) THEN
        ALTER TABLE public.inscripciones_escuela
            ADD CONSTRAINT ck_inscripcion_estado_activo
            CHECK ((activo = false) OR fecha_baja IS NULL);
    END IF;
END $$;

-- Evita dos inscripciones activas del mismo estudiante en la misma escuela.
CREATE UNIQUE INDEX IF NOT EXISTS uk_inscripcion_activa_estudiante_escuela
    ON public.inscripciones_escuela (id_estudiante, id_organizacion)
    WHERE estado = false AND activo = true AND fecha_baja IS NULL;

CREATE INDEX IF NOT EXISTS idx_inscripcion_organizacion
    ON public.inscripciones_escuela (id_organizacion);

CREATE INDEX IF NOT EXISTS idx_inscripcion_estudiante
    ON public.inscripciones_escuela (id_estudiante);

CREATE INDEX IF NOT EXISTS idx_inscripcion_responsable
    ON public.inscripciones_escuela (id_responsable)
    WHERE id_responsable IS NOT NULL;

/* =========================================================
   3. Mensualidades por estudiante + año + mes
   =========================================================
   reservas_periodo guarda el detalle de mensualidad.
   reservas.id_contribuyente guarda el contribuyente pagador de la tasa.
*/

-- Falla con un mensaje claro si existen datos que impedirían endurecer reglas.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM public.reservas_periodo
        WHERE estado = false
          AND (mes IS NULL OR "año" IS NULL)
    ) THEN
        RAISE EXCEPTION 'Existen mensualidades activas con mes o año nulo. Corrige esos datos antes de aplicar la restricción.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.reservas_periodo
        WHERE estado = false
          AND (mes < 1 OR mes > 12 OR "año" < 2000 OR "año" > 2100)
    ) THEN
        RAISE EXCEPTION 'Existen mensualidades activas con mes/año fuera de rango. Corrige esos datos antes de aplicar la restricción.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM (
            SELECT id_inscripcion_escuela, "año", mes
            FROM public.reservas_periodo
            WHERE estado = false
            GROUP BY id_inscripcion_escuela, "año", mes
            HAVING COUNT(*) > 1
        ) duplicados
    ) THEN
        RAISE EXCEPTION 'Existen mensualidades duplicadas para la misma inscripción/año/mes. Corrige duplicados antes de crear el índice único.';
    END IF;
END $$;

-- Protege rango de mes y año para nuevas mensualidades.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'ck_reservas_periodo_mes_anio'
          AND conrelid = 'public.reservas_periodo'::regclass
    ) THEN
        ALTER TABLE public.reservas_periodo
            ADD CONSTRAINT ck_reservas_periodo_mes_anio
            CHECK (mes BETWEEN 1 AND 12 AND "año" BETWEEN 2000 AND 2100);
    END IF;
END $$;

-- Evita cobrar dos veces el mismo mes activo al mismo estudiante dentro de la misma inscripción.
CREATE UNIQUE INDEX IF NOT EXISTS uk_reservas_periodo_inscripcion_anio_mes_activa
    ON public.reservas_periodo (id_inscripcion_escuela, "año", mes)
    WHERE estado = false;

CREATE INDEX IF NOT EXISTS idx_res_periodo_reserva
    ON public.reservas_periodo (id_reserva);

CREATE INDEX IF NOT EXISTS idx_res_periodo_inscripcion
    ON public.reservas_periodo (id_inscripcion_escuela);

/* =========================================================
   4. Validación de coherencia mensualidad vs escuela
   =========================================================
   Evita que una mensualidad de una escuela quede asociada por error
   a una inscripción de otra escuela.
*/

CREATE OR REPLACE FUNCTION public.validar_reserva_periodo_inscripcion()
RETURNS trigger AS $$
DECLARE
    v_tipo_reserva TEXT;
    v_org_reserva BIGINT;
    v_org_inscripcion BIGINT;
BEGIN
    SELECT r.tipo_reserva, r.id_organizacion
      INTO v_tipo_reserva, v_org_reserva
    FROM public.reservas r
    WHERE r.id_reserva = NEW.id_reserva;

    SELECT ie.id_organizacion
      INTO v_org_inscripcion
    FROM public.inscripciones_escuela ie
    WHERE ie.id_inscripcion = NEW.id_inscripcion_escuela;

    IF v_tipo_reserva IS NULL THEN
        RAISE EXCEPTION 'La reserva % no existe.', NEW.id_reserva;
    END IF;

    IF lower(v_tipo_reserva) <> 'periodo' THEN
        RAISE EXCEPTION 'Los detalles de mensualidad solo pueden pertenecer a reservas tipo periodo. Reserva: %, tipo: %', NEW.id_reserva, v_tipo_reserva;
    END IF;

    IF v_org_reserva IS NULL THEN
        RAISE EXCEPTION 'La reserva tipo periodo % debe tener organización/escuela.', NEW.id_reserva;
    END IF;

    IF v_org_inscripcion IS NULL THEN
        RAISE EXCEPTION 'La inscripción % no existe.', NEW.id_inscripcion_escuela;
    END IF;

    IF v_org_reserva <> v_org_inscripcion THEN
        RAISE EXCEPTION 'La mensualidad no coincide: organización de reserva % vs organización de inscripción %.', v_org_reserva, v_org_inscripcion;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_validar_reserva_periodo_inscripcion
    ON public.reservas_periodo;

CREATE TRIGGER trg_validar_reserva_periodo_inscripcion
BEFORE INSERT OR UPDATE OF id_reserva, id_inscripcion_escuela
ON public.reservas_periodo
FOR EACH ROW
EXECUTE FUNCTION public.validar_reserva_periodo_inscripcion();

COMMIT;

/* =========================================================
   5. OPCIONAL: sub-áreas de campos deportivos
   =========================================================
   Úsalo solo si quieres que el estadio tenga un campo padre
   "CAMPO DE PISTA ATLÉTICA" y áreas hijas "Área de Atletismo 1..6".
   No afecta reservas actuales; el backend actual ignora estas columnas.

   Si quieres usarlo, ejecuta este bloque aparte después del COMMIT anterior.
   Ajusta los nombres si en producción son diferentes.
*/

/*
BEGIN;

ALTER TABLE public.campos_deportivos
    ADD COLUMN IF NOT EXISTS id_campo_padre BIGINT NULL;

ALTER TABLE public.campos_deportivos
    ADD COLUMN IF NOT EXISTS tipo_espacio VARCHAR(30) NOT NULL DEFAULT 'CAMPO';

ALTER TABLE public.campos_deportivos
    ADD COLUMN IF NOT EXISTS permite_reserva_independiente BOOLEAN NOT NULL DEFAULT true;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_campos_deportivos_campo_padre'
          AND conrelid = 'public.campos_deportivos'::regclass
    ) THEN
        ALTER TABLE public.campos_deportivos
            ADD CONSTRAINT fk_campos_deportivos_campo_padre
            FOREIGN KEY (id_campo_padre)
            REFERENCES public.campos_deportivos(id_campo)
            ON DELETE RESTRICT;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'ck_campos_deportivos_tipo_espacio'
          AND conrelid = 'public.campos_deportivos'::regclass
    ) THEN
        ALTER TABLE public.campos_deportivos
            ADD CONSTRAINT ck_campos_deportivos_tipo_espacio
            CHECK (tipo_espacio IN ('CAMPO', 'SUBAREA'));
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'ck_campos_deportivos_no_self_parent'
          AND conrelid = 'public.campos_deportivos'::regclass
    ) THEN
        ALTER TABLE public.campos_deportivos
            ADD CONSTRAINT ck_campos_deportivos_no_self_parent
            CHECK (id_campo_padre IS NULL OR id_campo_padre <> id_campo);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_campos_deportivos_campo_padre
    ON public.campos_deportivos (id_campo_padre)
    WHERE id_campo_padre IS NOT NULL;

UPDATE public.campos_deportivos
SET tipo_espacio = 'SUBAREA',
    id_campo_padre = (
        SELECT padre.id_campo
        FROM public.campos_deportivos padre
        JOIN public.complejos_deportivos cp ON cp.id_complejo = padre.id_complejo
        WHERE upper(cp.nombre) = 'ESTADIO MUNICIPAL'
          AND upper(padre.nombre) = 'CAMPO DE PISTA ATLÉTICA'
        LIMIT 1
    )
WHERE id_complejo = (
        SELECT id_complejo
        FROM public.complejos_deportivos
        WHERE upper(nombre) = 'ESTADIO MUNICIPAL'
        LIMIT 1
    )
  AND upper(nombre) LIKE 'AREA DE ATLETISMO%';

COMMIT;
*/

