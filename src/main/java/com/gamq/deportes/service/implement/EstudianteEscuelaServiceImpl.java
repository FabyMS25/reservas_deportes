package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.EstudianteEscuelaDto;
import com.gamq.deportes.dto.mapper.EstudianteEscuelaMapper;
import com.gamq.deportes.model.Contribuyente;
import com.gamq.deportes.model.Estudiante;
import com.gamq.deportes.model.EstudianteEscuela;
import com.gamq.deportes.model.Organizacion;
import com.gamq.deportes.repository.ContribuyenteRepository;
import com.gamq.deportes.repository.EstudianteEscuelaRepository;
import com.gamq.deportes.repository.EstudianteRepository;
import com.gamq.deportes.repository.OrganizacionRepository;
import com.gamq.deportes.security.DeportesPrincipal;
import com.gamq.deportes.service.EstudianteEscuelaService;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.deportes.exception.enums.EntityType.ESTUDIANTE_ESCUELA;
import static com.gamq.deportes.exception.enums.EntityType.CONTRIBUYENTE;
import static com.gamq.deportes.exception.enums.EntityType.ORGANIZACION;

@Component
public class EstudianteEscuelaServiceImpl implements EstudianteEscuelaService {

    @Autowired
    private EstudianteEscuelaRepository estudianteEscuelaRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ContribuyenteRepository contribuyenteRepository;

    @Autowired
    private OrganizacionRepository organizacionRepository;

    @Override
    public EstudianteEscuelaDto findByUuid(String uuid) {
        Optional<EstudianteEscuela> estudianteOptional = estudianteEscuelaRepository.findByUuid(uuid);
        if (estudianteOptional.isPresent()) {
            validarPuedeVer(estudianteOptional.get());
            return EstudianteEscuelaMapper.toDto(estudianteOptional.get(), true);
        } else {
            throw new NotFoundException(ESTUDIANTE_ESCUELA, uuid);
        }
    }

    @Override
    public EstudianteEscuelaDto findByMatricula(String matricula) {
        Optional<EstudianteEscuela> estudianteOptional = estudianteEscuelaRepository.findByMatricula(matricula);
        if (estudianteOptional.isPresent()) {
            validarPuedeVer(estudianteOptional.get());
            return EstudianteEscuelaMapper.toDto(estudianteOptional.get(), true);
        } else {
            throw new NotFoundException(ESTUDIANTE_ESCUELA, matricula);
        }
    }

    @Override
    public EstudianteEscuelaDto findByNumeroDocumento(String numeroDocumento) {
        Optional<EstudianteEscuela> estudianteOptional = estudianteEscuelaRepository.findByNumeroDocumento(numeroDocumento);
        if (estudianteOptional.isPresent()) {
            validarPuedeVer(estudianteOptional.get());
            return EstudianteEscuelaMapper.toDto(estudianteOptional.get(), true);
        } else {
            throw new NotFoundException(ESTUDIANTE_ESCUELA, numeroDocumento);
        }
    }

    @Override
    @Transactional
    public List<EstudianteEscuelaDto> obtenerEstudiantes() {
        String contribuyenteUuid = contribuyenteActualUuid();
        List<EstudianteEscuela> estudiantes = contribuyenteUuid == null
                ? estudianteEscuelaRepository.findAll()
                : estudianteEscuelaRepository.findVisibleToContribuyente(contribuyenteUuid);
        return estudiantes.stream()
                .map(estudiante -> EstudianteEscuelaMapper.toDto(estudiante, false))
                .collect(Collectors.toList());
    }

    @Override
    public List<EstudianteEscuelaDto> findByContribuyenteUuid(String uuidContribuyente) {
        validarContribuyentePropio(uuidContribuyente);
        List<EstudianteEscuela> estudiantes = estudianteEscuelaRepository.findByContribuyenteUuid(uuidContribuyente);
        return estudiantes.stream()
                .map(estudiante -> EstudianteEscuelaMapper.toDto(estudiante, false))
                .collect(Collectors.toList());
    }

    @Override
    public List<EstudianteEscuelaDto> findByOrganizacionUuid(String uuidOrganizacion) {
        validarPuedeGestionarOrganizacion(uuidOrganizacion);
        return estudianteEscuelaRepository.findByOrganizacionUuid(uuidOrganizacion).stream()
                .map(inscripcion -> EstudianteEscuelaMapper.toDto(inscripcion, false))
                .collect(Collectors.toList());
    }

