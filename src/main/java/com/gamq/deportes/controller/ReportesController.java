package com.gamq.deportes.controller;

import com.gamq.deportes.utils.GeneradorReporte;
import net.sf.jasperreports.engine.JRParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
@Controller
@RequestMapping("/api/reporte")
public class ReportesController {
    @Autowired
    private GeneradorReporte generadorReporte;
    ///////////////reserva puntual por hora/////////////////
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
            //ingreso almacen
            HashMap<String, Object> parametros = new HashMap<String,Object>();
                parametros.put("uuid", uuid);
                //parametros.put("logo64","iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk");

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
    @RequestMapping(value="/ticket-reser-per",method= RequestMethod.GET)
    @ResponseBody
    public void reporteTicketReservePersona (
            @RequestParam(name = "uuid", defaultValue = "0") String uuid,
            @RequestHeader Map<String, String> headers,
            HttpServletResponse response
    ) {
        try {
            //usuario
            //String usuario = headers.getOrDefault("usuario", "Default");
            //ingreso almacen
            HashMap<String, Object> parametros = new HashMap<String,Object>();
            parametros.put("uuid", uuid);
            //parametros.put("logo64","iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk");

            parametros.put(JRParameter.REPORT_LOCALE, new Locale("es", "ES"));
            try {
                generadorReporte.generarSqlReportePdf(
                        "ticket",
                        "classpath:reportes/ticketPuntualperson.jrxml",
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
    ///////////////reserva puntual por evento/////////////////
    @RequestMapping(value="/ticket-reser-ev",method= RequestMethod.GET)
    @ResponseBody
    public void reporteTicketReservaEvento (
            @RequestParam(name = "uuid", defaultValue = "0") String uuid,
            @RequestHeader Map<String, String> headers,
            HttpServletResponse response
    ) {
        try {
            //usuario
            //String usuario = headers.getOrDefault("usuario", "Default");
            //ingreso almacen
            HashMap<String, Object> parametros = new HashMap<String,Object>();
            parametros.put("uuid", uuid);
            //parametros.put("logo64","iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk");

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
            //usuario
            //String usuario = headers.getOrDefault("usuario", "Default");
            //ingreso almacen
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
            //ingreso almacen
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

    ////////////////////////RECURRENTES POR Sesion//////////////////////////////
    @RequestMapping(value="/ticket-recuses",method= RequestMethod.GET)
    @ResponseBody
    public void TicketRecuSesion (
            @RequestParam(name = "uuid", defaultValue = "0") String uuid,
            @RequestHeader Map<String, String> headers,
            HttpServletResponse response
    ) {
        try {
            //usuario
            //String usuario = headers.getOrDefault("usuario", "Default");
            //ingreso almacen
            HashMap<String, Object> parametros = new HashMap<String,Object>();
            parametros.put("uuid", uuid);
            //parametros.put("logo64","iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk");

            parametros.put(JRParameter.REPORT_LOCALE, new Locale("es", "ES"));
            try {
                generadorReporte.generarSqlReportePdf(
                        "Recurrente",
                        "classpath:reportes/ticketRecu.jrxml",
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

    ////////////////////////RECURRENTES POR PERSONA//////////////////////////////
    @RequestMapping(value="/ticket-recuper",method= RequestMethod.GET)
    @ResponseBody
    public void ticketRecuPer (
            @RequestParam(name = "uuid", defaultValue = "0") String uuid,
            @RequestHeader Map<String, String> headers,
            HttpServletResponse response
    ) {
        try {
            //usuario
            //String usuario = headers.getOrDefault("usuario", "Default");
            //ingreso almacen
            HashMap<String, Object> parametros = new HashMap<String,Object>();
            parametros.put("uuid", uuid);
            //parametros.put("logo64","iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk");

            parametros.put(JRParameter.REPORT_LOCALE, new Locale("es", "ES"));
            try {
                generadorReporte.generarSqlReportePdf(
                        "Recurrente",
                        "classpath:reportes/ticketRecuPer.jrxml",
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

    ////////////////////////RECURRENTES POR MENSUALIDAD//////////////////////////////
    @RequestMapping(value="/ticket-recumes",method= RequestMethod.GET)
    @ResponseBody
    public void ticketRecuMes (
            @RequestParam(name = "uuid", defaultValue = "0") String uuid,
            @RequestHeader Map<String, String> headers,
            HttpServletResponse response
    ) {
        try {
            //usuario
            //String usuario = headers.getOrDefault("usuario", "Default");
            //ingreso almacen
            HashMap<String, Object> parametros = new HashMap<String,Object>();
            parametros.put("uuid", uuid);
            //parametros.put("logo64","iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk");

            parametros.put(JRParameter.REPORT_LOCALE, new Locale("es", "ES"));
            try {
                generadorReporte.generarSqlReportePdf(
                        "Recurrente",
                        "classpath:reportes/ticketRecumen.jrxml",
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

    ////////////////////////RECURRENTES POR MENSUALIDAD//////////////////////////////
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
            //ingreso almacen
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
        //ingreso almacen

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
    @RequestMapping(value="/ticket-mensual",method= RequestMethod.GET)
    @ResponseBody
    public void reporteTicketPago (
            @RequestParam(name = "uuid", defaultValue = "0") String uuid,
            @RequestHeader Map<String, String> headers,
            HttpServletResponse response
    ) {
        try {
            //usuario
            //String usuario = headers.getOrDefault("usuario", "Default");
            //ingreso almacen
            HashMap<String, Object> parametros = new HashMap<String,Object>();
            parametros.put("uuid", uuid);
            //parametros.put("logo64","iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk");

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
