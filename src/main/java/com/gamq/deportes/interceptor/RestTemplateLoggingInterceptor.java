package com.gamq.deportes.interceptor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.*;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** Observes every outgoing HTTP exchange without consuming or changing its response. */
public class RestTemplateLoggingInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger log = LoggerFactory.getLogger("external.http");
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final int MAX_BODY = 65536;
    private final URI usuarios;
    private final URI ruat;

    public RestTemplateLoggingInterceptor(String usuariosUrl, String ruatUrl) {
        usuarios = URI.create(usuariosUrl);
        ruat = URI.create(ruatUrl);
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        String target = matches(request.getURI(), usuarios) ? "usuarios"
                : matches(request.getURI(), ruat) ? "ruat" : "external";
        String id = correlationId(request);
        String url = safeUrl(request.getURI());
        long started = System.nanoTime();
        if (log.isInfoEnabled()) log.info(
                "requestId={} hop=deportes-be->{} direction=request method={} url={} contentType={} bytes={}\nbody:\n{}\n[end request]",
                id, target, request.getMethod(), url, request.getHeaders().getContentType(), body.length,
                safeBody(body, false));
        try {
            ClientHttpResponse response = execution.execute(request, body);
            int status = response.getRawStatusCode();
            long elapsed = (System.nanoTime() - started) / 1_000_000;
            String summary = "requestId={} hop=deportes-be->{} direction=response method={} url={} status={} elapsedMs={} contentType={}";
            Object[] args = {id, target, request.getMethod(), url, status, elapsed, response.getHeaders().getContentType()};
            if (status >= 500) log.error(summary, args);
            else if (status >= 400) log.warn(summary, args);
            else log.info(summary, args);
            // RestTemplate still owns and consumes the same stream. Capture only a bounded copy.
            return new ClientHttpResponse() {
                private InputStream observed;
                private final AtomicBoolean finished = new AtomicBoolean();
                private final ByteArrayOutputStream captured = new ByteArrayOutputStream();
                private long count;
                private boolean eof;

                private void finish() {
                    if (!finished.compareAndSet(false, true) || !log.isInfoEnabled()) return;
                    boolean complete = eof || (response.getHeaders().getContentLength() >= 0
                            && count == response.getHeaders().getContentLength());
                    String printable = complete ? safeBody(captured.toByteArray(), count > MAX_BODY)
                            : "[cuerpo no leído completamente; contenido omitido]";
                    log.info("requestId={} hop=deportes-be->{} direction=response-body url={} status={} bytes={}\nbody:\n{}\n[end response]",
                            id, target, url, status, count, printable);
                }
                private void capture(byte[] bytes, int offset, int length) {
                    int remaining = Math.max(0, MAX_BODY - captured.size());
                    if (remaining > 0) captured.write(bytes, offset, Math.min(remaining, length));
                    count += length;
                }
                @Override public InputStream getBody() throws IOException {
                    if (observed == null) observed = new FilterInputStream(response.getBody()) {
                        @Override public int read() throws IOException {
                            try {
                                int value = in.read();
                                if (value < 0) { eof = true; finish(); }
                                else capture(new byte[]{(byte)value}, 0, 1);
                                return value;
                            } catch (IOException ex) { readFailure(ex); throw ex; }
                        }
                        @Override public int read(byte[] bytes, int offset, int length) throws IOException {
                            try {
                                int read = in.read(bytes, offset, length);
                                if (read < 0) { eof = true; finish(); }
                                else if (read > 0) capture(bytes, offset, read);
                                return read;
                            } catch (IOException ex) { readFailure(ex); throw ex; }
                        }
                        private void readFailure(IOException ex) {
                            log.warn("requestId={} hop=deportes-be->{} direction=error phase=response-read url={} failure={}",
                                    id, target, url, ex.getClass().getSimpleName());
                        }
                        @Override public void close() throws IOException {
                            try { super.close(); } finally { finish(); }
                        }
                    };
                    return observed;
                }
                @Override public HttpStatus getStatusCode() throws IOException { return response.getStatusCode(); }
                @Override public int getRawStatusCode() throws IOException { return response.getRawStatusCode(); }
                @Override public String getStatusText() throws IOException { return response.getStatusText(); }
                @Override public HttpHeaders getHeaders() { return response.getHeaders(); }
                @Override public void close() {
                    try { response.close(); } finally { finish(); }
                }
            };
        } catch (IOException | RuntimeException ex) {
            // Exception messages can contain passwords/URLs. Keep the type and timing, not raw messages.
            log.error("requestId={} hop=deportes-be->{} direction=error method={} url={} elapsedMs={} failure={}",
                    id, target, request.getMethod(), url, (System.nanoTime() - started) / 1_000_000,
                    ex.getClass().getSimpleName());
            throw ex;
        }
    }

    private boolean matches(URI value, URI base) {
        String prefix = Optional.ofNullable(base.getPath()).orElse("").replaceAll("/+$", "");
        return Objects.equals(value.getScheme(), base.getScheme()) && Objects.equals(value.getHost(), base.getHost())
                && value.getPort() == base.getPort()
                && (value.getPath().equals(prefix) || value.getPath().startsWith(prefix + "/"));
    }

    private String correlationId(HttpRequest request) {
        ServletRequestAttributes context = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String id = request.getHeaders().getFirst("X-Request-ID");
        if (id == null && context != null) id = (String)context.getRequest().getAttribute("ruat.requestId");
        if (id == null || !id.matches("[A-Za-z0-9._-]{1,100}")) id = UUID.randomUUID().toString();
        request.getHeaders().set("X-Request-ID", id);
        if (context != null) {
            context.getRequest().setAttribute("ruat.requestId", id);
            if (context.getResponse() != null) context.getResponse().setHeader("X-Request-ID", id);
        }
        return id;
    }

    static String safeUrl(URI uri) {
        StringBuilder url = new StringBuilder().append(uri.getScheme()).append("://").append(uri.getHost());
        if (uri.getPort() >= 0) url.append(':').append(uri.getPort());
        url.append(uri.getRawPath());
        if (uri.getRawQuery() != null) {
            List<String> parts = new ArrayList<>();
            for (String pair : uri.getRawQuery().split("&")) {
                String[] values = pair.split("=", 2);
                String key = URLDecoder.decode(values[0], StandardCharsets.UTF_8);
                boolean allowed = Set.of("uuidUsuario", "codigoModulo", "numeroTasa", "page", "size").contains(key);
                parts.add(URLEncoder.encode(key, StandardCharsets.UTF_8) + "="
                        + (allowed && values.length > 1 ? values[1] : "[REDACTED]"));
            }
            url.append('?').append(String.join("&", parts));
        }
        return url.toString().replace("\r", "").replace("\n", "");
    }

    static String safeBody(byte[] bytes, boolean oversized) {
        if (oversized || bytes.length > MAX_BODY) return "[cuerpo mayor a 64 KiB; contenido omitido]";
        if (bytes.length == 0) return "[vacío]";
        try {
            JsonNode body = JSON.readTree(bytes);
            if (body == null || !body.isContainerNode()) return "[cuerpo no estructurado omitido por seguridad]";
            sanitize(body);
            return JSON.writerWithDefaultPrettyPrinter().writeValueAsString(body);
        } catch (Exception ex) {
            return "[cuerpo no JSON omitido por seguridad]";
        }
    }

    private static void sanitize(JsonNode value) {
        if (value.isObject()) {
            ObjectNode object = (ObjectNode)value;
            List<String> fields = new ArrayList<>();
            object.fieldNames().forEachRemaining(fields::add);
            for (String key : fields) {
                String name = Normalizer.normalize(key, Normalizer.Form.NFD)
                        .replaceAll("\\p{M}", "").replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
                if (sensitive(name)) object.put(key, "[REDACTED]");
                else if (object.get(key).isTextual()) object.put(key, safeText(object.get(key).asText()));
                else sanitize(object.get(key));
            }
        } else if (value.isArray()) {
            for (int i = 0; i < value.size(); i++) {
                if (value.get(i).isTextual()) ((com.fasterxml.jackson.databind.node.ArrayNode)value).set(i,
                        com.fasterxml.jackson.databind.node.TextNode.valueOf(safeText(value.get(i).asText())));
                else sanitize(value.get(i));
            }
        }
    }
    private static boolean sensitive(String name) {
        return name.contains("password") || name.contains("contrasena") || name.contains("token")
                || name.contains("secret") || name.contains("apikey") || name.contains("authorization")
                || name.contains("cookie") || name.contains("credencial") || name.contains("credential")
                || Set.of("pass", "clave", "username", "user", "email", "correo", "correoelectronico",
                "telefono", "celular", "direccion", "numerodocumento", "ci", "nombre", "nombres",
                "nombrecompleto", "primerapellido", "segundoapellido", "fechanacimiento", "foto", "imagen").contains(name);
    }
    private static String safeText(String text) {
        return text.replaceAll("(?i)Bearer\\s+[^\\s\\\"<>]+", "Bearer [REDACTED]")
                .replaceAll("eyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+", "[REDACTED]")
                .replaceAll("(?i)((?:password|contrasena|token|secret|api[_-]?key)\\s*[:=]\\s*)[^\\s,;]+", "$1[REDACTED]");
    }
}
