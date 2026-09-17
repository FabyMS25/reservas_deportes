# Revisión de confiabilidad de registro RUAT — 17/09/2026

## Conclusión

El transporte corregido no equivale a un registro distribuido recuperable. No se puede garantizar que toda respuesta llegue a Deportes o al navegador. El flujo actual aún permite una tasa creada sin reserva local si falla el segundo paso. Este documento es una revisión del código, no una certificación ni una implementación de recuperación.

## Evidencia del incidente

Los registros aportados muestran las respuestas HTTP 200 con números 36122 y 36123 tanto en ruat-be como en deportes-be. No demuestran que el navegador recibiera la respuesta ni que la reserva se guardara. Los cuerpos de las llamadas que fallaron por el antiguo interceptor no están disponibles; no puede deducirse si crearon una tasa.

## Transporte revisado

- Los interceptores actuales registran la solicitud serializada y devuelven directamente `execution.execute`. No leen, envuelven ni cierran el flujo de respuesta.
- El cuerpo se registra después de que RestTemplate lo obtiene como `byte[]`, también para `RestClientResponseException`. Los fallos de formato del logger no sustituyen la respuesta.
- Los endpoints de negocio conservan estado, tipo de contenido y cuerpo de RUAT. Los fallos locales de transporte siguen siendo distintos de una respuesta RUAT.
- RuatClient no reenvía automáticamente una operación tras un 401 o un fallo de transporte. Un 401 invalida el token para solicitudes posteriores.
- `numero` no se serializa en conceptos; el gateway agrega alcaldía y los tipos de tasa correspondientes y conserva el importe recibido.
- Deportes espera 150 segundos; el gateway configura 10 segundos de conexión y 60 de lectura. La autenticación puede preceder a la operación. Estos tiempos no garantizan entrega frente a cortes, reinicios o intermediarios.

## Riesgo pendiente: separación entre tasa y reserva

En `mensuality-workflow.ts`, primero se espera `createTasaInRuat`, luego se asigna `numeroTasa` y finalmente se ejecuta `saveReservaPeriodo`. En `reservation-form.component.ts`, `registerReservationInRuat` llama a `saveReservation` después de recibir el número.

El navegador coordina ambos pasos. No hay una operación persistente que vincule esos dos pasos en los métodos revisados. Un cierre del navegador, pérdida de respuesta o fallo al guardar deja un resultado externo que el sistema local puede no conservar. Volver a comenzar el flujo puede emitir otra tasa.

## Cambio necesario para recuperación

1. Deportes persiste una operación y el borrador completo de reserva antes de llamar a RUAT. La operación tiene una clave estable por intento lógico, propietario y estado.
2. Un único ejecutor envía la solicitud. Solicitudes repetidas con esa clave consultan o recuperan el mismo intento, sin emitir otra tasa.
3. Deportes conserva la respuesta original y número recibido y finaliza la reserva desde el backend. Un fallo de guardado reintenta solamente la persistencia local.
4. Un resultado incierto permanece pendiente de conciliación, incluso después de un reinicio. No se interpreta como rechazo ni se reenvía automáticamente.
5. Resolver un corte después de que RUAT acepte requiere un identificador idempotente reconocido por RUAT, o una consulta oficial que permita encontrar inequívocamente esa operación. No debe inventarse un parámetro RUAT para ello ni usar solo monto/contribuyente como identidad: dos tasas legítimas pueden coincidir.
6. La interfaz consulta el estado de la operación y muestra por separado tasa recibida, reserva guardada y conciliación pendiente.

La clave local evita reenvíos locales, pero por sí sola no garantiza exactamente una ejecución dentro de RUAT. El diseño debe conservar explícitamente esa incertidumbre.

## Alcance de validación

Inspección de ambos clientes HTTP, interceptores, autenticación, tiempos, DTO de conceptos y flujos de creación del frontend. No se ejecutaron registros reales ni pruebas automatizadas. No se consultó la base de datos para confirmar las reservas de 36122 y 36123.
