# Deportes Error Diagnosis Guide

Esta guía está enfocada en el proyecto Deportes y cubre sus dependencias principales:

```text
Frontend → deportes-be → ruat-be → RUAT
Frontend → deportes-be → servicio Usuarios
```

## 1. Empieza en el frontend

Abre `F12 → Network`, activa **Preserve log**, reproduce el error una sola vez y abre la solicitud fallida.

Guarda:

- URL y método HTTP.
- Request payload.
- Status HTTP.
- Response headers y response body.
- `X-Request-ID`.
- `X-Response-Origin`.
- Hora exacta y usuario/pantalla donde ocurrió.

También revisa la pestaña **Console** por errores de Angular o JavaScript. El mensaje visual puede ser solo un resumen; el body real de Network es la evidencia principal.

### Significado rápido del status

| Evidencia | Probable causa |
| --- | --- |
| Status `0` | Backend apagado, CORS, proxy, DNS o red |
| No aparece solicitud en Network | Validación, permisos o error JavaScript antes de HTTP |
| `400` | Payload o validación incorrecta |
| `401` | Sesión, credenciales o autorización |
| `403` | Permisos o acceso rechazado |
| `500` | Error interno del backend |
| `502` | Gateway, servicio upstream o timeout |

## 2. Revisa `deportes-be`

Busca el mismo `X-Request-ID` en la consola de `deportes-be`.

Líneas importantes:

```text
stage=deportes-to-gateway
hop=deportes-be->ruat-be direction=request
hop=deportes-be->ruat-be direction=response
stage=gateway-transport
stage=gateway-response
```

Interpretación:

- Sin `stage=deportes-to-gateway`: la solicitud no llegó a la integración RUAT.
- Request presente sin response: conexión o timeout hacia `ruat-be`.
- `X-Response-Origin=ruat-be`: error generado localmente por el gateway.
- `X-Response-Origin=RUAT`: respuesta real del sistema externo RUAT.
- `stage=gateway-transport`: no se recibió una respuesta HTTP completa.

Revisa especialmente:

- `RUAT_GATEWAY_BASE_URL`.
- `RUAT_GATEWAY_INTERNAL_API_KEY`.
- `APPLICATION_NAME`.
- `external.client.connect-timeout` (`EXTERNAL_CLIENT_CONNECT_TIMEOUT`, por defecto `10s`) y `external.client.read-timeout` (`EXTERNAL_CLIENT_READ_TIMEOUT`, por defecto `180s`): límites separados por fase, compartidos por todas las llamadas HTTP salientes a RUAT y usuarios, manuales y revisiones de listas. No son un límite total de duración de la lista. Sustituyen las propiedades `ruat.client.*-timeout` y la propiedad temporal `external.client.timeout`; revisar los overrides del despliegue al actualizar. Un timeout no confirma que RUAT haya rechazado una operación; no reenviar anulaciones sin verificar su resultado.
- En `ruat-be`, el cliente compartido usa `ruat.client.connect-timeout=${RUAT_CLIENT_CONNECT_TIMEOUT:10s}` y `ruat.client.read-timeout=${RUAT_CLIENT_READ_TIMEOUT:150s}`. Ese margen no garantiza un plazo total: autenticación y operaciones encadenadas pueden acumular esperas. Verificar el proxy activo y las variables de entorno del servidor al desplegar; `nginx.host.conf.backup` no acredita la configuración activa.
- Errores de base de datos.
- Errores de permisos de Deportes.
- Errores de mapeo o parsing JSON.
- Si el error ocurrió antes o después de guardar datos locales.

## 3. Revisa `ruat-be`

Busca el mismo `X-Request-ID` en la consola de `ruat-be`.

Líneas importantes:

```text
stage=gateway-guard
stage=ruat-auth
stage=ruat-operation
hop=ruat-be->RUAT direction=request
hop=ruat-be->RUAT direction=response
stage=ruat-operation-response
stage=ruat-transport
```

Interpretación:

- `gateway-guard accepted=false`: API key o sistema no autorizado; RUAT no fue llamado.
- `gateway-guard accepted=true`: el caller fue aceptado.
- Request y response de RUAT presentes: analizar el body real recibido.
- Request presente con `responseReceived=false`: timeout o conexión fallida; el resultado puede ser incierto.
- `stage=ruat-auth`: problema de autenticación técnica o token municipal.
- HTTP `400/401/403/404/500` con origen `RUAT`: respuesta real de RUAT.
- HTTP `502` con `respuestaHttpRecibida=false`: no hubo respuesta completa del upstream.

Revisa:

- `RUAT_BASE_URL`.
- `RUAT_AUTH_USERNAME` y `RUAT_AUTH_PASSWORD`.
- `RUAT_MUNICIPALITY_CODE`.
- `RUAT_GATEWAY_ALLOWED_SYSTEMS`.
- `RUAT_GATEWAY_INTERNAL_API_KEY`.
- URL, Content-Type y body de la respuesta externa.

El logger `ruat.http` muestra el método, URL, body enviado, status y body recibido.

## 4. Revisa el servicio Usuarios

Primero confirma en Network si el error corresponde a:

- `/auth/login`.
- `/usuarios`.
- `/usuarios/{uuid}`.
- `/usuarios/user`.
- `/usuarios/person`.
- `/usuarios/change-password`.

Después revisa en `deportes-be`:

- `USUARIOS_BASE_URL`.
- Status y body devueltos por Usuarios.
- Timeout o error de conexión.
- Existencia y estado activo del usuario.
- Roles, permisos y asignaciones.

Diferencias importantes:

- `401/403`: credenciales o permisos de Usuarios.
- Status `0`: Usuarios no es accesible o existe un problema de red.
- Timeout: Usuarios está caído o demasiado lento.
- `500`: error interno de Usuarios.
- Login exitoso pero RUAT fallido: la autenticación funcionó; el problema es otra etapa.

Si falla el login, no empieces investigando RUAT: la consulta RUAT probablemente nunca se inició.

## 5. Consultas y operaciones que modifican datos

Las búsquedas y consultas de pago normalmente pueden repetirse después de revisar el error.

Ten cuidado con:

- Registro de contribuyente.
- Registro de tasa.
- Anulación de tasa.

Si una operación modificadora llegó a RUAT pero no tuvo respuesta:

1. Guarda el `X-Request-ID`.
2. Busca el ID en `deportes-be` y `ruat-be`.
3. Confirma si RUAT devolvió cualquier respuesta.
4. Verifica el resultado en RUAT antes de reenviar.
5. No repitas automáticamente una operación incierta.

## 6. Plantilla para reportar un error

```text
Fecha y hora:
Usuario:
Pantalla/acción:
URL del frontend:
Status HTTP:
X-Request-ID:
X-Response-Origin:
Body enviado, sin secretos:
Body recibido:
Última etapa en frontend:
Última etapa en deportes-be:
Última etapa en ruat-be:
¿Usuarios involucrado?: sí/no
¿Modificaba datos?: sí/no
¿Se repitió la solicitud?: sí/no
```

## Regla principal

Siempre sigue este orden:

```text
Frontend Network
→ copiar X-Request-ID
→ buscar en deportes-be
→ buscar en ruat-be
→ analizar request y response reales
→ revisar Usuarios solo si el flujo lo utiliza
```

No diagnostiques únicamente con el mensaje visual del frontend y no repitas registros o anulaciones hasta confirmar qué ocurrió en RUAT.
