package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.NotificacionDto;
import com.gamq.deportes.model.ConfiguracionSistema;
import com.gamq.deportes.model.EncargadoComplejo;
import com.gamq.deportes.model.Reserva;
import com.gamq.deportes.repository.ConfiguracionSistemaRepository;
import com.gamq.deportes.repository.EncargadoComplejoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Applies the recipient meaning of a notification configuration:
 * contribuyente = reservation taxpayer; global = taxpayer, active complex
 * managers and the administrator event feed.
 */
@Service
public class NotificacionDestinatariosReservaService {

    private final ConfiguracionSistemaRepository configuracionRepository;
    private final EncargadoComplejoRepository encargadoRepository;
    private final NotificacionEventPublisher eventPublisher;

    public NotificacionDestinatariosReservaService(
            ConfiguracionSistemaRepository configuracionRepository,
            EncargadoComplejoRepository encargadoRepository,
            NotificacionEventPublisher eventPublisher) {
        this.configuracionRepository = configuracionRepository;
        this.encargadoRepository = encargadoRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public void publicar(String clave,
                         Reserva reserva,
                         NotificacionDto mensajeContribuyente,
                         NotificacionDto mensajeInterno,
                         String usuarioExcluidoUuid) {
        ConfiguracionSistema config = configuracionRepository.findByClaveAndEstadoFalse(clave)
                .filter(ConfiguracionSistema::isConfiguracionActiva)
                .filter(this::tieneValorHabilitado)
                .orElse(null);
        if (config == null || reserva == null) {
            return;
        }

        boolean global = "global".equalsIgnoreCase(config.getAplicaA());
        boolean contribuyente = global || "contribuyente".equalsIgnoreCase(config.getAplicaA());
        if (!contribuyente) {
            return;
        }

        Set<String> destinatarios = new HashSet<>();
        if (reserva.getContribuyente() != null
                && reserva.getContribuyente().getUuid() != null) {
            String uuid = reserva.getContribuyente().getUuid();
            destinatarios.add(uuid);
            eventPublisher.publicar(copiar(mensajeContribuyente).setUsuarioUuid(uuid));
        }

        if (!global) {
            return;
        }

        if (reserva.getCampoDeportivo() != null
                && reserva.getCampoDeportivo().getComplejo() != null
                && reserva.getCampoDeportivo().getComplejo().getUuid() != null) {
            for (EncargadoComplejo encargado : encargadoRepository.findActivosByComplejo(
                    reserva.getCampoDeportivo().getComplejo().getUuid())) {
                String uuid = encargado.getUserUuid();
                if (uuid == null || uuid.isBlank()
                        || uuid.equals(usuarioExcluidoUuid)
                        || !destinatarios.add(uuid)) {
                    continue;
                }
                eventPublisher.publicar(copiar(mensajeInterno).setUsuarioUuid(uuid));
            }
        }

        eventPublisher.publicarAdminSiHabilitada(
                clave, copiar(mensajeInterno), usuarioExcluidoUuid);
    }

    private boolean tieneValorHabilitado(ConfiguracionSistema config) {
        if (config.getValorBoolean() != null) {
            return Boolean.TRUE.equals(config.getValorBoolean());
        }
        if (config.getValorNumerico() != null) {
            return config.getValorNumerico() > 0;
        }
        return false;
    }

    private NotificacionDto copiar(NotificacionDto origen) {
        return new NotificacionDto()
                .setTipo(origen.getTipo())
                .setTitulo(origen.getTitulo())
                .setMensaje(origen.getMensaje())
                .setPrioridad(origen.getPrioridad())
                .setCanal(origen.getCanal())
                .setReferenciaTipo(origen.getReferenciaTipo())
                .setReferenciaUuid(origen.getReferenciaUuid())
                .setAccionUrl(origen.getAccionUrl())
                .setProgramadaPara(origen.getProgramadaPara())
                .setExpiraEn(origen.getExpiraEn())
                .setMetadataJson(origen.getMetadataJson());
    }
}
