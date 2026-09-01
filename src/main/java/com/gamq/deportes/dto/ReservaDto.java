package com.gamq.deportes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@ToString
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class    ReservaDto {
    private Long idReserva;
    private String uuid;
    private boolean estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    private LocalDateTime fechaReserva;
    private LocalDateTime fechaLimitePago;
    private Boolean cancelacionAutomatica;
    private String motivoCancelacionAutomatica;
    private String autorizadoPor;
    private LocalDateTime fechaAprobacion;
    private LocalDateTime fechaRechazo;
    private String motivoRechazo;
    private String aprobadoPor;
    private String creadoPor;
    private String anuladoPor;
    private String estadoValidacion; // "PENDIENTE", "APROBADA", "RECHAZADA"

    private String estadoReserva;

    private String tipoReserva; //puntual / recurrente
    private String numeroReserva;
    private String numeroTasa;
    private BigDecimal montoTotal;
    private String clasificadorRuatId; //saved to identify later
//    private String categoria; //saved to identify later
//    private String superficie; //saved to identify later
//    private Integer totalPersonas;
//    private LocalTime horaEntrada;
//    private LocalTime horaSalida;

    private boolean esReservaGratuita;
    private String motivoGratuidad;

    private OrganizacionDto organizacionDto;
    private ContribuyenteDto contribuyenteDto;
    private CampoDeportivoDto campoDeportivoDto;
    private TarifaDto tarifaDto;

    private ReservaPuntualDto reservaPuntualDto;
    private ReservaRecurrenteDto reservaRecurrenteDto;
    private List<ReservaPeriodoDto> reservasPeriodoDtoList = new ArrayList<>();

    public List<PagoDto> pagosDtoList = new ArrayList<>();
    public List<ReservaAuditoriaDto> auditoriasDtoList = new ArrayList<>();
    public List<ReservaTarifaDetalleDto> tarifaDetallesDtoList = new ArrayList<>();
}
