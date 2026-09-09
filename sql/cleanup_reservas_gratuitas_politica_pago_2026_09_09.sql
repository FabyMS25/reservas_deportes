-- Limpieza para reservas gratuitas creadas antes de corregir la política de pago.
-- Ejecutar en pgAdmin sobre la base db_deportes.
--
-- Objetivo:
--   - Mantener estado_reserva como está, por ejemplo PENDIENTE.
--   - Evitar cancelación automática por falta de pago en reservas gratuitas.
--   - Quitar fecha_limite_pago porque no existe tasa pendiente que vencer.
--   - No tocar reservas pagadas, cobradas, no gratuitas, ni el flujo RUAT.

BEGIN;

UPDATE public.reservas
SET
    cancelacion_automatica = false,
    fecha_limite_pago = NULL,
    motivo_cancelacion_automatica = COALESCE(
        NULLIF(TRIM(motivo_cancelacion_automatica), ''),
        'Reserva gratuita: no requiere pago ni cancelación automática por falta de pago.'
    ),
    fecha_actualizacion = NOW()
WHERE COALESCE(es_reserva_gratuita, false) = true
  AND (
      COALESCE(cancelacion_automatica, true) = true
      OR fecha_limite_pago IS NOT NULL
      OR motivo_cancelacion_automatica IS NULL
      OR TRIM(motivo_cancelacion_automatica) = ''
  );

-- Verificación: esta consulta debe devolver 0 filas.
SELECT
    id_reserva,
    numero_reserva,
    estado_reserva,
    tipo_reserva,
    es_reserva_gratuita,
    cancelacion_automatica,
    fecha_limite_pago
FROM public.reservas
WHERE COALESCE(es_reserva_gratuita, false) = true
  AND (
      COALESCE(cancelacion_automatica, true) = true
      OR fecha_limite_pago IS NOT NULL
  );

COMMIT;