    @Override
    public List<EstudianteEscuelaDto> findByActivoTrue() {
        String contribuyenteUuid = contribuyenteActualUuid();
        List<EstudianteEscuela> estudiantes = contribuyenteUuid == null
                ? estudianteEscuelaRepository.findByActivoTrue()
                : estudianteEscuelaRepository.findActiveVisibleToContribuyente(contribuyenteUuid);
        return estudiantes.stream()
                .map(estudiante -> EstudianteEscuelaMapper.toDto(estudiante, false))
                .collect(Collectors.toList());
    }

    @Override
    public List<EstudianteEscuelaDto> findByActivoTrueAndOrganizacionUuid(String uuidOrganizacion) {
        validarPuedeGestionarOrganizacion(uuidOrganizacion);
        return estudianteEscuelaRepository.findByActivoTrueAndOrganizacionUuid(uuidOrganizacion).stream()
                .map(inscripcion -> EstudianteEscuelaMapper.toDto(inscripcion, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EstudianteEscuelaDto crearEstudiante(EstudianteEscuelaDto estudianteDto) {
        validarSolicitud(estudianteDto);
        Estudiante estudiante = resolverEstudiante(estudianteDto);
        Organizacion organizacion = organizacionRepository.findByUuid(estudianteDto.getOrganizacionDto().getUuid())
                .orElseThrow(() -> new NotFoundException(ORGANIZACION,
                        estudianteDto.getOrganizacionDto().getUuid()));
        Contribuyente responsable = resolverResponsable(estudianteDto);
        validarPuedeCrearInscripcion(organizacion, responsable);
        // An existing student keeps one identity across schools. Apply any edited
        // personal data to that identity before creating the new enrollment.
        EstudianteEscuelaMapper.updatePersonalDataFromDto(estudianteDto, estudiante);
        estudianteRepository.save(estudiante);
        validarInscripcionActivaDuplicada(estudiante, organizacion);
        EstudianteEscuela inscripcion = EstudianteEscuelaMapper.toNewEntity(
                estudianteDto, responsable, organizacion);
        inscripcion.setEstudiante(estudiante);
        EstudianteEscuela guardada = estudianteEscuelaRepository.saveAndFlush(inscripcion);
        entityManager.refresh(guardada); // Read matrícula generated by the database trigger.
        return EstudianteEscuelaMapper.toDto(guardada, true);
    }

    @Override
    @Transactional
    public EstudianteEscuelaDto actualizarEstudiante(EstudianteEscuelaDto estudianteDto) {
        if (estudianteDto != null) {
            if (estudianteDto.getNombreCompleto() == null
                    || estudianteDto.getNombreCompleto().trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre del estudiante es requerido");
            }
            Optional<EstudianteEscuela> estudianteOptional = estudianteEscuelaRepository.findByUuid(estudianteDto.getUuid());

            if (estudianteOptional.isPresent()) {
                EstudianteEscuela estudianteExistente = estudianteOptional.get();
                validarPuedeActualizarInscripcion(estudianteExistente, estudianteDto);
                EstudianteEscuelaMapper.updateEntityFromDto(estudianteDto, estudianteExistente);

                if (estudianteDto.getOrganizacionDto() != null
                        && estudianteDto.getOrganizacionDto().getUuid() != null) {
                    String escuelaActual = estudianteExistente.getOrganizacionDto().getUuid();
                    if (!escuelaActual.equals(estudianteDto.getOrganizacionDto().getUuid())) {
                        throw new IllegalArgumentException(
                                "No se puede cambiar la escuela de una inscripción. Dé de baja esta inscripción y cree otra");
                    }
                }
                Contribuyente responsable = estudianteDto.getContribuyenteDto() != null
                        ? resolverResponsable(estudianteDto)
                        : estudianteExistente.getContribuyenteDto();
                estudianteExistente.setContribuyenteDto(responsable);
                estudianteRepository.save(estudianteExistente.getEstudiante());

                return EstudianteEscuelaMapper.toDto(estudianteEscuelaRepository.save(estudianteExistente), true);
            } else {
                throw new NotFoundException(ESTUDIANTE_ESCUELA, estudianteDto.getUuid());
            }
        } else {
            throw new NullReferenceException(ESTUDIANTE_ESCUELA);
        }
    }

    @Override
    @Transactional
    public EstudianteEscuelaDto darDeBajaEstudiante(String uuid, String motivoBaja) {
        Optional<EstudianteEscuela> estudianteOptional = estudianteEscuelaRepository.findByUuid(uuid);
        if (estudianteOptional.isPresent()) {
            EstudianteEscuela estudiante = estudianteOptional.get();
            validarPuedeGestionar(estudiante.getOrganizacionDto());
            if (!estudiante.estaActivo()) {
                throw new IllegalArgumentException("La inscripción ya se encuentra inactiva");
            }
            estudiante.darDeBaja();
            estudiante.setMotivoBaja(
                    motivoBaja == null || motivoBaja.trim().isEmpty() ? null : motivoBaja.trim());
            return EstudianteEscuelaMapper.toDto(estudianteEscuelaRepository.save(estudiante), true);
        } else {
            throw new NotFoundException(ESTUDIANTE_ESCUELA, uuid);
        }
    }

    @Override
    @Transactional
    public EstudianteEscuelaDto reactivarEstudiante(String uuid) {
        Optional<EstudianteEscuela> estudianteOptional = estudianteEscuelaRepository.findByUuid(uuid);
        if (estudianteOptional.isPresent()) {
            EstudianteEscuela anterior = estudianteOptional.get();
            validarPuedeGestionar(anterior.getOrganizacionDto());
            if (anterior.estaActivo()) {
                throw new IllegalArgumentException("La inscripción ya se encuentra activa");
            }
            validarInscripcionActivaDuplicada(anterior.getEstudiante(), anterior.getOrganizacionDto());
            EstudianteEscuela nueva = new EstudianteEscuela();
            nueva.setEstudiante(anterior.getEstudiante());
            nueva.setOrganizacionDto(anterior.getOrganizacionDto());
            nueva.setContribuyenteDto(anterior.getContribuyenteDto());
            nueva.setFechaInscripcion(java.time.LocalDate.now());
            nueva.setActivo(true);
            EstudianteEscuela guardada = estudianteEscuelaRepository.saveAndFlush(nueva);
            entityManager.refresh(guardada);
            return EstudianteEscuelaMapper.toDto(guardada, true);
        } else {
            throw new NotFoundException(ESTUDIANTE_ESCUELA, uuid);
        }
    }

    @Override
    @Transactional
    public String eliminarEstudiante(String uuid) {
        EstudianteEscuela estudiante = estudianteEscuelaRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(ESTUDIANTE_ESCUELA, uuid));
        validarPuedeGestionar(estudiante.getOrganizacionDto());
        estudiante.darDeBaja();
        estudianteEscuelaRepository.save(estudiante);
        return "La inscripción fue dada de baja; el estudiante y su historial se conservaron: " + uuid;
    }

    @Override
    @Transactional
    public EstudianteEscuelaDto quitarResponsable(String uuid) {
        EstudianteEscuela inscripcion = estudianteEscuelaRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(ESTUDIANTE_ESCUELA, uuid));
        validarPuedeGestionar(inscripcion.getOrganizacionDto());
        inscripcion.setContribuyenteDto(null);
        return EstudianteEscuelaMapper.toDto(estudianteEscuelaRepository.save(inscripcion), true);
    }

    @Override
    public Long contarEstudiantesActivosPorOrganizacion(String uuidOrganizacion) {
        validarPuedeGestionarOrganizacion(uuidOrganizacion);
        return estudianteEscuelaRepository.countByOrganizacionUuidAndActivoTrue(uuidOrganizacion);
    }

    private void validarSolicitud(EstudianteEscuelaDto dto) {
        if (dto == null) throw new NullReferenceException(ESTUDIANTE_ESCUELA);
        if (dto.getNombreCompleto() == null || dto.getNombreCompleto().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del estudiante es requerido");
        }
        if (dto.getOrganizacionDto() == null || dto.getOrganizacionDto().getUuid() == null) {
            throw new IllegalArgumentException("La escuela es requerida para crear una inscripción");
        }
    }

    private Estudiante resolverEstudiante(EstudianteEscuelaDto dto) {
        if (dto.getEstudianteUuid() != null && !dto.getEstudianteUuid().trim().isEmpty()) {
            return estudianteRepository.findByUuid(dto.getEstudianteUuid())
                    .orElseThrow(() -> new IllegalArgumentException("No existe el estudiante indicado"));
        }
        if (dto.getNumeroDocumento() != null && !dto.getNumeroDocumento().trim().isEmpty()) {
            Optional<Estudiante> existente = estudianteRepository.findByNumeroDocumento(
                    dto.getNumeroDocumento().trim());
            if (existente.isPresent()) return existente.get();
        }
        return estudianteRepository.save(EstudianteEscuelaMapper.toEstudiante(dto));
    }

    private Contribuyente resolverResponsable(EstudianteEscuelaDto dto) {
        if (dto.getContribuyenteDto() == null || dto.getContribuyenteDto().getUuid() == null
                || dto.getContribuyenteDto().getUuid().trim().isEmpty()) {
            return null;
        }
        return contribuyenteRepository.findByUuid(dto.getContribuyenteDto().getUuid())
                .orElseThrow(() -> new NotFoundException(CONTRIBUYENTE,
                        dto.getContribuyenteDto().getUuid()));
    }

    private void validarInscripcionActivaDuplicada(Estudiante estudiante, Organizacion organizacion) {
        if (estudianteEscuelaRepository.existsActiveEnrollment(
                estudiante.getIdEstudiante(), organizacion.getIdOrganizacion())) {
            throw new IllegalArgumentException(
                    "El estudiante ya tiene una inscripción activa en esta escuela");
        }
    }

    private void validarPuedeVer(EstudianteEscuela inscripcion) {
        String contribuyenteUuid = contribuyenteActualUuid();
        if (contribuyenteUuid == null) return;
        boolean esTutor = inscripcion.getContribuyenteDto() != null
                && contribuyenteUuid.equals(inscripcion.getContribuyenteDto().getUuid());
        boolean gestionaEscuela = representaOrganizacion(inscripcion.getOrganizacionDto(), contribuyenteUuid);
        if (!esTutor && !gestionaEscuela) {
            throw new IllegalArgumentException("No tiene acceso a esta inscripción");
        }
    }

    private void validarPuedeGestionarOrganizacion(String organizacionUuid) {
        if (contribuyenteActualUuid() == null) return;
        Organizacion organizacion = organizacionRepository.findByUuid(organizacionUuid)
                .orElseThrow(() -> new NotFoundException(ORGANIZACION, organizacionUuid));
        validarPuedeGestionar(organizacion);
    }

    private void validarPuedeGestionar(Organizacion organizacion) {
        String contribuyenteUuid = contribuyenteActualUuid();
        if (contribuyenteUuid != null && !representaOrganizacion(organizacion, contribuyenteUuid)) {
            throw new IllegalArgumentException("No tiene autorización para administrar esta escuela");
        }
    }

    private void validarPuedeCrearInscripcion(Organizacion organizacion, Contribuyente responsable) {
        String contribuyenteUuid = contribuyenteActualUuid();
        if (contribuyenteUuid == null) return;
        boolean esResponsable = responsable != null && contribuyenteUuid.equals(responsable.getUuid());
        boolean gestionaEscuela = representaOrganizacion(organizacion, contribuyenteUuid);
        if (!esResponsable && !gestionaEscuela) {
            throw new IllegalArgumentException(
                    "Solo puede inscribir estudiantes bajo su responsabilidad o de escuelas que representa");
        }
    }

    private void validarPuedeActualizarInscripcion(EstudianteEscuela inscripcion, EstudianteEscuelaDto dto) {
        String contribuyenteUuid = contribuyenteActualUuid();
        if (contribuyenteUuid == null) return;
        boolean esResponsableActual = inscripcion.getContribuyenteDto() != null
                && contribuyenteUuid.equals(inscripcion.getContribuyenteDto().getUuid());
        boolean gestionaEscuela = representaOrganizacion(inscripcion.getOrganizacionDto(), contribuyenteUuid);
        if (!esResponsableActual && !gestionaEscuela) {
            throw new IllegalArgumentException("No tiene autorización para administrar esta inscripción");
        }
        if (dto.getContribuyenteDto() != null && dto.getContribuyenteDto().getUuid() != null
                && !gestionaEscuela && !contribuyenteUuid.equals(dto.getContribuyenteDto().getUuid())) {
            throw new IllegalArgumentException("No puede asignar otro responsable a esta inscripción");
        }
    }

    private void validarContribuyentePropio(String uuidConsultado) {
        String contribuyenteUuid = contribuyenteActualUuid();
        if (contribuyenteUuid != null && !contribuyenteUuid.equals(uuidConsultado)) {
            throw new IllegalArgumentException("No tiene acceso a los estudiantes de otro contribuyente");
        }
    }

    private boolean representaOrganizacion(Organizacion organizacion, String contribuyenteUuid) {
        return organizacion != null && organizacion.tieneRepresentante(contribuyenteUuid);
    }

    /** Returns the authenticated taxpayer UUID; staff/admin requests return null. */
    private String contribuyenteActualUuid() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof DeportesPrincipal)) {
            return null;
        }
        DeportesPrincipal principal = (DeportesPrincipal) authentication.getPrincipal();
        return "TAXPAYER".equalsIgnoreCase(principal.getUserType())
                ? principal.getSubject() : null;
    }
}
