-- Allow the same key to have one value per scope/reference.
-- Resolution order in the application is:
-- exact aplica_a + referencia_uuid, aplica_a default, global default.
DO $$
DECLARE
    constraint_name text;
BEGIN
    FOR constraint_name IN
        SELECT con.conname
        FROM pg_constraint con
        JOIN pg_class rel ON rel.oid = con.conrelid
        JOIN pg_namespace nsp ON nsp.oid = rel.relnamespace
        WHERE rel.relname = 'configuracion_sistema'
          AND con.contype = 'u'
          AND pg_get_constraintdef(con.oid) = 'UNIQUE (clave)'
    LOOP
        EXECUTE format(
            'ALTER TABLE configuracion_sistema DROP CONSTRAINT %I',
            constraint_name
        );
    END LOOP;
END $$;

DROP INDEX IF EXISTS idx_configuracion_clave;

CREATE INDEX IF NOT EXISTS idx_configuracion_clave
    ON configuracion_sistema (clave);

CREATE UNIQUE INDEX IF NOT EXISTS uq_configuracion_scope_activa
    ON configuracion_sistema (
        clave,
        COALESCE(aplica_a, 'global'),
        COALESCE(referencia_uuid, '')
    )
    WHERE estado = false;

-- Repair legacy/reseed audit rows whose update time predates creation.
UPDATE configuracion_sistema
SET fecha_actualizacion = GREATEST(
        COALESCE(fecha_actualizacion, fecha_creacion),
        fecha_creacion
    ),
    ultima_modificacion = GREATEST(
        COALESCE(ultima_modificacion, fecha_creacion),
        fecha_creacion
    ),
    modificado_por = COALESCE(
        NULLIF(BTRIM(modificado_por), ''),
        'MIGRACION_CONFIGURACIONES_20260729'
    )
WHERE fecha_actualizacion IS NULL
   OR fecha_actualizacion < fecha_creacion
   OR ultima_modificacion IS NULL
   OR ultima_modificacion < fecha_creacion
   OR modificado_por IS NULL
   OR BTRIM(modificado_por) = '';
