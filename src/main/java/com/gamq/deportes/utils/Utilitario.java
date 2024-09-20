package com.gamq.deportes.utils;

import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class Utilitario {
    private String pattern = "dd/MM/yyyy";
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
    private Calendar calendar = Calendar.getInstance();
    private static String[] mesesLiterales= {"ENERO","FEBRERO","MARZO","ABRIL","MAYO","JUNIO","JULIO","AGOSTO","SEPTIEMBRE","OCTUBRE","NOVIEMBRE","DICIEMBRE"};

    private String pattern2 = "yyyy-MM-dd";
    private SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat(pattern2);

    public Utilitario() {}

    public String getFechaFormateadaAnioMesDia(Date fecha)
    {
        return simpleDateFormat2.format(fecha);
    }

    /**
     *
     * @return fecha actual string formateada dd/mm/yyyy
     */
    public String getFechaActualFormateada()
    {
        return simpleDateFormat.format(new Date());
    }

    /**
     *
     * @return anio actual int
     */
    public Integer getAnioActual()
    {
        return calendar.get(Calendar.YEAR);
    }

    /**
     *
     * @return string mes actual formato mm 01-12
     */
    public String getMesActual()
    {
        String mesL = "01";
        int mes = calendar.get(Calendar.MONTH)+1;
        mesL = mes<10?"0"+mes:mes+"";
        return mesL;
    }

    /**
     *
     * @return string mes literal actual ENERO-DICIEMBRE
     */
    public String getMesLiteralActual()
    {
        String mesL = mesesLiterales[0];
        int mes = calendar.get(Calendar.MONTH);
        mesL = mesesLiterales[mes];
        return mesL;
    }

    /**
     *
     * @param fecha
     * @return fecha actual string formateada dd/mm/yyyy
     */
    public String getFechaFormateadaByDate(Date fecha)
    {
        return simpleDateFormat.format(fecha);
    }

    /**
     *
     * @param fecha
     * @return anio de la fecha enviada int
     */
    public Integer getAnioByDate(Date fecha)
    {
        calendar.setTime(fecha);
        return calendar.get(Calendar.YEAR);
    }

    /**
     *
     * @param fecha
     * @return string mes actual formato mm 01-12
     */
    public String getMesByDate(Date fecha)
    {
        calendar.setTime(fecha);
        String mesL = "01";
        int mes = calendar.get(Calendar.MONTH)+1;
        mesL = mes<10?"0"+mes:mes+"";
        return mesL;
    }

    /**
     *
     * @param fecha
     * @return string mes literal ENERO-FEBRERO
     */
    public String getMesLiteralByDate(Date fecha)
    {
        calendar.setTime(fecha);
        String mesL = mesesLiterales[0];
        int mes = calendar.get(Calendar.MONTH);
        mesL = mesesLiterales[mes];
        return mesL;
    }

    /**
     *
     * @param fecha
     * @return string fecha hora formateada
     */
    public String getFechaHoraFormateadaByDate(Date fecha)
    {
        String pattern2 = "dd/MM/yyyy HH:mm";
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat(pattern2);
        return simpleDateFormat2.format(fecha);
    }

    /**
     *
     * @param request
     * @return regresa toda la informacion posible del cliente en forma de texto
     */
    public String getAllInformacionFromCliente(HttpServletRequest request)
    {
        String res = "";
        res += "IP USUARIO: "+request.getRemoteAddr()+" || ";
        res += "PUERTO CLIENTE: "+request.getRemotePort()+" || ";
        res += "HOST CLIENTE: "+request.getRemoteHost()+" || ";
        res += "DATOS EQUIPO: "+request.getHeader("User-Agent");
        return res;
    }

    /**
     *
     * @param request
     * @return regresa la ip del cliente que realizo el request
     */
    public String getIpClienteFromRequest(HttpServletRequest request)
    {
        return request.getRemoteAddr();
    }

    /**
     *
     * @param request
     * @return regresa informacion del cliente su navegador su sistema operativo y versiones de esos
     */
    public String getInformacionNavegadorClienteFromRequest(HttpServletRequest request)
    {
        return request.getHeader("User-Agent");
    }

    /**
     *
     * @param session
     * @return Regresa un array string con tres valores
     * posicion 0 -> nuevo nombre de la carpeta
     * posicion 1 -> ruta completa de la carpeta
     * posicion 2 -> si todo salio bien OK sino ERROR
     */
    public String[] crearDirectorio(HttpSession session)
    {
        String res[] = new String[4];
        Date date = new Date();
        String strDateFormat = "ddMMyyyyhhmmssa";
        DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
        String formattedDate= dateFormat.format(date);
        Random random = new Random();
        int randomNum = random.nextInt();
        String nuevoNombreCarpeta = formattedDate+randomNum;
        String rutaCompletaCarpeta = session.getServletContext().getRealPath("/WEB-INF/classes/static/uploads/"+nuevoNombreCarpeta);
        try {
            File newFolder = new File(rutaCompletaCarpeta);
            boolean created =  newFolder.mkdir();
            if(created)
            {
                res[0] = nuevoNombreCarpeta;
                res[1] = rutaCompletaCarpeta;
                res[2] = "OK";
            }
            else
            {
                res[0] = "";
                res[1] = "";
                res[2] = "ERROR";
                System.out.println("No se pudo crear la carpeta");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return res;
    }

    /**
     *
     * @param rutaCarpeta
     * @param rutaZipFile
     * @return regresa el nombre del archivo zip creado
     */
    public String crearZipFile(String rutaCarpeta,String rutaZipFile)
    {
        String archivoZip = "";
        String nombreZip = rutaZipFile;
        try {
            archivoZip = this.zipFolder(new File(rutaCarpeta), new File(nombreZip));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return archivoZip;
    }

    private String zipFolder(File srcFolder, File destZipFile) throws Exception {
        String archivoZip = "";
        try {
            FileOutputStream fileWriter = new FileOutputStream(destZipFile);
            ZipOutputStream zip = new ZipOutputStream(fileWriter);
            addFolderToZip(srcFolder, srcFolder, zip);
            archivoZip = destZipFile.getName();

            zip.closeEntry();
            zip.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        return archivoZip;
    }

    private void addFileToZip(File rootPath, File srcFile, ZipOutputStream zip) throws Exception {

        if (srcFile.isDirectory()) {
            addFolderToZip(rootPath, srcFile, zip);
        } else {
            byte[] buf = new byte[1024];
            int len;
            try (FileInputStream in = new FileInputStream(srcFile)) {
                String name = srcFile.getPath();
                name = name.replace(rootPath.getPath(), "");

                zip.putNextEntry(new ZipEntry(name));
                while ((len = in.read(buf)) > 0) {
                    zip.write(buf, 0, len);
                }
            }
        }
    }

    private void addFolderToZip(File rootPath, File srcFolder, ZipOutputStream zip) throws Exception {
        for (File fileName : srcFolder.listFiles()) {
            addFileToZip(rootPath, fileName, zip);
        }
    }

    /**
     *
     * @param fechaUno
     * @param fechaDos
     * @return regresa 0 si las fechas son iguales, -1 si la fecha uno es menor a la fecha dos y 1 si la fecha uno es mayor a la fecha dos
     */
    public int compararSoloFechas(Date fechaUno,Date fechaDos) {
        int res = 0;
        if(fechaUno!=null && fechaDos!=null) {
            calendar.setTime(fechaUno);
            int dia1 = calendar.get(Calendar.DAY_OF_MONTH);
            int mes1 = calendar.get(Calendar.MONTH);
            int anio1 = calendar.get(Calendar.YEAR);

            calendar.setTime(fechaDos);
            int dia2 = calendar.get(Calendar.DAY_OF_MONTH);
            int mes2 = calendar.get(Calendar.MONTH);
            int anio2 = calendar.get(Calendar.YEAR);

            if(anio1>anio2) {
                res = 1;
            }else if(anio1 == anio2) {
                if(mes1>mes2) {
                    res = 1;
                }else if(mes1==mes2) {
                    if(dia1>dia2) {
                        res = 1;
                    }else if(dia1==dia2) {
                        res = 0;
                    }else {
                        res = -1;
                    }
                }else {
                    res = -1;
                }
            }else {
                res = -1;
            }

        }
        return res;
    }

    /**
     * param fechaUno
     * param fechaDos
     * @return regresa
     * 0 si alguna de las fechas en null
     * 1 verde si ahora es menor a la fecha limite
     * 2 amarillo si ahora es igual a la fecha limite en dia mes anio no importa la hora y minuto
     * 3 rojo si ahora es mayor a la fecha limite
     */
    public int compararAhoraConFechaLimiteHoraViewBandejaTramite(Date fechaLimite)
    {
        int res = 0;
        if(fechaLimite!=null) {
            calendar.setTime(new Date());
            int dia1 = calendar.get(Calendar.DAY_OF_MONTH);
            int mes1 = calendar.get(Calendar.MONTH);
            int anio1 = calendar.get(Calendar.YEAR);
            int hora1 = calendar.get(Calendar.HOUR_OF_DAY);
            int min1 = calendar.get(Calendar.MINUTE);

            calendar.setTime(fechaLimite);
            int dia2 = calendar.get(Calendar.DAY_OF_MONTH);
            int mes2 = calendar.get(Calendar.MONTH);
            int anio2 = calendar.get(Calendar.YEAR);
            int hora2 = calendar.get(Calendar.HOUR_OF_DAY);
            int min2 = calendar.get(Calendar.MINUTE);
            //System.out.println(String.format("Uno %s/%s/%s %s:%s", dia1,mes1,anio1,hora1,min1));
            //System.out.println(String.format("Dos %s/%s/%s %s:%s", dia2,mes2,anio2,hora2,min2));

            if(anio1<anio2) {
                res = 1;
            }else if(anio1 == anio2) {
                if(mes1<mes2) {
                    res = 1;
                }else if (mes1 == mes2) {
                    if(dia1<dia2) {
                        res = 1;
                    }else if (dia1 == dia2) {
                        if(hora1<hora2) {
                            res = 2;
                        }else if (hora1 == hora2) {
                            if(min1<min2) {
                                res = 2;
                            }else if (min1 == min2) {
                                res = 2;
                            }else {
                                res = 3;
                            }
                        }else {
                            res = 3;
                        }
                    }else {
                        res = 3;
                    }
                }else {
                    res = 3;
                }
            }else {
                res = 3;
            }

        }
        return res;
    }

    public Date agregarHorasAFecha(Date fecha,int horas)
    {
        Calendar calen = Calendar.getInstance();
        calen.setTime(fecha);
        calen.add(Calendar.HOUR_OF_DAY, horas);
        return calen.getTime();
    }

    public Date getFechaActual() {
        return new Date();
    }

    public Date getPrimeroEneroEsteAnio() {
        Calendar calen = Calendar.getInstance();
        calen.setTime(new Date());
        calen.set(calen.get(Calendar.YEAR), Calendar.JANUARY, 1);
        return calen.getTime();
    }

    public Date getUltimoDiaDiciembreAnioAnterior() {
        Calendar calen = Calendar.getInstance();
        calen.setTime(new Date());
        calen.set(calen.get(Calendar.YEAR)-1, Calendar.DECEMBER, 31);
        return calen.getTime();
    }

    /**
     * Agrega dias a la fecha enviada y devuel la nueva fecha
     * @param fecha
     * @param dias
     * @return
     */
    public Date agregarDiasAFecha(Date fecha,int dias)
    {
        Calendar calen = Calendar.getInstance();
        calen.setTime(fecha);
        calen.add(Calendar.DATE, dias);
        return calen.getTime();
    }

    /*
     * Permite ordenar un hash map por sus valores, recomendacion es mejor usar linked hash map en vez de ordenar
     */
    public Map<String,String> ordenarHashMapByValue(Map<String, String> mapa){
        List<Map.Entry<String, String> > list = new LinkedList<Map.Entry<String, String> >(mapa.entrySet());
        Collections.sort(list,(i1,i2) -> i1.getValue().compareTo(i2.getValue()));
        HashMap<String, String> temp = new LinkedHashMap<String, String>();
        for (Map.Entry<String, String> aa : list) {
            //System.out.println(aa.getKey()+" "+aa.getValue());
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    public String cantidadConLetra(Double s) {
        StringBuilder result = new StringBuilder();
        BigDecimal totalBigDecimal = new BigDecimal(s).setScale(2, RoundingMode.HALF_EVEN);
        long parteEntera = totalBigDecimal.toBigInteger().longValue();
        int triUnidades      = (int)((parteEntera % 1000));
        int triMiles         = (int)((parteEntera / 1000) % 1000);
        int triMillones      = (int)((parteEntera / 1000000) % 1000);
        int triMilMillones   = (int)((parteEntera / 1000000000) % 1000);

        if (parteEntera == 0) {
            result.append("Cero ");
            return result.toString();
        }

        if (triMilMillones > 0) result.append(triTexto(triMilMillones).toString() + "Mil ");
        if (triMillones > 0)    result.append(triTexto(triMillones).toString());

        if (triMilMillones == 0 && triMillones == 1) result.append("Millón ");
        else if (triMilMillones > 0 || triMillones > 0) result.append("Millones ");

        if (triMiles > 0)       result.append(triTexto(triMiles).toString() + "Mil ");
        if (triUnidades > 0)    result.append(triTexto(triUnidades).toString());

        BigDecimal fraccionParte = totalBigDecimal.remainder(BigDecimal.ONE).setScale(2, RoundingMode.HALF_EVEN);
        Long fraccionEntero = new BigDecimal((fraccionParte.doubleValue()*100)).toBigInteger().longValue();
        String fraccionLiteral = fraccionEntero+"/100";
        if(fraccionEntero > 0) {
            return result.toString()+" "+fraccionLiteral;
        }else {
            return result.toString();
        }
    }

    private StringBuilder triTexto(int n) {
        StringBuilder result = new StringBuilder();
        int centenas = n / 100;
        int decenas  = (n % 100) / 10;
        int unidades = (n % 10);

        switch (centenas) {
            case 0: break;
            case 1:
                if (decenas == 0 && unidades == 0) {
                    result.append("Cien ");
                    return result;
                }
                else result.append("Ciento ");
                break;
            case 2: result.append("Doscientos "); break;
            case 3: result.append("Trescientos "); break;
            case 4: result.append("Cuatrocientos "); break;
            case 5: result.append("Quinientos "); break;
            case 6: result.append("Seiscientos "); break;
            case 7: result.append("Setecientos "); break;
            case 8: result.append("Ochocientos "); break;
            case 9: result.append("Novecientos "); break;
        }

        switch (decenas) {
            case 0: break;
            case 1:
                if (unidades == 0) { result.append("Diez "); return result; }
                else if (unidades == 1) { result.append("Once "); return result; }
                else if (unidades == 2) { result.append("Doce "); return result; }
                else if (unidades == 3) { result.append("Trece "); return result; }
                else if (unidades == 4) { result.append("Catorce "); return result; }
                else if (unidades == 5) { result.append("Quince "); return result; }
                else result.append("Dieci");
                break;
            case 2:
                if (unidades == 0) { result.append("Veinte "); return result; }
                else result.append("Veinti");
                break;
            case 3: result.append("Treinta "); break;
            case 4: result.append("Cuarenta "); break;
            case 5: result.append("Cincuenta "); break;
            case 6: result.append("Sesenta "); break;
            case 7: result.append("Setenta "); break;
            case 8: result.append("Ochenta "); break;
            case 9: result.append("Noventa "); break;
        }

        if (decenas > 2 && unidades > 0)
            result.append("y ");

        switch (unidades) {
            case 0: break;
            case 1: result.append("Un "); break;
            case 2: result.append("Dos "); break;
            case 3: result.append("Tres "); break;
            case 4: result.append("Cuatro "); break;
            case 5: result.append("Cinco "); break;
            case 6: result.append("Seis "); break;
            case 7: result.append("Siete "); break;
            case 8: result.append("Ocho "); break;
            case 9: result.append("Nueve "); break;
        }

        return result;
    }

    /**
     *
     * @return anio anterior a la actual int
     */
    public Integer getAnioAnterior()
    {
        Calendar calenYear = Calendar.getInstance();
        calenYear.add(Calendar.YEAR, -1);
        return calenYear.get(Calendar.YEAR);
    }

    /**
     *
     * @param fecha
     * @return Devuelve el primero de enero del anio de la fecha que se envio
     */
    public Date getPrimeroEneroDelAnioDeLaFecha(Date fecha) {
        Calendar calen = Calendar.getInstance();
        calen.setTime(fecha);
        calen.set(calen.get(Calendar.YEAR), Calendar.JANUARY, 1);
        return calen.getTime();
    }

    public String getQrBase64Default() {
        return "iVBORw0KGgoAAAANSUhEUgAAAcwAAAHMAQMAAABiFQrFAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAGUExURQAAAP"
                + "///6XZn90AAAAJcEhZcwAADsMAAA7DAcdvqGQAAAIRSURBVHja7ZtNloIwEAabx8JljjBH8Wh6tDmKR8hyFj56RmJIgjoKoi1Q30Z"
                + "+UiTlAokd5Ep2+peftL9RPVy2ciaomKC44jodKiYorg+jteY5JPRLdS9VcTTmGPD3o84ExRVXXNfk6qXbLNHigsd4H80H/GZUTFBcc"
                + "cV1za6NdKkGDvjFqDNBccUV15W7ZrPszcABvxIVExRXXHFdk6v2HiSzzSahWdKA34w6ExRXXHGdl+tFivbNwHrOI6gzQcUExRXX6VAx"
                + "QdflqjdS1pqvZTzqTVDFVXGdCsX11aiVaxt33o1PjVWqrDQJDWe3z6NqguKKK6643htwvML2NPgw996fWsT2WZElnP0+77nxqJigDtdF"
                + "fsO44jqla/hoD6bKiqbHy6q/ECc2VBPUz23AuOI6nauf2zc8Q9dQmynO9Jdlx8Xa2c20Dv8QzArFFVdccb3fqxaPl+XNtEBDtuefDGeCy"
                + "twGjCuuuM7OtZ8wy87at1c5JrS+WUV6BFVQUFDQj0SvZJdXYpqiTJ0t5QYFBQVdIFprnrh+US5vph0aWoIuEvW44roM1/6reBdXkXS0TX"
                + "rJZgzqTFDBFVdccR054Gw9opw3u1f3Aj4e9Sao4vq5rvRKrx/ea5ZYZOnQNm48KvRKr/T63l5Bh6BZygl3ln3qo933oKCgoMtCb1dWYqqC"
                + "j1D9FOpNUFxxxRXXf1H1v4n8w0j8QbaeAAAAAElFTkSuQmCC";
    }

    /**
     * Regresa un numero entero, si no se pudo convertir regresa -1
     * @param numeroStr
     * @return
     */
    public Integer convertiStringAInteger(String numeroStr) {
        Integer res = 0;
        try {
            res = new BigDecimal(numeroStr).setScale(0, RoundingMode.HALF_EVEN).intValue();
        } catch (Exception e) {
            res = -1;
        }
        return res;
    }

    /**
     * Regresa un numero double con la cantidad de decimales enviados, si no se pudo convertir regresa -1
     * @param numeroStr
     * @param decimales
     * @return
     */
    public double convertirStringToDoubleAndDecimalPlaces(String numeroStr,int decimales) {
        double res = 0;
        try {
            res = new BigDecimal(numeroStr).setScale(decimales, RoundingMode.HALF_EVEN).doubleValue();
        } catch (Exception e) {
            res = -1;
        }
        return res;
    }

    /**
     * Convierte un string fecha en formato dd/mm/yyyy a date, si no es pudo regresa null
     * @param fechaStr
     * @return
     */
    public Date convertirStringDateEnFormatoDDMMYYYYToDate(String fechaStr){
        Date res = null;
        try {
            res = new SimpleDateFormat("dd/MM/yyyy").parse(fechaStr);
        } catch (Exception e) {
            res = null;
        }
        return res;
    }
}
