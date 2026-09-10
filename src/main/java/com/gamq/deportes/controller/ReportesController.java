package com.gamq.deportes.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamq.deportes.dto.ReservaRecurrenteDto;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.model.ReservaTarifaDetalle;
import com.gamq.deportes.repository.ReservaTarifaDetalleRepository;
import com.gamq.deportes.service.ReservaRecurrenteService;
import com.gamq.deportes.service.SistemaAuditoriaService;
import com.gamq.deportes.utils.GeneradorReporte;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.sf.jasperreports.engine.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@PreAuthorize("denyAll()")
@Controller
@RequestMapping("/api/reporte")
@Tag(name = "Reportes", description = "API para Reportes imprimibles del Sistema")
public class ReportesController {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private GeneradorReporte generadorReporte;

     @Autowired
     private ReservaRecurrenteService reservaRecurrenteService;

    @Autowired
    private SistemaAuditoriaService sistemaAuditoriaService;

    @Autowired
    private ReservaTarifaDetalleRepository reservaTarifaDetalleRepository;

    ///////////////reserva puntual por hora/////////////////
    @Operation(summary = "Reporte de ticket de Reserva Puntual")
    @RequestMapping(value="/ticket-reservap",method= RequestMethod.GET)
    @ResponseBody
    @PreAuthorize("@permissions.ticket('puntual',#p0)")
    public void reporteTicketReserve (
            @RequestParam(name = "uuid", defaultValue = "0") String uuid,
            @RequestHeader Map<String, String> headers,
            HttpServletResponse response
    ) {
        try {
            //usuario
            //String usuario = headers.getOrDefault("usuario", "Default");
            HashMap<String, Object> parametros = new HashMap<String,Object>();
            InputStream logoStream = getClass().getResourceAsStream("/imagenes/gamq1.jpg");
            parametros.put("logoPath", logoStream);
            parametros.put("uuid", uuid);
            parametros.put("detalle_tarifario", buildDetalleTarifario(uuid));
            parametros.put(JRParameter.REPORT_LOCALE, new Locale("es", "ES"));
            try {
                generadorReporte.generarSqlReportePdf(
                        "ticket",
                        "classpath:reportes/ticketPuntual.jrxml",
                        parametros,
                        response
                );
                registrarReporteGenerado(headers, "TICKET_RESERVA_PUNTUAL", "reserva", uuid,
                        "Generó ticket de reserva puntual por hora", "{\"uuid\":\"" + safeValue(uuid) + "\"}");
            } catch (SQLException ex) {
                response.setStatus(500);
                throw new RuntimeException(ex);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(e.getLocalizedMessage());
            sendErrorResponse(response, 500, "Error inesperado al generar ticket de reserva puntual.");
        }
    }
    ///////////////reserva puntual por persona/////////////////
    @Operation(summary = "Reporte de ticket de Reserva Puntual por Persona")
    @RequestMapping(value="/ticket-reser-per",method= RequestMethod.GET)
    @ResponseBody
    @PreAuthorize("@permissions.ticket('puntual',#p0)")
    public void reporteTicketReservePersona(
            @RequestParam(name = "uuid", defaultValue = "0") String uuid,
            @RequestHeader Map<String, String> headers,
            HttpServletResponse response
    ) {
        try {
            HashMap<String, Object> parametros = new HashMap<>();
            InputStream logoStream = getClass().getResourceAsStream("/imagenes/gamq1.jpg");
            parametros.put("logoPath", logoStream);
            parametros.put("uuid", uuid);
            parametros.put("detalle_tarifario", buildDetalleTarifario(uuid));
            parametros.put(JRParameter.REPORT_LOCALE, new Locale("es", "ES"));
            generadorReporte.generarSqlReportePdf(
                    "ticket",
                    "classpath:reportes/ticketPuntualperson.jrxml",
                    parametros,
                    response
            );
            registrarReporteGenerado(headers, "TICKET_RESERVA_PUNTUAL_PERSONA", "reserva", uuid,
                    "Generó ticket de reserva puntual por persona", "{\"uuid\":\"" + safeValue(uuid) + "\"}");
        } catch (SQLException ex) {
            ex.printStackTrace();
            sendErrorResponse(response, 500, "Error SQL al generar ticketPuntualperson.");
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(response, 500, "Error inesperado al generar ticketPuntualperson.");
        }
    }

    ///////////////reserva puntual por persona/////////////////
    ///////////////reserva puntual por evento/////////////////
    @Operation(summary = "Reporte de ticket de Reserva Puntual por Evento")
    @RequestMapping(value="/ticket-reser-ev",method= RequestMethod.GET)
    @ResponseBody
    @PreAuthorize("@permissions.ticket('puntual',#p0)")
    public void reporteTicketReservaEvento (
            @RequestParam(name = "uuid", defaultValue = "0") String uuid,
            @RequestHeader Map<String, String> headers,
            HttpServletResponse response
    ) {
        try {
            //String usuario = headers.getOrDefault("usuario", "Default");
            HashMap<String, Object> parametros = new HashMap<String,Object>();
            InputStream logoStream = getClass().getResourceAsStream("/imagenes/gamq1.jpg");
            parametros.put("logoPath", logoStream);
            parametros.put("uuid", uuid);
            parametros.put("detalle_tarifario", buildDetalleTarifario(uuid));
            parametros.put(JRParameter.REPORT_LOCALE, new Locale("es", "ES"));
            try {
                generadorReporte.generarSqlReportePdf(
                        "ticket",
                        "classpath:reportes/ticketPuntualEven.jrxml",
                        parametros,
                        response
                );
                registrarReporteGenerado(headers, "TICKET_RESERVA_PUNTUAL_EVENTO", "reserva", uuid,
                        "Generó ticket de reserva puntual por evento", "{\"uuid\":\"" + safeValue(uuid) + "\"}");
            } catch (SQLException ex) {
                response.setStatus(500);
                throw new RuntimeException(ex);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(e.getLocalizedMessage());
            sendErrorResponse(response, 500, "Error inesperado al generar ticket de reserva puntual por evento.");
        }
    }

    ///////////////reserva puntual por persona/////////////////
    @Operation(summary = "Reporte Lista de reservas por intervalo de fechas y tipo de cobrom, estado, cancha ")
    @RequestMapping(value="/lista-Reservas",method= RequestMethod.GET)
    @ResponseBody
    @PreAuthorize("@permissions.has('/reports/list/puntual','GENERAR')")
    public void reporteListaReserva (
            @RequestParam(name = "fechaini",required = false) @DateTimeFormat(pattern="dd/MM/yyyy") Date fecha_ini,
            @RequestParam(name = "fechafin",required = false) @DateTimeFormat(pattern="dd/MM/yyyy") Date fecha_fin,
            @RequestParam(name = "tipocobro",required = false) String tipocobro,
            @RequestParam(name = "estado",required = false) String estado,
            @RequestParam(name = "cancha",required = false) String cancha,
            @RequestHeader Map<String, String> headers,
            HttpServletResponse response
    ) {
        try {
            //String usuario = headers.getOrDefault("usuario", "Default");
            tipocobro = normalizeFilter(tipocobro);
            estado = normalizeFilter(estado);
            cancha = normalizeFilter(cancha);

            HashMap<String, Object> parametros = new HashMap<String,Object>();
            parametros.put("fechaini",fecha_ini);
            parametros.put("fechafin",fecha_fin);
            parametros.put("tipocobro",tipocobro);
            parametros.put("estado",estado);
            parametros.put("cancha",cancha);
            //parametros.put("logo64","iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk");

            parametros.put(JRParameter.REPORT_LOCALE, new Locale("es", "ES"));
            try {
                generadorReporte.generarSqlReportePdf(
                        "Reserva",
                        "classpath:reportes/Listado.jrxml",
                        parametros,
                        response
                );
                registrarReporteGenerado(headers, "REPORTE_LISTA_RESERVAS", "reporte", null,
                        "Generó listado de reservas", buildReportFilters(parametros));
            } catch (SQLException ex) {
                response.setStatus(500);
                throw new RuntimeException(ex);

            }
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(response, 500, "Error inesperado al generar listado de reservas.");
        }
    }
    ////////////////////////Listado Recurrentes//////////////////////////////
    @Operation(summary = "Reporte Lista de reservas Recurrentes por intervalo de fechas y tipo de cobrom, estado, cancha ")
    @RequestMapping(value="/lista-Recurrentes",method= RequestMethod.GET)
    @ResponseBody
    @PreAuthorize("@permissions.has('/reports/list/recurrente','GENERAR')")
    public void TicketReservaRecuPorSesion (
            @RequestParam(name = "fechaini",required = false) @DateTimeFormat(pattern="dd/MM/yyyy") Date fecha_ini,
            @RequestParam(name = "fechafin",required = false) @DateTimeFormat(pattern="dd/MM/yyyy") Date fecha_fin,
            @RequestParam(name = "tipocobro",required = false) String tipocobro,
            @RequestParam(name = "estado",required = false) String estado,
            @RequestParam(name = "cancha",required = false) String cancha,
            @RequestHeader Map<String, String> headers,
            HttpServletResponse response
    ) {
        try {
            //usuario
            //String usuario = headers.getOrDefault("usuario", "Default");
            tipocobro = normalizeFilter(tipocobro);
            estado = normalizeFilter(estado);
            cancha = normalizeFilter(cancha);

            HashMap<String, Object> parametros = new HashMap<String,Object>();
            parametros.put("fechaini",fecha_ini);
            parametros.put("fechafin",fecha_fin);
            parametros.put("tipocobro",tipocobro);
            parametros.put("estado",estado);
            parametros.put("cancha",cancha);
            parametros.put(JRParameter.REPORT_LOCALE, new Locale("es", "ES"));
            try {
                generadorReporte.generarSqlReportePdf(
                        "ReservaRecuerrente",
                        "classpath:reportes/ListadoRecu.jrxml",
                        parametros,
                        response
                );
                registrarReporteGenerado(headers, "REPORTE_LISTA_RECURRENTES", "reporte", null,
                        "Generó listado de reservas recurrentes", buildReportFilters(parametros));
            } catch (SQLException ex) {
                response.setStatus(500);
                throw new RuntimeException(ex);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(response, 500, "Error inesperado al generar listado de reservas recurrentes.");
        }
    }

    @Operation(summary = "Reporte de reserva Recurrente por Sesión")
    @RequestMapping(value="/ticket-recuses", method = RequestMethod.GET)
    @ResponseBody
    @PreAuthorize("@permissions.ticket('recurrente',#p0)")
    public void ticketRecuSesion(
            @RequestParam(name = "uuid", defaultValue = "0") String uuid,
            @RequestHeader Map<String, String> headers,
            HttpServletResponse response
    ) {
        generarTicketRecurrente(uuid, "classpath:reportes/ticketRecu.jrxml", "TICKET_RECURRENTE_SESION", headers, response);
    }
    @Operation(summary = "Reporte de reserva Recurrente por Mensualidad")
    @RequestMapping(value="/ticket-recumes", method = RequestMethod.GET)
    @ResponseBody
    @PreAuthorize("@permissions.ticket('recurrente',#p0)")
    public void ticketRecuMes(
            @RequestParam(name = "uuid", defaultValue = "0") String uuid,
            @RequestHeader Map<String, String> headers,
            HttpServletResponse response
    ) {
        generarTicketRecurrente(uuid, "classpath:reportes/ticketRecumen.jrxml", "TICKET_RECURRENTE_MENSUALIDAD", headers, response);
    }
    @Operation(summary = "Reporte de reserva Recurrente por Persona")
    @RequestMapping(value="/ticket-recuper", method = RequestMethod.GET)
    @ResponseBody
    @PreAuthorize("@permissions.ticket('recurrente',#p0)")
    public void ticketRecuPer(
            @RequestParam(name = "uuid", defaultValue = "0") String uuid,
            @RequestHeader Map<String, String> headers,
            HttpServletResponse response
    ) {
        generarTicketRecurrente(uuid, "classpath:reportes/ticketRecuPer.jrxml", "TICKET_RECURRENTE_PERSONA", headers, response);
    }
    private void generarTicketRecurrente(
            String uuid,
            String rutaReporte,
            String accion,
            Map<String, String> headers,
            HttpServletResponse response
    ) {
        try {
            ReservaRecurrenteDto reserva = buscarReservaRecurrente(uuid);
            String horarioFormateado;
            Integer cantidadMeses = null;
            if (reserva == null) {
                horarioFormateado = "No se encontró la reserva recurrente para el UUID: " + uuid;
            } else {
                if (reserva.getTableroDto() == null) {
                    horarioFormateado = "La reserva no tiene tablero de horarios configurado.";
                } else if (reserva.getTableroDto().getHorario() == null) {
                    horarioFormateado = "La reserva existe pero no tiene horarios configurados.";
                } else {
                    horarioFormateado = formatHorario3DForReport(reserva);
                }
                cantidadMeses = calcularCantidadMeses(reserva.getFechaInicio(), reserva.getFechaFin());
            }
            HashMap<String, Object> parametros = new HashMap<>();
            InputStream logoStream = getClass().getResourceAsStream("/imagenes/gamq1.jpg");
            parametros.put("logoPath", logoStream);
            parametros.put("uuid", uuid);
            parametros.put("horario_formateado", horarioFormateado);
            parametros.put("cantidad_meses", cantidadMeses);
            parametros.put("detalle_tarifario", buildDetalleTarifario(uuid));
            parametros.put(JRParameter.REPORT_LOCALE, new Locale("es", "ES"));
            generadorReporte.generarSqlReportePdf(
                    "Recurrente",
                    rutaReporte,
                    parametros,
                    response
            );
            registrarReporteGenerado(headers, accion, "reserva", uuid,
                    "Generó ticket de reserva recurrente", "{\"uuid\":\"" + safeValue(uuid) + "\",\"rutaReporte\":\"" + safeValue(rutaReporte) + "\"}");
        } catch (SQLException e) {
            sendErrorResponse(response, 500, "Error SQL al generar el reporte.");
        } catch (Exception e) {
            sendErrorResponse(response, 500, "Error inesperado al generar el reporte.");
        }
    }

    private Integer calcularCantidadMeses(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) return null;
        if (fechaFin.isBefore(fechaInicio)) return 0;
        long meses = java.time.temporal.ChronoUnit.MONTHS.between(fechaInicio, fechaFin);
        if (fechaInicio.plusMonths(meses).isBefore(fechaFin)) {
            meses++;
        }
        return (int) Math.max(meses, 1);
    }
    private ReservaRecurrenteDto buscarReservaRecurrente(String uuid) {
        try {
            return reservaRecurrenteService.findByReservaUuid(uuid);
        } catch (NotFoundException e) {
            try {
                return reservaRecurrenteService.findByUuid(uuid);
            } catch (NotFoundException ex) {
                return null;
            }
        }
    }
    private String formatHorario3DForReport(ReservaRecurrenteDto reserva) {

        if (reserva.getTableroDto() == null ||
                reserva.getTableroDto().getHorario() == null) {
            return "No hay horarios registrados.";
        }

        String[][][] horario3D = reserva.getTableroDto().getHorario();

        LocalDate startDate = reserva.getFechaInicio();
        LocalDate endDate = reserva.getFechaFin();

        if (startDate == null) {
            return "No hay fecha de inicio definida.";
        }

        if (endDate == null) {
            endDate = startDate.plusMonths(3);
        }

        StringBuilder sb = new StringBuilder();

        boolean hasAnySchedule = false;
        int totalSessions = 0;

        String[] dias = {
                "Lun",
                "Mar",
                "Mié",
                "Jue",
                "Vie",
                "Sáb",
                "Dom"
        };

        for (int monthIdx = 0; monthIdx < horario3D.length; monthIdx++) {

            String[][] daysMatrix = horario3D[monthIdx];

            if (daysMatrix == null || daysMatrix.length == 0) {
                continue;
            }

            LocalDate monthStart = startDate.plusMonths(monthIdx);

            int year = monthStart.getYear();
            int month = monthStart.getMonthValue();

            @SuppressWarnings("unchecked")
            List<String>[] grouped = new ArrayList[7];

            for (int i = 0; i < 7; i++) {
                grouped[i] = new ArrayList<>();
            }

            int monthSessions = 0;

            for (int dayIdx = 0; dayIdx < daysMatrix.length; dayIdx++) {

                String[] daySlots = daysMatrix[dayIdx];

                if (daySlots == null) {
                    continue;
                }

                int dayOfMonth = dayIdx + 1;

                try {

                    LocalDate currentDate =
                            LocalDate.of(year, month, dayOfMonth);

                    if (currentDate.isBefore(startDate) ||
                            currentDate.isAfter(endDate)) {
                        continue;
                    }

                    int dowIndex =
                            currentDate.getDayOfWeek().getValue() - 1;

                    for (String slot : daySlots) {

                        if (slot == null || slot.trim().isEmpty()) {
                            continue;
                        }

                        String cleanSlot = slot.trim();

                        if (cleanSlot.contains("-")
                                && cleanSlot.indexOf("-")
                                != cleanSlot.lastIndexOf("-")) {

                            cleanSlot =
                                    cleanSlot.substring(
                                            0,
                                            cleanSlot.lastIndexOf("-")
                                    );
                        }

                        String formattedTime;

                        if (cleanSlot.matches(".*\\d{2}:\\d{2}.*-.*\\d{2}:\\d{2}.*")) {

                            formattedTime =
                                    cleanSlot.replaceAll("\\s*-\\s*", " - ");

                        } else if (cleanSlot.matches("\\d{2}:\\d{2}")) {

                            formattedTime =
                                    cleanSlot + " - " + cleanSlot;

                        } else {
                            continue;
                        }

                        grouped[dowIndex].add(
                                String.format(
                                        "%02d/%02d %s",
                                        dayOfMonth,
                                        month,
                                        formattedTime
                                )
                        );

                        hasAnySchedule = true;
                        monthSessions++;
                        totalSessions++;
                    }

                } catch (Exception ignored) {
                }
            }

            if (monthSessions > 0) {

                sb.append("\n");
                sb.append(getMonthName(month))
                        .append(" ")
                        .append(year)
                        .append("\n");

                for (int i = 0; i < 7; i++) {

                    if (!grouped[i].isEmpty()) {

                        sb.append(dias[i])
                                .append(": ")
                                .append(formatGroupedLinePlain(grouped[i], 3))
                                .append("\n");
                    }
                }

                sb.append("\n");
            }
        }

        if (!hasAnySchedule) {
            return "No hay horarios programados.";
        }

        int sesiones =
                reserva.getCantidadSesiones() != null
                        ? reserva.getCantidadSesiones()
                        : totalSessions;

        int duracion =
                reserva.getDuracionSesionHoras() != null
                        ? reserva.getDuracionSesionHoras()
                        : 1;

        int totalHoras = sesiones * duracion;

        sb.append("\n");
        sb.append("Resumen:\n");
        sb.append(sesiones)
                .append(" sesiones | ");
        sb.append(duracion)
                .append(" hora(s) por sesión | ");
        sb.append(totalHoras)
                .append(" hora(s) totales\n");

        sb.append("Días programados: ")
                .append(formatDiasSemana(reserva.getDiasSemana()));

        return sb.toString();
    }
    private String formatGroupedLinePlain(List<String> items, int chunkSize) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                if (i % chunkSize == 0) {
                    out.append("\n");
                } else {
                    out.append(" · ");
                }
            }
            out.append(items.get(i));
        }
        return out.toString();
    }
    private String formatGroupedLine(java.util.List<String> items, int chunkSize) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                if (i % chunkSize == 0) {
                    out.append("<br/>");
                } else {
                    out.append(" &nbsp;·&nbsp; ");
                }
            }
            out.append(items.get(i));
        }
        return out.toString();
    }
    private String getMonthName(int month) {
        String[] months = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        return months[month - 1];
    }

    private String formatDiasSemana(String diasSemana) {
        if (diasSemana == null || diasSemana.isEmpty()) return "No especificados";
        String[] dias = diasSemana.split(",");
        java.util.Map<String, String> mapDias = new java.util.HashMap<>();
        mapDias.put("LUNES", "Lunes");
        mapDias.put("MARTES", "Martes");
        mapDias.put("MIERCOLES", "Miércoles");
        mapDias.put("JUEVES", "Jueves");
        mapDias.put("VIERNES", "Viernes");
        mapDias.put("SABADO", "Sábado");
        mapDias.put("DOMINGO", "Domingo");

        String result = java.util.Arrays.stream(dias)
                .map(d -> mapDias.getOrDefault(d.trim().toUpperCase(), d.trim()))
                .collect(java.util.stream.Collectors.joining(", "));

        return result;
    }
    private void sendErrorResponse(HttpServletResponse response, int status, String message) {
        try {
            if (response.isCommitted()) {
                return;
            }
            response.setStatus(status);
            response.setContentType("text/plain");
            response.getWriter().write(message);
        } catch (IOException e) {
            System.err.println("Failed to send error response: " + e.getMessage());
        }
    }

    private String normalizeFilter(String value) {
        if (value == null || value.trim().isEmpty() || "TODO".equalsIgnoreCase(value.trim())) {
            return "%";
        }
        return "%" + value.trim() + "%";
    }

    ////////////////////////RECURRENTES POR MENSUALIDAD//////////////////////////////
    @Operation(summary = "Reporte Lista de Pagos en un intervalo de fechas")
    @RequestMapping(value="/lista-Pagos",method= RequestMethod.GET)
    @ResponseBody
    @PreAuthorize("@permissions.has('/reports/payments','GENERAR')")
    public void reporteListaPagos (
            @RequestParam(name = "fecha_ini",required = false) @DateTimeFormat(pattern="dd/MM/yyyy") Date fecha_ini,
            @RequestParam(name = "fecha_fin",required = false) @DateTimeFormat(pattern="dd/MM/yyyy") Date fecha_fin,
            @RequestHeader Map<String, String> headers,
            HttpServletResponse response
    ) {
        try {
            //usuario
            //String usuario = headers.getOrDefault("usuario", "Default");
            HashMap<String, Object> parametros = new HashMap<String,Object>();
            parametros.put("fecha_ini",fecha_ini);
            parametros.put("fecha_fin",fecha_fin);

            //parametros.put("logo64","iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk");

            parametros.put(JRParameter.REPORT_LOCALE, new Locale("es", "ES"));
            try {
                generadorReporte.generarSqlReportePdf(
                        "Pagos",
                        "classpath:reportes/listpago.jrxml",
                        parametros,
                        response
                );
                registrarReporteGenerado(headers, "REPORTE_LISTA_PAGOS", "reporte", null,
                        "Generó listado de pagos", buildReportFilters(parametros));
            } catch (SQLException ex) {
                response.setStatus(500);
                throw new RuntimeException(ex);

            }
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(response, 500, "Error inesperado al generar listado de pagos.");
        }
    }
