# RUAT: solicitud, respuesta y diagnóstico

## Error observado

RUAT respondió HTTP 400, `text/html;charset=UTF-8`, con:

> Unrecognized field "numero" (class ruatserviciostasoi.dao.beans.DatosConcepto), not marked as ignorable

El rechazo ocurre al deserializar la solicitud, antes de producir una respuesta JSON de negocio. `datosConcepto` debe enviar `codigoClasificador`, `tipoArancel` y `monto`, sin `numero`. La tabla del PDF y sus ejemplos difieren; el servicio desplegado rechaza ese campo. Ambos DTO aceptan `numero` de clientes antiguos únicamente como entrada y no lo serializan hacia RUAT. El frontend ya no lo envía ni lo exige.

## Transformaciones explícitas de solicitudes

- Frontend: construye los DTO; calcula importes de reservas/mensualidades con dos decimales; selecciona los tres campos del concepto; adapta `motivo` a `motivoTasa`. El registro de contribuyente incluye motivo y observación propios de Deportes.
- Deportes-be: extrae los contenedores `tasa`, `registro`, etc.; agrega el operador configurado y, para tasas, el servicio municipal configurado.
- Ruat-be: agrega el código de alcaldía configurado y `tipoTasa=TO` donde corresponde; valida campos; administra autenticación y token. No reformatea el texto de `monto` recibido.

Esto no es un proxy de solicitudes arbitrarias: los DTO definen qué campos admite cada operación. No se afirma que todos los valores de entrada atraviesen el sistema sin adaptación.

## Respuestas

Los endpoints de negocio de ambos backends propagan el estado HTTP, tipo de contenido y cuerpo recibido sin sustituir el mensaje de RUAT. Si RUAT devuelve JSON, se recibe JSON; si rechaza la solicitud con HTML, se recibe ese HTML. Los errores locales de validación, seguridad o transporte siguen siendo locales y se distinguen por `X-Response-Origin`. `/auth/check` tiene un contrato local de disponibilidad.

El frontend analiza JSON para leer `datos`, `mensaje` y `continuarFlujo`; esto es necesario para presentar la respuesta y decidir si continúa el flujo. No convierte HTTP 4xx en respuestas exitosas ni deduce una anulación exitosa de palabras del mensaje. Para texto, decodifica entidades habituales como `&quot;` únicamente para mostrar texto legible; no inserta HTML remoto. La respuesta de red permanece disponible sin esa adaptación visual.

## Consola

Ambos backends usan el logger SLF4J `ruat.http`, activo en INFO por defecto. Se registra cada solicitud de negocio después de serializarla y cada respuesta antes de que el cliente HTTP la interprete, tanto exitosa como fallida.

- Deportes-be: `hop=deportes-be->ruat-be` muestra la solicitud enviada al gateway y la respuesta recibida del gateway.
- Ruat-be: `hop=ruat-be->RUAT` muestra la solicitud enviada a RUAT y su respuesta.
- `direction=request`: método, URL, Content-Type, cantidad de bytes y cuerpo completo.
- `direction=response`: estado HTTP, Content-Type, cantidad de bytes, duración y cuerpo completo.
- `requestId`: permite seguir la misma operación en ambas consolas; coincide con `X-Request-ID`.
- `complete=false`: fallo de transporte; no se recibió un cuerpo completo.

El campo `body` aparece debajo de los metadatos, en varias líneas. JSON se presenta con sangría únicamente en el registro; otros cuerpos se muestran como texto. No se truncan cuerpos. Las respuestas se conservan para su lectura normal después del registro; el logger no las modifica. No se registran cabeceras con credenciales ni los intercambios de autenticación con RUAT.

`RUAT_HTTP_LOG_LEVEL=INFO` habilita los cuerpos; `WARN` los deshabilita. El valor predeterminado es INFO. Reiniciar ambos backends para aplicar el código y la configuración. Los cuerpos pueden contener datos personales y deben tener acceso restringido como el resto de los registros del backend.

Los prints directos de consola en deportes-be se retiraron: los mensajes de depuración periódica de reservas se eliminaron y los errores usan SLF4J.

Validación: compilación Java de ambos backends, sin pruebas automatizadas ni solicitudes reales de registro.
Corrección del interceptor: leer getRawStatusCode() antes de getBody() para que el cliente JDK seleccione correctamente el flujo de error. La lectura del cuerpo antes del estado podía convertir respuestas HTTP de error en IOException/FileNotFoundException y ocultar el cuerpo original en ambos saltos. Los registros del 17/09 confirman registros exitosos 36122 y 36123, pero no conservan los cuerpos de las operaciones afectadas por este defecto.
