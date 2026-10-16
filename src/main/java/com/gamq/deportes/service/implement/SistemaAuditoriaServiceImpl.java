package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.SistemaAuditoriaDto;
import com.gamq.deportes.dto.mapper.SistemaAuditoriaMapper;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import com.gamq.deportes.model.SistemaAuditoria;
import com.gamq.deportes.repository.SistemaAuditoriaRepository;
import com.gamq.deportes.service.SistemaAuditoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.gamq.deportes.enums.EntityType.SISTEMA_AUDITORIA;

@Service
@Transactional
public class SistemaAuditoriaServiceImpl implements SistemaAuditoriaService {

    @Autowired
    private SistemaAuditoriaRepository sistemaAuditoriaRepository;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Override
    public String snapshotPago(com.gamq.deportes.model.Pago pago) {
        com.fasterxml.jackson.databind.node.ObjectNode data = objectMapper.createObjectNode();
        data.put("uuid", pago.getUuid());
        data.put("reservaUuid", pago.getReserva().getUuid());
        data.put("numeroReserva", pago.getReserva().getNumeroReserva());
        data.put("numeroTasa", pago.getNumeroTasa());
        data.put("folio", pago.getFolio());
        data.put("estadoPago", pago.getEstadoPago());
        data.put("montoPagado", pago.getMontoPagado());
        data.set("fechaPago", objectMapper.valueToTree(pago.getFechaPago()));
        data.put("formaPago", pago.getFormaPago());
        data.put("entidadFinanciera", pago.getEntidadFinanciera());
        data.put("observaciones", pago.getObservaciones());
        // Only the payment-query body; never HTTP headers or credentials.
        data.put("respuestaRuat", pago.getRespuestaCompletaRuat());
        return data.toString();
    }

    @Override
    public void registrarPago(String accion, String anterior, com.gamq.deportes.model.Pago pago) {
        String cambios = "{\"anterior\":" + (anterior == null ? "null" : anterior)
                + ",\"nuevo\":" + ("DELETE".equals(accion) ? "null" : snapshotPago(pago)) + "}";
        registrarAccion(null, accion, "PAGOS", "PAGO", pago.getUuid(),
                "Pago de reserva " + pago.getReserva().getNumeroReserva(), cambios, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public SistemaAuditoriaDto findByUuid(String uuid) {
        SistemaAuditoria auditoria = sistemaAuditoriaRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(SISTEMA_AUDITORIA, uuid));
        return SistemaAuditoriaMapper.toDto(auditoria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SistemaAuditoriaDto> obtenerSistemaAuditorias() {
        List<SistemaAuditoria> auditorias = sistemaAuditoriaRepository.findAll();
        return auditorias.stream()
                .map(SistemaAuditoriaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SistemaAuditoriaDto> obtenerSistemaAuditorias(Pageable pageable) {
        return sistemaAuditoriaRepository.findAll(pageable)
                .map(SistemaAuditoriaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SistemaAuditoriaDto> findByUsuarioUuid(String usuarioUuid) {
        return sistemaAuditoriaRepository.findByUsuarioUuid(usuarioUuid).stream()
                .map(SistemaAuditoriaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SistemaAuditoriaDto> findByAccion(String accion) {
        return sistemaAuditoriaRepository.findByAccion(accion).stream()
                .map(SistemaAuditoriaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SistemaAuditoriaDto> findByModulo(String modulo) {
        return sistemaAuditoriaRepository.findByModulo(modulo).stream()
                .map(SistemaAuditoriaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SistemaAuditoriaDto> findByEntidad(String entidadTipo, String entidadUuid) {
        return sistemaAuditoriaRepository.findByEntidad(entidadTipo, entidadUuid).stream()
                .map(SistemaAuditoriaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SistemaAuditoriaDto> findByFechaAccionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return sistemaAuditoriaRepository.findByFechaAccionBetween(fechaInicio, fechaFin).stream()
                .map(SistemaAuditoriaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SistemaAuditoriaDto> findByFechaAccionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable) {
        return sistemaAuditoriaRepository.findByFechaAccionBetween(fechaInicio, fechaFin, pageable)
                .map(SistemaAuditoriaMapper::toDto);
    }

    @Override
    public SistemaAuditoriaDto crearSistemaAuditoria(SistemaAuditoriaDto auditoriaDto) {
        if (auditoriaDto == null) {
            throw new NullReferenceException(SISTEMA_AUDITORIA);
        }
        SistemaAuditoria auditoria = SistemaAuditoriaMapper.toEntity(auditoriaDto);
        return SistemaAuditoriaMapper.toDto(sistemaAuditoriaRepository.save(auditoria));
    }

    @Override
    public void registrarAccion(String usuarioUuid, String accion, String modulo, String entidadTipo, String entidadUuid,
                                String descripcion, String datosCambio, String direccionIp, String userAgent) {
        SistemaAuditoria auditoria = new SistemaAuditoria();
        auditoria.setUsuarioUuid(usuarioUuid);
        auditoria.setAccion(accion);
        auditoria.setModulo(modulo);
        auditoria.setEntidadTipo(entidadTipo);
        auditoria.setEntidadUuid(entidadUuid);
        auditoria.setDescripcion(descripcion);
        auditoria.setDatosCambio(datosCambio);
        auditoria.setDireccionIp(direccionIp);
        auditoria.setUserAgent(userAgent);
        auditoria.setFechaAccion(LocalDateTime.now());

        sistemaAuditoriaRepository.save(auditoria);
    }

    @Override
    public String delete(String uuid) {
        SistemaAuditoria auditoria = sistemaAuditoriaRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(SISTEMA_AUDITORIA, uuid));
        auditoria.setEstado(true);
        sistemaAuditoriaRepository.save(auditoria);
        return "Eliminación exitosa de auditoría de sistema";
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getEstadisticasPorAccion() {
        return sistemaAuditoriaRepository.countByAccionGroup();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getEstadisticasPorModulo() {
        return sistemaAuditoriaRepository.countByModuloGroup();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getEstadisticasPorUsuario() {
        return sistemaAuditoriaRepository.countByUsuarioUuidGroup();
    }
}
