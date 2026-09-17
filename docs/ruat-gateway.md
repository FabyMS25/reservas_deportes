# Deportes and ruat-be

## Configuration

Values and defaults live in each application's src/main/resources/application.properties. Java reads @Value("${property.name}") without a second default. Do not edit generated build/resources/main/application.properties.

Deportes uses ruat.gateway.base-url, ruat.gateway.internal-api-key, and deportes.application.name. These supply the destination, X-Api-Key, and X-System-Code. Existing key values were preserved. Environment overrides are optional when a nonempty default is configured. Separate processes do not automatically share configuration; a single physical secret definition requires a common external configuration source.

## Login flow

Deportes' normal login calls the existing gateway POST /api/ruat/auth/check once. GatewayAuthInterceptor checks the shared key and app name BEFORE the handler can call RUAT. A rejection prevents Deportes login. Once gateway access is accepted, RUAT authentication reports available true/false and a message. A municipal authentication failure is a status warning, not a gateway-access rejection.

Deportes still validates user credentials and permissions. Its successful login response includes ruatStatus and ruatMessage. The frontend uses these for the welcome notification without another authentication request. This single request/response flow waits for the combined result. A transport failure cannot confirm gateway acceptance and blocks login.

The separate /access/check endpoint and service were removed. The authenticated Deportes /api/ruat/login route remains for an explicit later status check, but is not automatically called by the frontend.

## Business operations

| Deportes POST | Gateway POST |
| --- | --- |
| /api/ruat/buscar | /api/ruat/taxpayers/search |
| /api/ruat/registro | /api/ruat/taxpayers |
| /api/ruat/creartasa | /api/ruat/taxes |
| /api/ruat/anulartasa | /api/ruat/taxes/annulments |
| /api/ruat/pago | /api/ruat/taxes/payments/query |

Deportes unwraps its frontend DTO and supplies its service/operator configuration. The gateway adds municipality/type and preserves RUAT business JSON. Business responses retain the upstream HTTP status, Content-Type and body bytes, including HTML, empty bodies and HTTP 401/403. Neither backend parses or rewrites these bodies. The frontend uses X-Response-Origin (exposed through CORS) to distinguish RUAT/gateway authorization failures from an expired Deportes session.

Tokens remain inside ruat-be. Business operations use the cached token, refreshed on demand after the configured cache duration or invalidation. Explicit authentication checks perform fresh authentication; overlapping successful refreshes share the result. There is no background renewal timer.

Tasa creation and reservation saving remain separate operations. A save failure can leave an existing tasa. Never automatically repeat creation. ruat-be has no database/persistence.

## Trace a request

In browser Network, inspect the request Payload and Response, then copy the X-Request-ID response header. Search that ID in both backend consoles:

- deportes-to-gateway: incoming route, outgoing operation and payload class.
- gateway-guard: acceptance/rejection, allowed system name when accepted, rejection HTTP status.
- ruat-auth: municipal authentication request/result.
- ruat-operation and ruat-operation-response: business operation and HTTP status.
- gateway-response: response status and Deportes-side elapsed time.

Normal INFO logs exclude passwords, API keys, tokens, taxpayer identities and complete payloads. The ID is diagnostic, never authorization. A guard rejection occurs before RUAT authentication. A ruat-auth 401/403 after guard acceptance identifies municipal credential rejection.

For raw business HTTP diagnostics, start **ruat-be** with `RUAT_HTTP_LOG_LEVEL=DEBUG` (IntelliJ environment variables, PowerShell `$env:RUAT_HTTP_LOG_LEVEL='DEBUG'`, or the gateway container environment). Restart the gateway after configuration/code changes. Its `RuatClient` logger records the final method/URL, outgoing business JSON (`ruat-request-body`), original upstream status/content type/length/timing (`ruat-operation-response`) and original decoded response text (`ruat-response-body`) without JSON parsing. This includes HTML and empty/non-object responses. Authentication headers and municipal login credentials are not logged. Business bodies can contain personal data; return this specific logger to INFO after diagnosing.

An upstream response is never rejected merely because it is not a JSON object. The browser receives the original status, content type and body; no diagnostic fields are inserted into that body. A transport failure with no complete HTTP response is a distinct local error. Prior discarded response bodies cannot be recovered from the generic error alone. Do not automatically repeat a tasa creation to obtain logs without checking whether it was registered.

For exact values during local manual debugging, set IntelliJ breakpoints:

1. Deportes ApiController: incoming frontend DTO.
2. Deportes AbstractClient.gatewayPost before exchange: outgoing payload after service/operator configuration.
3. Gateway ApiServiceImpl before client.post: outgoing request after municipality/type validation.
4. Gateway RuatTokenProvider.refreshToken: municipal authentication sends usuario/clave from gateway configuration only. Inspect locally; do not put secrets in logs/screenshots.
5. Gateway RuatClient: raw business response before JSON forwarding. RuatAvailabilityController handles authentication exceptions.

## Manual verification

Use `ruat-be/postman/gamq-ruat-be.postman_collection.json` from the gateway project. It contains manual requests with no scripts or automatic execution. Configure the base URL, local API key and allowed system name. Start with `POST /auth/check`: HTTP 200 confirms gateway acceptance; inspect `available` to distinguish successful municipal authentication from an unavailable RUAT service. Gateway 401/403 means rejection before calling RUAT.

Every request includes an optional UUID `X-Request-ID`; the response returns the ID used in both backends' logs. Then search a taxpayer, copy the returned taxpayer code, and use the individual registration/payment/annulment requests as needed. Creating or annulling a tasa affects RUAT; compilation does not perform these operations or verify the deployed database.
