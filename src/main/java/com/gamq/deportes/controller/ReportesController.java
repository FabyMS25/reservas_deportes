package com.gamq.deportes.controller;

import com.gamq.deportes.dto.ReservaRecurrenteDto;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.service.ReservaRecurrenteService;
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

@Controller
@RequestMapping("/api/reporte")
@Tag(name = "Reportes", description = "API para Reportes imprimibles del Sistema")
public class ReportesController {
    @Autowired
    private GeneradorReporte generadorReporte;

     @Autowired
     private ReservaRecurrenteService reservaRecurrenteService;
    ///////////////reserva puntual por hora/////////////////
    @Operation(summary = "Reporte de ticket de Reserva Puntual")
    @RequestMapping(value="/ticket-reservap",method= RequestMethod.GET)
    @ResponseBody
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
            parametros.put(JRParameter.REPORT_LOCALE, new Locale("es", "ES"));
            try {
                generadorReporte.generarSqlReportePdf(
                        "ticket",
                        "classpath:reportes/ticketPuntual.jrxml",
                        parametros,
                        response
                );
            } catch (SQLException ex) {
                response.setStatus(500);
                throw new RuntimeException(ex);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(e.getLocalizedMessage());
        }
    }
    ///////////////reserva puntual por persona/////////////////
    @Operation(summary = "Reporte de ticket de Reserva Puntual por Persona")
    @RequestMapping(value="/ticket-reser-per",method= RequestMethod.GET)
    @ResponseBody
    public void reporteTicketReservePersona(
            @RequestParam(name = "uuid", defaultValue = "0") String uuid,
            HttpServletResponse response
    ) {
        try {
            HashMap<String, Object> parametros = new HashMap<>();
            InputStream logoStream = getClass().getResourceAsStream("/imagenes/gamq1.jpg");
            parametros.put("logoPath", logoStream);
            parametros.put("uuid", uuid);
            parametros.put(JRParameter.REPORT_LOCALE, new Locale("es", "ES"));
            generadorReporte.generarSqlReportePdf(
                    "ticket",
                    "classpath:reportes/ticketPuntualperson.jrxml",
                    parametros,
                    response
            );
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
            parametros.put(JRParameter.REPORT_LOCALE, new Locale("es", "ES"));
            try {
                generadorReporte.generarSqlReportePdf(
                        "ticket",
                        "classpath:reportes/ticketPuntualEven.jrxml",
                        parametros,
                        response
                );
            } catch (SQLException ex) {
                response.setStatus(500);
                throw new RuntimeException(ex);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(e.getLocalizedMessage());
        }
    }

    ///////////////reserva puntual por persona/////////////////
    @Operation(summary = "Reporte Lista de reservas por intervalo de fechas y tipo de cobrom, estado, cancha ")
    @RequestMapping(value="/lista-Reservas",method= RequestMethod.GET)
    @ResponseBody
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
            if (tipocobro.equals("TODO")){
                tipocobro="%";
            }else
            {
                tipocobro="%"+tipocobro+"%";
            }
            if (estado.equals("TODO")){
                estado="%";
            }else
            {
                estado="%"+estado+"%";
            }
            if (cancha.equals("TODO")){
                cancha="%";
            }else
            {
                cancha="%"+cancha+"%";
            }

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
            } catch (SQLException ex) {
                response.setStatus(500);
                throw new RuntimeException(ex);

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(e.getLocalizedMessage());
        }
    }
    ////////////////////////Listado Recurrentes//////////////////////////////
    @Operation(summary = "Reporte Lista de reservas Recurrentes por intervalo de fechas y tipo de cobrom, estado, cancha ")
    @RequestMapping(value="/lista-Recurrentes",method= RequestMethod.GET)
    @ResponseBody
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
            if (tipocobro.equals("TODO")){
                tipocobro="%";
            }else
            {
                tipocobro="%"+tipocobro+"%";
            }
            if (estado.equals("TODO")){
                estado="%";
            }else
            {
                estado="%"+estado+"%";
            }
            if (cancha.equals("TODO")){
                cancha="%";
            }else
            {
                cancha="%"+cancha+"%";
            }

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
            } catch (SQLException ex) {
                response.setStatus(500);
                throw new RuntimeException(ex);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(e.getLocalizedMessage());
        }
    }

    @Operation(summary = "Reporte de reserva Recurrente por Sesión")
    @RequestMapping(value="/ticket-recuses", method = RequestMethod.GET)
    @ResponseBody
    public void ticketRecuSesion(
            @RequestParam(name = "uuid", defaultValue = "0") String uuid,
            HttpServletResponse response
    ) {
        generarTicketRecurrente(uuid, "classpath:reportes/ticketRecu.jrxml", response);
    }
    @Operation(summary = "Reporte de reserva Recurrente por Mensualidad")
    @RequestMapping(value="/ticket-recumes", method = RequestMethod.GET)
    @ResponseBody
    public void ticketRecuMes(
            @RequestParam(name = "uuid", defaultValue = "0") String uuid,
            HttpServletResponse response
    ) {
        generarTicketRecurrente(uuid, "classpath:reportes/ticketRecumen.jrxml", response);
    }
    @Operation(summary = "Reporte de reserva Recurrente por Persona")
    @RequestMapping(value="/ticket-recuper", method = RequestMethod.GET)
    @ResponseBody
    public void ticketRecuPer(
            @RequestParam(name = "uuid", defaultValue = "0") String uuid,
            HttpServletResponse response
    ) {
        generarTicketRecurrente(uuid, "classpath:reportes/ticketRecuPer.jrxml", response);
    }
    private void generarTicketRecurrente(
            String uuid,
            String rutaReporte,
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
            parametros.put(JRParameter.REPORT_LOCALE, new Locale("es", "ES"));
            generadorReporte.generarSqlReportePdf(
                    "Recurrente",
                    rutaReporte,
                    parametros,
                    response
            );
        } catch (SQLException e) {
            sendErrorResponse(response, 500, "Error SQL al generar el reporte.");
        } catch (Exception e) {
            sendErrorResponse(response, 500, "Error inesperado al generar el reporte.");
        }
    }
    private Integer calcularCantidadMeses(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            return null;
        }

        if (fechaFin.isBefore(fechaInicio)) {
            return 0;
        }

        java.time.YearMonth inicio = java.time.YearMonth.from(fechaInicio);
        java.time.YearMonth fin = java.time.YearMonth.from(fechaFin);

        long meses = java.time.temporal.ChronoUnit.MONTHS.between(inicio, fin) + 1;

        return (int) meses;
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
            response.setStatus(status);
            response.setContentType("text/plain");
            response.getWriter().write(message);
        } catch (IOException e) {
            System.err.println("Failed to send error response: " + e.getMessage());
        }
    }

    ////////////////////////RECURRENTES POR MENSUALIDAD//////////////////////////////
    @Operation(summary = "Reporte Lista de Pagos en un intervalo de fechas")
    @RequestMapping(value="/lista-Pagos",method= RequestMethod.GET)
    @ResponseBody
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
            } catch (SQLException ex) {
                response.setStatus(500);
                throw new RuntimeException(ex);

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(e.getLocalizedMessage());
        }
    }
