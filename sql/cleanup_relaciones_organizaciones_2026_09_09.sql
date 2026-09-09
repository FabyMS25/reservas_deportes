-- Cleanup after migrating organizations from one representative to many representatives.
-- Run this in pgAdmin against db_deportes after backing up production.
-- This script does NOT modify reservas, reservas_recurrentes, reservas_puntuales, or campos_deportivos.

BEGIN;

-- 1) Keep/make the many-to-many table the source of truth.
CREATE TABLE IF NOT EXISTS public.organizaciones_representantes (
    id_organizacion bigint NOT NULL,
    id_contribuyente bigint NOT NULL,
    CONSTRAINT organizaciones_representantes_pkey PRIMARY KEY (id_organizacion, id_contribuyente),
    CONSTRAINT fk_org_representantes_organizacion FOREIGN KEY (id_organizacion)
        REFERENCES public.organizaciones (id_organizacion) ON DELETE CASCADE,
    CONSTRAINT fk_org_representantes_contribuyente FOREIGN KEY (id_contribuyente)
        REFERENCES public.contribuyentes (id_contribuyente)
);

-- 2) If the old organizaciones.id_representante column still exists, back it up and migrate it.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'organizaciones'
          AND column_name = 'id_representante'
    ) THEN
        CREATE TABLE IF NOT EXISTS public.migration_backup_20260909_organizaciones_id_representante (
            id_organizacion bigint,
            uuid character varying(50),
            nombre character varying(200),
            id_representante bigint,
            backup_created_at timestamp with time zone
        );

        INSERT INTO public.migration_backup_20260909_organizaciones_id_representante (
            id_organizacion,
            uuid,
            nombre,
            id_representante,
            backup_created_at
        )
        SELECT
            o.id_organizacion,
            o.uuid,
            o.nombre,
            o.id_representante,
            now()
        FROM public.organizaciones o
        WHERE NOT EXISTS (
            SELECT 1
            FROM public.migration_backup_20260909_organizaciones_id_representante b
            WHERE b.id_organizacion = o.id_organizacion
        );

        INSERT INTO public.organizaciones_representantes (id_organizacion, id_contribuyente)
        SELECT o.id_organizacion, o.id_representante
        FROM public.organizaciones o
        WHERE o.id_representante IS NOT NULL
          AND EXISTS (
              SELECT 1
              FROM public.contribuyentes c
              WHERE c.id_contribuyente = o.id_representante
          )
        ON CONFLICT (id_organizacion, id_contribuyente) DO NOTHING;
    END IF;
END $$;

-- 3) Remove orphaned organization-representative rows, if any were left by manual testing.
DELETE FROM public.organizaciones_representantes r
WHERE NOT EXISTS (
    SELECT 1 FROM public.organizaciones o
    WHERE o.id_organizacion = r.id_organizacion
)
OR NOT EXISTS (
    SELECT 1 FROM public.contribuyentes c
    WHERE c.id_contribuyente = r.id_contribuyente
);

-- 4) Stop if any active organization would be left with no representative.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM public.organizaciones o
        WHERE COALESCE(o.activo, true) = true
          AND NOT EXISTS (
              SELECT 1
              FROM public.organizaciones_representantes r
              WHERE r.id_organizacion = o.id_organizacion
          )
    ) THEN
        RAISE EXCEPTION 'Hay organizaciones activas sin representantes en organizaciones_representantes. Revise antes de eliminar id_representante.';
    END IF;
END $$;

-- 5) Drop any FK/index attached to organizaciones.id_representante, then drop the obsolete column.
DO $$
DECLARE
    rec record;
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'organizaciones'
          AND column_name = 'id_representante'
    ) THEN
        FOR rec IN
            SELECT conname
            FROM pg_constraint
            WHERE conrelid = 'public.organizaciones'::regclass
              AND pg_get_constraintdef(oid) ILIKE '%id_representante%'
        LOOP
            EXECUTE format('ALTER TABLE public.organizaciones DROP CONSTRAINT IF EXISTS %I', rec.conname);
        END LOOP;

        FOR rec IN
            SELECT indexname
            FROM pg_indexes
            WHERE schemaname = 'public'
              AND tablename = 'organizaciones'
              AND indexdef ILIKE '%id_representante%'
        LOOP
            EXECUTE format('DROP INDEX IF EXISTS public.%I', rec.indexname);
        END LOOP;

        ALTER TABLE public.organizaciones DROP COLUMN id_representante;
    END IF;
END $$;

-- 6) Required indexes for the relationship table.
CREATE INDEX IF NOT EXISTS idx_org_representantes_organizacion
    ON public.organizaciones_representantes (id_organizacion);

CREATE INDEX IF NOT EXISTS idx_org_representantes_contribuyente
    ON public.organizaciones_representantes (id_contribuyente);

-- 7) Normalize student-school history: dar de baja must keep the row but mark it inactive.
UPDATE public.inscripciones_escuela
SET activo = false
WHERE fecha_baja IS NOT NULL
  AND activo IS DISTINCT FROM false;

UPDATE public.inscripciones_escuela
SET fecha_baja = CURRENT_DATE
WHERE activo = false
  AND fecha_baja IS NULL;

-- 8) Optional cleanup of the unused/confusing student-responsible table.
-- Current application code uses inscripciones_escuela.id_responsable as the responsible
-- for that specific student-school relationship. The estudiantes_responsables table is
-- not used by the backend/frontend and was empty in the reviewed local DB.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'estudiantes_responsables'
    ) THEN
        CREATE TABLE IF NOT EXISTS public.migration_backup_20260909_estudiantes_responsables AS
        SELECT *, now() AS backup_created_at
        FROM public.estudiantes_responsables
        WHERE false;

        INSERT INTO public.migration_backup_20260909_estudiantes_responsables
        SELECT er.*, now()
        FROM public.estudiantes_responsables er;

        DROP TABLE public.estudiantes_responsables;
    END IF;
END $$;

COMMIT;

-- Validation queries to run after COMMIT.
SELECT 'organizaciones_sin_representante' AS check_name, count(*) AS total
FROM public.organizaciones o
WHERE COALESCE(o.activo, true) = true
  AND NOT EXISTS (
      SELECT 1
      FROM public.organizaciones_representantes r
      WHERE r.id_organizacion = o.id_organizacion
  );

SELECT 'columna_id_representante_existe' AS check_name, count(*) AS total
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'organizaciones'
  AND column_name = 'id_representante';

SELECT 'tabla_estudiantes_responsables_existe' AS check_name, count(*) AS total
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_name = 'estudiantes_responsables';

SELECT 'inscripciones_inactivas_sin_fecha_baja' AS check_name, count(*) AS total
FROM public.inscripciones_escuela
WHERE activo = false
  AND fecha_baja IS NULL;
