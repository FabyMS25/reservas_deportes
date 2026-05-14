package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.NotificacionDto;
import com.gamq.deportes.dto.mapper.NotificacionMapper;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import com.gamq.deportes.model.ConfiguracionSistema;
import com.gamq.deportes.model.Notificacion;
import com.gamq.deportes.repository.ConfiguracionSistemaRepository;
import com.gamq.deportes.repository.NotificacionRepository;
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

    @Autowired
    private NotificacionRepository notificacionRepository;

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
        if (notificacionDto.getReferenciaTipo() != null && notificacionDto.getReferenciaUuid() != null) {
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
    public NotificacionDto crearNotificacionSiHabilitada(String claveConfiguracion, NotificacionDto notificacionDto) {
        if (!estaHabilitada(claveConfiguracion)) {
            return null;
        }
        return crearNotificacion(notificacionDto);
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
    public NotificacionDto marcarComoEnviada(String uuid) {
        Notificacion notificacion = getNotificacion(uuid);
        notificacion.setEnviadaEn(LocalDateTime.now());
        NotificacionDto dto = NotificacionMapper.toDto(notificacionRepository.save(notificacion));
        publicar(dto);
        return dto;
    }

    @Override
    public NotificacionDto actualizarEstadoLectura(String uuid, String estadoLectura) {
        if (!"LEIDA".equalsIgnoreCase(estadoLectura) && !"NO_LEIDA".equalsIgnoreCase(estadoLectura)) {
            throw new IllegalArgumentException("estadoLectura debe ser LEIDA o NO_LEIDA");
        }
        Notificacion notificacion = getNotificacion(uuid);
        String nuevoEstado = estadoLectura.toUpperCase();
        notificacion.setEstadoLectura(nuevoEstado);
        notificacion.setLeidaEn("LEIDA".equals(nuevoEstado) ? LocalDateTime.now() : null);
        NotificacionDto dto = NotificacionMapper.toDto(notificacionRepository.save(notificacion));
        publicar(dto);
        return dto;
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
        Optional<ConfiguracionSistema> config = configuracionSistemaRepository.findByClaveAndEstadoFalse(claveConfiguracion);
        if (config.isEmpty() || !Boolean.TRUE.equals(config.get().getActivo())) {
            return true;
        }
        Boolean valor = config.get().getValorComoBoolean();
        return valor == null || Boolean.TRUE.equals(valor);
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
