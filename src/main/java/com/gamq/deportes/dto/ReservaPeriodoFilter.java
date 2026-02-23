package com.gamq.deportes.dto;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ReservaPeriodoFilter {
    private Integer tab; // 0=Todas, 1=Solicitadas, 2=Pendientes, 3=Pagadas, 4=Mes en Curso, 5=Completadas, 6=Canceladas/Expiradas
    private String searchTerm;
    private String estadoReserva;
    private String estadoValidacion;
    private String categoria;
    private String escuelaFiltro; // UUID de organización
    private String periodoFiltro; // YYYY-MM

    public String getEstadoReservaFromTab() {
        if (tab == null) return null;
        switch (tab) {
            case 1: return "SOLICITADA";
            case 2: return "PENDIENTE";
            case 3: return "PAGADA";
            case 4: return "EN_CURSO";
            case 5: return "COMPLETADA";
            case 6: return "CANCELADA,EXPIRADA,NO_SHOW,RECHAZADA";
            default: return null;
        }
    }
}