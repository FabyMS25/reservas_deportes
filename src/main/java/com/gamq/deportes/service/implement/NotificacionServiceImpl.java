package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.NotificacionDto;
import com.gamq.deportes.dto.NotificacionAdminDto;
import com.gamq.deportes.dto.mapper.NotificacionAdminMapper;
import com.gamq.deportes.dto.mapper.NotificacionMapper;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import com.gamq.deportes.model.ConfiguracionSistema;
import com.gamq.deportes.model.Notificacion;
import com.gamq.deportes.model.NotificacionAdmin;
import com.gamq.deportes.repository.ConfiguracionSistemaRepository;
import com.gamq.deportes.repository.NotificacionRepository;
import com.gamq.deportes.repository.NotificacionAdminRepository;
import com.gamq.deportes.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.deportes.exception.enums.EntityType.NOTIFICACION;

@Service
@Transactional
public class NotificacionServiceImpl implements NotificacionService {

    private static final int DIAS_NOTIFICACIONES_ADMIN = 30;

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private NotificacionAdminRepository notificacionAdminRepository;

    @Autowired
    private ConfiguracionSistemaRepository configuracionSistemaRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional(readOnly = true)
    public NotificacionDto findByUuid(String uuid) {
        return NotificacionMapper.toDto(getNotificacion(uuid));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionDto> obtenerPorUsuario(String usuarioUuid) {
        return notificacionRepository.findByUsuarioUuid(usuarioUuid).stream()
                .map(NotificacionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificacionDto> obtenerPorUsuario(String usuarioUuid, Pageable pageable) {
        return notificacionRepository.findByUsuarioUuid(usuarioUuid, pageable)
                .map(NotificacionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionDto> obtenerNoLeidas(String usuarioUuid) {
        return notificacionRepository.findNoLeidasByUsuarioUuid(usuarioUuid).stream()
                .map(NotificacionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarNoLeidas(String usuarioUuid) {
        return notificacionRepository.countNoLeidasByUsuarioUuid(usuarioUuid);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NotificacionDto crearNotificacion(NotificacionDto notificacionDto) {
        if (notificacionDto == null) {
            throw new NullReferenceException(NOTIFICACION);
        }
        validarCreacion(notificacionDto);

        Optional<Notificacion> duplicada = Optional.empty();
        if (notificacionDto.getReferenciaTipo() != null
                && notificacionDto.getReferenciaUuid() != null
                && !"NOTIFICAR_CAMBIO_HORARIO".equals(notificacionDto.getTipo())) {
            duplicada = notificacionRepository.findDuplicada(
                    notificacionDto.getUsuarioUuid(),
                    notificacionDto.getTipo(),
                    notificacionDto.getReferenciaTipo(),
                    notificacionDto.getReferenciaUuid());
        }
        if (duplicada.isPresent()) {
            return NotificacionMapper.toDto(duplicada.get());
        }

        Notificacion notificacion = NotificacionMapper.toEntity(notificacionDto);
        notificacion.setFechaCreacion(LocalDateTime.now());
        notificacion.setFechaActualizacion(LocalDateTime.now());
        Notificacion guardada = notificacionRepository.save(notificacion);
        NotificacionDto dto = NotificacionMapper.toDto(guardada);
        publicar(dto);
        return dto;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void crearNotificacionAdminSiHabilitada(String claveConfiguracion, NotificacionDto dto, String usuarioExcluidoUuid) {
        if (!estaHabilitada(claveConfiguracion)
                || !ambitoGlobal(claveConfiguracion)
                || dto == null) return;
        if (!"NOTIFICAR_CAMBIO_HORARIO".equals(dto.getTipo())
                && dto.getReferenciaTipo() != null && dto.getReferenciaUuid() != null
                && notificacionAdminRepository.findDuplicada(dto.getTipo(), dto.getReferenciaTipo(), dto.getReferenciaUuid()).isPresent()) {
            return;
        }
        NotificacionAdminDto eventoDto = new NotificacionAdminDto()
                .setTipo(dto.getTipo())
                .setTitulo(dto.getTitulo())
                .setMensaje(dto.getMensaje())
                .setPrioridad(dto.getPrioridad())
                .setCanal(dto.getCanal())
                .setReferenciaTipo(dto.getReferenciaTipo())
                .setReferenciaUuid(dto.getReferenciaUuid())
                .setAccionUrl(dto.getAccionUrl())
                .setMetadataJson(dto.getMetadataJson())
                .setUsuarioExcluidoUuid(usuarioExcluidoUuid);
        notificacionAdminRepository.save(NotificacionAdminMapper.toEntity(eventoDto));
    }

    @Override
    public List<NotificacionDto> sincronizarNotificacionesAdmin(String usuarioUuid) {
        if (usuarioUuid == null || usuarioUuid.isBlank()) {
            throw new IllegalArgumentException("usuarioUuid es requerido");
        }
        LocalDateTime desde = LocalDateTime.now().minusDays(DIAS_NOTIFICACIONES_ADMIN);
        for (NotificacionAdmin evento : notificacionAdminRepository.findRecientes(desde)) {
            NotificacionAdminDto eventoDto = NotificacionAdminMapper.toDto(evento);
            if (usuarioUuid.equals(eventoDto.getUsuarioExcluidoUuid())
                    || notificacionRepository.existsEntregaAdminIncluyendoEliminadas(usuarioUuid, evento.getUuid())) {
                continue;
            }
            crearNotificacion(new NotificacionDto()
                    .setUsuarioUuid(usuarioUuid)
                    .setTipo(eventoDto.getTipo())
                    .setTitulo(eventoDto.getTitulo())
                    .setMensaje(eventoDto.getMensaje())
                    .setPrioridad(eventoDto.getPrioridad())
                    .setCanal(eventoDto.getCanal())
                    .setReferenciaTipo(eventoDto.getReferenciaTipo())
                    .setReferenciaUuid(eventoDto.getReferenciaUuid())
                    .setAccionUrl(eventoDto.getAccionUrl())
                    .setMetadataJson(eventoDto.getMetadataJson())
                    .setNotificacionAdminUuid(eventoDto.getUuid()));
        }
        return obtenerPorUsuario(usuarioUuid);
    }

    @Override
    public int limpiarNotificacionesAdminAntiguas(int diasRetencion) {
        if (diasRetencion < DIAS_NOTIFICACIONES_ADMIN) {
            throw new IllegalArgumentException("La retencion no puede ser menor a " + DIAS_NOTIFICACIONES_ADMIN + " dias");
        }
        return notificacionAdminRepository.archivarAnterioresA(LocalDateTime.now().minusDays(diasRetencion));
    }

    @Override
    public NotificacionDto marcarComoLeida(String uuid) {
        Notificacion notificacion = getNotificacion(uuid);
        notificacion.setEstadoLectura("LEIDA");
        notificacion.setLeidaEn(LocalDateTime.now());
        NotificacionDto dto = NotificacionMapper.toDto(notificacionRepository.save(notificacion));
        publicar(dto);
        return dto;
    }

    @Override
    public List<NotificacionDto> marcarTodasComoLeidas(String usuarioUuid) {
        notificacionRepository.marcarTodasComoLeidas(usuarioUuid, LocalDateTime.now());
        List<NotificacionDto> notificaciones = obtenerPorUsuario(usuarioUuid);
        publicarResumen(usuarioUuid);
        return notificaciones;
    }

    @Override
    public String eliminarNotificacion(String uuid) {
        try {
            Notificacion notificacion = getNotificacion(uuid);
            String usuarioUuid = notificacion.getUsuarioUuid();
            notificacionRepository.delete(notificacion);
            publicarResumen(usuarioUuid);
            return "Eliminacion exitosa";
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al intentar eliminar la notificacion.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaHabilitada(String claveConfiguracion) {
        if (claveConfiguracion == null || claveConfiguracion.trim().isEmpty()) {
            return true;
        }
        Optional<ConfiguracionSistema> config = configuracionSistemaRepository
                .findNotificationConfigsByClave(claveConfiguracion).stream().findFirst();
        if (config.isEmpty() || !Boolean.TRUE.equals(config.get().getActivo())) {
            return false;
        }
        ConfiguracionSistema configuracion = config.get();
        if (configuracion.getValorBoolean() != null) {
            return Boolean.TRUE.equals(configuracion.getValorBoolean());
        }
        return configuracion.getValorNumerico() != null
                && configuracion.getValorNumerico() > 0;
    }

    private boolean ambitoGlobal(String claveConfiguracion) {
        return configuracionSistemaRepository.findNotificationConfigsByClave(claveConfiguracion).stream().findFirst()
                .map(config -> "global".equalsIgnoreCase(config.getAplicaA()))
                .orElse(false);
    }

    private Notificacion getNotificacion(String uuid) {
        return notificacionRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(NOTIFICACION, uuid));
    }

    private void validarCreacion(NotificacionDto dto) {
        if (dto.getUsuarioUuid() == null || dto.getUsuarioUuid().trim().isEmpty()) {
            throw new IllegalArgumentException("usuarioUuid es requerido");
        }
        if (dto.getTipo() == null || dto.getTipo().trim().isEmpty()) {
            throw new IllegalArgumentException("tipo es requerido");
        }
        if (dto.getTitulo() == null || dto.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("titulo es requerido");
        }
        if (dto.getMensaje() == null || dto.getMensaje().trim().isEmpty()) {
            throw new IllegalArgumentException("mensaje es requerido");
        }
    }

    private void publicar(NotificacionDto dto) {
        messagingTemplate.convertAndSend("/topic/notificaciones/" + dto.getUsuarioUuid(), dto);
        publicarResumen(dto.getUsuarioUuid());
    }

    private void publicarResumen(String usuarioUuid) {
        messagingTemplate.convertAndSend(
                "/topic/notificaciones/" + usuarioUuid + "/contador",
                contarNoLeidas(usuarioUuid));
    }
}
