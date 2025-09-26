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

    @RequestMapping(value="/ticket-reserva",method= RequestMethod.GET)
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

                parametros.put(JRParameter.REPORT_LOCALE, Locale.ENGLISH);
            try {
                generadorReporte.generarSqlReportePdf(
                        "ticket",
                        "classpath:reportes/ticket.jrxml",
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
    @RequestMapping(value="/lista-Reserva",method= RequestMethod.GET)
    @ResponseBody
    public void reporteListaReserva (
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

            parametros.put(JRParameter.REPORT_LOCALE, Locale.ENGLISH);
            try {
                generadorReporte.generarSqlReportePdf(
                        "Reserva",
                        "classpath:reportes/Reserva.jrxml",
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
    @RequestMapping(value="/lista-pagos",method= RequestMethod.GET)
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

            parametros.put(JRParameter.REPORT_LOCALE, Locale.ENGLISH);
            try {
                generadorReporte.generarSqlReportePdf(
                        "pago",
                        "classpath:reportes/pago.jrxml",
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

    @RequestMapping(value="/ticket-pago",method= RequestMethod.GET)
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

            parametros.put(JRParameter.REPORT_LOCALE, Locale.ENGLISH);
            try {
                generadorReporte.generarSqlReportePdf(
                        "ticketpago",
                        "classpath:reportes/ticketpago.jrxml",
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