/////////////////////Listado Ticket Mensual ///////////////////////////////////////////////////
@Operation(summary = "Reporte lista de reserva Mensual")
@RequestMapping(value="/lista-mensuales",method= RequestMethod.GET)
@ResponseBody
@PreAuthorize("@permissions.has('/reports/list/mensual','GENERAR')")
public void reporteListaMensuales (
        @RequestParam(name = "fechaini",required = false) @DateTimeFormat(pattern="dd/MM/yyyy") Date fecha_ini,
        @RequestParam(name = "fechafin",required = false) @DateTimeFormat(pattern="dd/MM/yyyy") Date fecha_fin,
        @RequestParam(name = "estado",required = false) String estado,
        @RequestParam(name = "escuelita",required = false) String escuelita,
        @RequestHeader Map<String, String> headers,
        HttpServletResponse response
) {
    try {
        //usuario
        //String usuario = headers.getOrDefault("usuario", "Default");
        //ingreso almacen

        estado = normalizeFilter(estado);
        escuelita = normalizeFilter(escuelita);
        HashMap<String, Object> parametros = new HashMap<String,Object>();
        parametros.put("fechaini",fecha_ini);
        parametros.put("fechafin",fecha_fin);
        parametros.put("estado",estado);
        parametros.put("escuelita",escuelita);

        //parametros.put(JRParameter.REPORT_LOCALE, new Locale("es", "ES"));
        //parametros.put("logo64","iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk");

        parametros.put(JRParameter.REPORT_LOCALE, new Locale("es", "ES"));
        try {
            generadorReporte.generarSqlReportePdf(
                    "Reservasmensuales",
                    "classpath:reportes/ListadoMensual.jrxml",
                    parametros,
                    response
            );
            registrarReporteGenerado(headers, "REPORTE_LISTA_MENSUALES", "reporte", null,
                    "Generó listado de reservas mensuales", buildReportFilters(parametros));
        } catch (SQLException ex) {
            response.setStatus(500);
            throw new RuntimeException(ex);

        }
    } catch (Exception e) {
        e.printStackTrace();
        sendErrorResponse(response, 500, "Error inesperado al generar listado de reservas mensuales.");
    }
}
////////////////////listado por alumno//////////////////////////////////
@Operation(summary = "Reporte Lista de pagos de Escuelas del GAMQ pór matrciula o alumno")

