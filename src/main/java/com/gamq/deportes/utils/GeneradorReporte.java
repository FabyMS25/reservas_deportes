package com.gamq.deportes.utils;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.*;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

@Component("generadorReporteComponent")
public class GeneradorReporte {
    @Autowired
    private DataSource dataSource;
    @Autowired
    private ResourceLoader resourceLoader;
    /**
     *
     * @param nombreArchivo para el archivo
     * @param pathReporte del archivo jrxml
     * @param parametros del reporte
     * @param response para devolver el archivo
     * @throws SQLException
     */
    public void generarSqlReportePdf(
            String nombreArchivo,
            String pathReporte,
            HashMap<String, Object> parametros,
            HttpServletResponse response
    ) throws SQLException
    {
        generarSqlReportePdf(nombreArchivo, pathReporte, parametros, response, false);
    }

    public void generarSqlReportePdfNoCache(
            String nombreArchivo,
            String pathReporte,
            HashMap<String, Object> parametros,
            HttpServletResponse response
    ) throws SQLException
    {
        generarSqlReportePdf(nombreArchivo, pathReporte, parametros, response, true);
    }

    private void generarSqlReportePdf(
            String nombreArchivo,
            String pathReporte,
            HashMap<String, Object> parametros,
            HttpServletResponse response,
            boolean noStore
    ) throws SQLException
    {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            Resource resource = resourceLoader.getResource(pathReporte);
            InputStream inputStream = resource.getInputStream();
            JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);
            String logo64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=";
            Resource logoResource = resourceLoader.getResource("classpath:imagenes/gamq1.jpg");
            if(logoResource.exists()) {
                logo64 = new String(Base64.encodeBase64(logoResource.getInputStream().readAllBytes()), "UTF-8");
            }
            parametros.put("logo64", logo64);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros,conn);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename="+nombreArchivo+".pdf");
            response.setHeader("Cache-Control", noStore ? "private, no-store, no-cache, must-revalidate" : "cache, must-revalidate");
            response.setHeader("Pragma", noStore ? "no-cache" : "public");
            JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } catch (JRException jrException) {
            throw new IllegalStateException("No se pudo generar el reporte PDF.", jrException);
        } catch (IOException ioException) {
            throw new IllegalStateException("No se pudo escribir el reporte PDF.", ioException);
        } catch (SQLException sqlException) {
            throw sqlException;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el reporte PDF.", e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    /**
     *
     * @param nombreArchivo para el archivo
     * @param pathReporte del archivo jrxml
     * @param parametros del reporte
     * @param response para devolver el archivo
     * @throws SQLException
     */
    public void generarSqlReporteWord(
            String nombreArchivo,
            String pathReporte,
            HashMap<String, Object> parametros,
            HttpServletResponse response
    ) throws SQLException
    {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            Resource resource = resourceLoader.getResource(pathReporte);
            InputStream inputStream = resource.getInputStream();
            JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);
            String logo64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=";
            Resource logoResource = resourceLoader.getResource("classpath:imagenes/gamq1.jpg");
            if(logoResource.exists()) {
                logo64 = new String(Base64.encodeBase64(logoResource.getInputStream().readAllBytes()), "UTF-8");
            }
            parametros.put("logo64", logo64);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros,conn);

            response.setContentType("application/msword");
            response.setHeader("Content-disposition", "attachment; filename=" + nombreArchivo + ".docx");
            JRDocxExporter exporter = new JRDocxExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(response.getOutputStream()));
            SimpleDocxReportConfiguration configuration = new SimpleDocxReportConfiguration();
            configuration.setFlexibleRowHeight(true);
            SimpleDocxExporterConfiguration configurationExport = new SimpleDocxExporterConfiguration();
            configurationExport.setMetadataAuthor("EMAPAQ");
            configurationExport.setMetadataTitle(nombreArchivo);
            exporter.setConfiguration(configuration);
            exporter.setConfiguration(configurationExport);
            exporter.exportReport();
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } catch (JRException jrException) {
            throw new IllegalStateException("No se pudo generar el reporte Word.", jrException);
        } catch (IOException ioException) {
            throw new IllegalStateException("No se pudo escribir el reporte Word.", ioException);
        } catch (SQLException sqlException) {
            throw sqlException;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el reporte Word.", e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    /**
     *
     * @param nombreArchivo para el archivo
     * @param pathReporte del archivo jrxml
     * @param parametros del reporte
     * @param response para devolver el archivo
     * @throws SQLException
     */
    public void generarSqlReporteExcel(
            String nombreArchivo,
            String pathReporte,
            HashMap<String, Object> parametros,
            HttpServletResponse response
    ) throws SQLException
    {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            Resource resource = resourceLoader.getResource(pathReporte);
            InputStream inputStream = resource.getInputStream();
            JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);
            String logo64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=";
            Resource logoResource = resourceLoader.getResource("classpath:imagenes/gamq1.jpg");
            if(logoResource.exists()) {
                logo64 = new String(Base64.encodeBase64(logoResource.getInputStream().readAllBytes()), "UTF-8");
            }
            parametros.put("logo64", logo64);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros,conn);

            response.setContentType("application/vnd.ms-excel;charset=UTF-8");
            response.setHeader("Content-disposition", "attachment; filename=" + nombreArchivo + ".xlsx");
            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(response.getOutputStream()));
            SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
            configuration.setOnePagePerSheet(false);
            configuration.setDetectCellType(true);
            configuration.setCollapseRowSpan(false);
            configuration.setWhitePageBackground(false);
            exporter.setConfiguration(configuration);
            exporter.exportReport();
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } catch (JRException jrException) {
            throw new IllegalStateException("No se pudo generar el reporte Excel.", jrException);
        } catch (IOException ioException) {
            throw new IllegalStateException("No se pudo escribir el reporte Excel.", ioException);
        } catch (SQLException sqlException) {
            throw sqlException;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el reporte Excel.", e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