/////////////////////Listado Ticket Mensual ///////////////////////////////////////////////////
@Operation(summary = "Reporte lista de reserva Mensual")
@RequestMapping(value="/lista-mensuales",method= RequestMethod.GET)
@ResponseBody
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

        if (estado.equals("TODO")){
            estado="%";
        }else
        {
            estado="%"+estado+"%";
        }
        if (escuelita.equals("TODO")){
            escuelita="%";
        }else
        {
            escuelita="%"+escuelita+"%";
        }
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
        } catch (SQLException ex) {
            response.setStatus(500);
            throw new RuntimeException(ex);

        }
    } catch (Exception e) {
        System.out.println(e.getMessage());
        System.out.println(e.getLocalizedMessage());
    }
}
////////////////////listado por alumno//////////////////////////////////
@Operation(summary = "Reporte Lista de pagos de Escuelas del GAMQ pór matrciula o alumno")

@RequestMapping(value="/lista-alumno",method= RequestMethod.GET)
@ResponseBody
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
        if (estado.equals("TODO")){
            estado="%";
        }else
        {
            estado="%"+estado+"%";
        }
        if (matricula.equals("TODO")){
            matricula="%";
        }else
        {
            matricula="%"+matricula+"%";
        }
        if (alumno.equals("TODO")){
            alumno="%";
        }else
        {
            alumno="%"+alumno+"%";
        }
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
        } catch (SQLException ex) {
            response.setStatus(500);
            throw new RuntimeException(ex);

        }
    } catch (Exception e) {
        System.out.println(e.getMessage());
        System.out.println(e.getLocalizedMessage());
    }
}
////////////////////Ticket Mensual///////////////////////////////////////
    @Operation(summary = "Reporte de ticket de Reserva Mensual (Escuela)")
    @RequestMapping(value="/ticket-mensual", method = RequestMethod.GET)
    @ResponseBody
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
            parametros.put(JRParameter.REPORT_LOCALE, new Locale("es", "ES"));
            try {
                generadorReporte.generarSqlReportePdf(
                        "ticketmensual",
                        "classpath:reportes/ticketmensual.jrxml",
                        parametros,
                        response
                );
            } catch (SQLException ex) {
                response.setStatus(500);
                throw new RuntimeException(ex);

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(e.getLocalizedMessage());
        }
    }
}