@RequestMapping(value="/lista-alumno",method= RequestMethod.GET)
@ResponseBody
@PreAuthorize("@permissions.has('/reports/list/mensual','GENERAR')")
public void reporteListaMensAlumno (
        @RequestParam(name = "fechaini",required = false) @DateTimeFormat(pattern="dd/MM/yyyy") Date fecha_ini,
        @RequestParam(name = "fechafin",required = false) @DateTimeFormat(pattern="dd/MM/yyyy") Date fecha_fin,
        @RequestParam(name = "estado",required = false) String estado,
        @RequestParam(name = "matricula",required = false) String matricula,
        @RequestParam(name = "alumno",required = false) String alumno,
        @RequestHeader Map<String, String> headers,
        HttpServletResponse response
) {
    try {
        //usuario
        //String usuario = headers.getOrDefault("usuario", "Default");
        estado = normalizeFilter(estado);
        matricula = normalizeFilter(matricula);
        alumno = normalizeFilter(alumno);
        HashMap<String, Object> parametros = new HashMap<String,Object>();
        parametros.put("fechaini",fecha_ini);
        parametros.put("fechafin",fecha_fin);
        parametros.put("estado",estado);
        parametros.put("matricula",matricula);
        parametros.put("alumno",alumno);

        //parametros.put(JRParameter.REPORT_LOCALE, new Locale("es", "ES"));
        //parametros.put("logo64","iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk");

        parametros.put(JRParameter.REPORT_LOCALE, new Locale("es", "ES"));
        try {
            generadorReporte.generarSqlReportePdf(
                    "Reservasmensuales",
                    "classpath:reportes/LisMenAlumno.jrxml",
                    parametros,
                    response
            );
            registrarReporteGenerado(headers, "REPORTE_LISTA_MENSUALIDADES_ALUMNO", "reporte", null,
                    "Generó listado de pagos de escuelas por alumno", buildReportFilters(parametros));
        } catch (SQLException ex) {
            response.setStatus(500);
            throw new RuntimeException(ex);

        }
    } catch (Exception e) {
        e.printStackTrace();
        sendErrorResponse(response, 500, "Error inesperado al generar listado por alumno.");
    }
}
////////////////////Ticket Mensual///////////////////////////////////////
    @Operation(summary = "Reporte de ticket de Reserva Mensual (Escuela)")
    @RequestMapping(value="/ticket-mensual", method = RequestMethod.GET)
    @ResponseBody
    @PreAuthorize("@permissions.ticket('periodo',#p0)")
    public void ticketMensual(
            @RequestParam(name = "uuid", defaultValue = "0") String uuid,
            @RequestHeader Map<String, String> headers,
            HttpServletResponse response
    ) {
        try {
            //usuario
            //String usuario = headers.getOrDefault("usuario", "Default");
            HashMap<String, Object> parametros = new HashMap<>();
            InputStream logoStream = getClass().getResourceAsStream("/imagenes/gamq1.jpg");
            parametros.put("logoPath", logoStream);
            parametros.put("uuid", uuid);
            parametros.put("detalle_tarifario", buildDetalleTarifario(uuid));
            parametros.put(JRParameter.REPORT_LOCALE, new Locale("es", "ES"));
            try {
                generadorReporte.generarSqlReportePdf(
                        "ticketmensual",
                        "classpath:reportes/ticketmensual.jrxml",
                        parametros,
                        response
                );
                registrarReporteGenerado(headers, "TICKET_MENSUAL_ESCUELA", "reserva", uuid,
                        "Generó ticket de mensualidad de escuela", "{\"uuid\":\"" + safeValue(uuid) + "\"}");
            } catch (SQLException ex) {
                response.setStatus(500);
                throw new RuntimeException(ex);

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(e.getLocalizedMessage());
            sendErrorResponse(response, 500, "Error inesperado al generar ticket mensual.");
        }
    }

    private void registrarReporteGenerado(Map<String, String> headers, String accion, String entidadTipo,
                                          String entidadUuid, String descripcion, String datosCambio) {
        try {
            sistemaAuditoriaService.registrarAccion(
                    getHeader(headers, "usuario_uuid", "usuarioUuid", "user_uuid", "userUuid", "usuario"),
                    accion,
                    "REPORTES",
                    entidadTipo,
                    entidadUuid,
                    descripcion,
                    datosCambio,
                    getHeader(headers, "x-forwarded-for", "X-Forwarded-For", "x-real-ip", "X-Real-IP"),
                    getHeader(headers, "user-agent", "User-Agent")
            );
        } catch (RuntimeException exception) {
            // El PDF ya fue generado. Un fallo de auditoría no debe invalidar la descarga.
            System.err.println("No se pudo registrar la auditoría del reporte: " + exception.getMessage());
        }
    }

    private String buildDetalleTarifario(String reservaUuid) {
        List<ReservaTarifaDetalle> detalles = reservaTarifaDetalleRepository.findByReservaUuid(reservaUuid);
        if (detalles == null || detalles.isEmpty()) {
            return "Detalle tarifario no registrado. Verificar monto total de la reserva.";
        }
        StringBuilder builder = new StringBuilder();
        for (ReservaTarifaDetalle detalle : detalles) {
            if (detalle == null) {
                continue;
            }
            String concepto = detalle.getNombreConcepto();
            if (concepto == null || concepto.trim().isEmpty()) {
                concepto = "BASE".equalsIgnoreCase(detalle.getTipoLinea()) ? "Tarifa base" : "Concepto tarifario";
            }
            builder.append(concepto);
            if (detalle.getTipoCobro() != null && !detalle.getTipoCobro().trim().isEmpty()) {
                builder.append(" (").append(detalle.getTipoCobro()).append(")");
            }
            JsonNode snapshot = parseSnapshot(detalle.getDatosSnapshot());
            BigDecimal precioUnitario = getSnapshotDecimal(snapshot, "precioUnitarioOriginal", detalle.getPrecioUnitario());
            BigDecimal subtotal = getSnapshotDecimal(snapshot, "subtotalOriginal", detalle.getSubtotal());
            boolean exento = isGratuidad(detalle, snapshot);
            String motivoGratuidad = getSnapshotText(snapshot, "motivoGratuidad");

            builder.append(": ");
            builder.append(formatMoney(detalle.getCantidad())).append(" x ");
            builder.append(formatMoney(precioUnitario)).append(" Bs.");
            builder.append(" = ").append(formatMoney(subtotal)).append(" Bs.");
            if (exento) {
                builder.append(" -> exento por gratuidad");
                if (motivoGratuidad != null && !motivoGratuidad.trim().isEmpty()) {
                    builder.append(" (").append(motivoGratuidad).append(")");
                }
            } else if (detalle.getReglaAplicada() != null && !detalle.getReglaAplicada().trim().isEmpty()) {
                builder.append(" - ").append(detalle.getReglaAplicada());
            }
            builder.append("\n");
        }
        return builder.toString().trim();
    }

    private JsonNode parseSnapshot(String datosSnapshot) {
        if (datosSnapshot == null || datosSnapshot.trim().isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readTree(datosSnapshot);
        } catch (Exception ignored) {
            return null;
        }
    }

    private BigDecimal getSnapshotDecimal(JsonNode snapshot, String field, BigDecimal fallback) {
        if (snapshot != null && snapshot.has(field) && snapshot.get(field).isNumber()) {
            return snapshot.get(field).decimalValue();
        }
        return fallback;
    }

    private String getSnapshotText(JsonNode snapshot, String field) {
        if (snapshot != null && snapshot.has(field) && !snapshot.get(field).isNull()) {
            return snapshot.get(field).asText();
        }
        return null;
    }

    private boolean isGratuidad(ReservaTarifaDetalle detalle, JsonNode snapshot) {
        if (detalle.getReglaAplicada() != null && detalle.getReglaAplicada().startsWith("GRATUIDAD")) {
            return true;
        }
        return snapshot != null && snapshot.has("esReservaGratuita") && snapshot.get("esReservaGratuita").asBoolean(false);
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return String.format(Locale.US, "%.2f", value);
    }

    private String buildReportFilters(Map<String, Object> parametros) {
        StringBuilder builder = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : parametros.entrySet()) {
            if ("logoPath".equals(entry.getKey()) || JRParameter.REPORT_LOCALE.equals(entry.getKey())) {
                continue;
            }
            if (!first) {
                builder.append(",");
            }
            builder.append("\"").append(safeValue(entry.getKey())).append("\":\"")
                    .append(safeValue(entry.getValue())).append("\"");
            first = false;
        }
        builder.append("}");
        return builder.toString();
    }

    private String getHeader(Map<String, String> headers, String... names) {
        if (headers == null) {
            return null;
        }
        for (String name : names) {
            if (headers.containsKey(name)) {
                return headers.get(name);
            }
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            for (String name : names) {
                if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    private String safeValue(Object value) {
        return value == null ? "" : String.valueOf(value).replace("\"", "\\\"");
    }
}
