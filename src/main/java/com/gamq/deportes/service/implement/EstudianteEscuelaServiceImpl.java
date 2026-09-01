package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.EstudianteEscuelaDto;
import com.gamq.deportes.dto.mapper.EstudianteEscuelaMapper;
import com.gamq.deportes.model.Contribuyente;
import com.gamq.deportes.model.EstudianteEscuela;
import com.gamq.deportes.model.Organizacion;
import com.gamq.deportes.repository.ContribuyenteRepository;
import com.gamq.deportes.repository.EstudianteEscuelaRepository;
import com.gamq.deportes.repository.OrganizacionRepository;
import com.gamq.deportes.service.EstudianteEscuelaService;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
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
    private ContribuyenteRepository contribuyenteRepository;

    @Autowired
    private OrganizacionRepository organizacionRepository;

    @Override
    public EstudianteEscuelaDto findByUuid(String uuid) {
        Optional<EstudianteEscuela> estudianteOptional = estudianteEscuelaRepository.findByUuid(uuid);
        if (estudianteOptional.isPresent()) {
            return EstudianteEscuelaMapper.toDto(estudianteOptional.get(), true);
        } else {
            throw new NotFoundException(ESTUDIANTE_ESCUELA, uuid);
        }
    }

    @Override
    public EstudianteEscuelaDto findByMatricula(String matricula) {
        Optional<EstudianteEscuela> estudianteOptional = estudianteEscuelaRepository.findByMatricula(matricula);
        if (estudianteOptional.isPresent()) {
            return EstudianteEscuelaMapper.toDto(estudianteOptional.get(), true);
        } else {
            throw new NotFoundException(ESTUDIANTE_ESCUELA, matricula);
        }
    }

    @Override
    public EstudianteEscuelaDto findByNumeroDocumento(String numeroDocumento) {
        Optional<EstudianteEscuela> estudianteOptional = estudianteEscuelaRepository.findByNumeroDocumento(numeroDocumento);
        if (estudianteOptional.isPresent()) {
            return EstudianteEscuelaMapper.toDto(estudianteOptional.get(), true);
        } else {
            throw new NotFoundException(ESTUDIANTE_ESCUELA, numeroDocumento);
        }
    }

    @Override
    public List<EstudianteEscuelaDto> obtenerEstudiantes() {
        List<EstudianteEscuela> estudiantes = estudianteEscuelaRepository.findAll();
        return estudiantes.stream()
                .map(estudiante -> EstudianteEscuelaMapper.toDto(estudiante, true))
                .collect(Collectors.toList());
    }

    @Override
    public List<EstudianteEscuelaDto> findByContribuyenteUuid(String uuidContribuyente) {
        List<EstudianteEscuela> estudiantes = estudianteEscuelaRepository.findByContribuyenteUuid(uuidContribuyente);
        return estudiantes.stream()
                .map(estudiante -> EstudianteEscuelaMapper.toDto(estudiante, false))
                .collect(Collectors.toList());
    }

//    @Override
//    public List<EstudianteEscuelaDto> findByOrganizacionUuid(String uuidOrganizacion) {
//        List<EstudianteEscuela> estudiantes = estudianteEscuelaRepository.findByOrganizacionUuid(uuidOrganizacion);
//        if (!estudiantes.isEmpty()) {
//            return estudiantes.stream()
//                    .map(estudiante -> EstudianteEscuelaMapper.toDto(estudiante, false))
//                    .collect(Collectors.toList());
//        } else {
//            throw new NoResultException(ESTUDIANTE_ESCUELA);
//        }
//    }

    @Override
    public List<EstudianteEscuelaDto> findByActivoTrue() {
        List<EstudianteEscuela> estudiantes = estudianteEscuelaRepository.findByActivoTrue();
        return estudiantes.stream()
                .map(estudiante -> EstudianteEscuelaMapper.toDto(estudiante, false))
                .collect(Collectors.toList());
    }

//    @Override
//    public List<EstudianteEscuelaDto> findByActivoTrueAndOrganizacionUuid(String uuidOrganizacion) {
//        List<EstudianteEscuela> estudiantes = estudianteEscuelaRepository.findByActivoTrueAndOrganizacionUuid(uuidOrganizacion);
//        if (!estudiantes.isEmpty()) {
//            return estudiantes.stream()
//                    .map(estudiante -> EstudianteEscuelaMapper.toDto(estudiante, false))
//                    .collect(Collectors.toList());
//        } else {
//            throw new NoResultException(ESTUDIANTE_ESCUELA);
//        }
//    }

    @Override
    @Transactional
    public EstudianteEscuelaDto crearEstudiante(EstudianteEscuelaDto estudianteDto) {
        if (estudianteDto != null) {
            // Verify contribuyente exists
            Contribuyente contribuyente = contribuyenteRepository.findByUuid(
                            estudianteDto.getContribuyenteDto().getUuid())
                    .orElseThrow(() -> new NotFoundException(CONTRIBUYENTE,
                            estudianteDto.getContribuyenteDto().getUuid()));

            // Verify organizacion exists (you might need to add this to your DTO or get it from context)
            // For now, we'll assume it's passed in the DTO or set elsewhere
            // Organizacion organizacion = organizacionRepository.findByUuid(...)

            EstudianteEscuela nuevoEstudiante = EstudianteEscuelaMapper.toEntity(estudianteDto);
            nuevoEstudiante.setContribuyenteDto(contribuyente);
            // nuevoEstudiante.setOrganizacionDto(organizacion); // Set organizacion when available

            return EstudianteEscuelaMapper.toDto(estudianteEscuelaRepository.save(nuevoEstudiante), true);
        } else {
            throw new NullReferenceException(ESTUDIANTE_ESCUELA);
        }
    }

    @Override
    @Transactional
    public EstudianteEscuelaDto actualizarEstudiante(EstudianteEscuelaDto estudianteDto) {
        if (estudianteDto != null) {
            Optional<EstudianteEscuela> estudianteOptional = estudianteEscuelaRepository.findByUuid(estudianteDto.getUuid());

            if (estudianteOptional.isPresent()) {
                EstudianteEscuela estudianteExistente = estudianteOptional.get();
                EstudianteEscuelaMapper.updateEntityFromDto(estudianteDto, estudianteExistente);

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
    public EstudianteEscuelaDto darDeBajaEstudiante(String uuid) {
        Optional<EstudianteEscuela> estudianteOptional = estudianteEscuelaRepository.findByUuid(uuid);
        if (estudianteOptional.isPresent()) {
            EstudianteEscuela estudiante = estudianteOptional.get();
            estudiante.darDeBaja();
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
            EstudianteEscuela estudiante = estudianteOptional.get();
            estudiante.reactivar();
            return EstudianteEscuelaMapper.toDto(estudianteEscuelaRepository.save(estudiante), true);
        } else {
            throw new NotFoundException(ESTUDIANTE_ESCUELA, uuid);
        }
    }

    @Override
    @Transactional
    public String eliminarEstudiante(String uuid) {
        EstudianteEscuela estudiante = estudianteEscuelaRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(ESTUDIANTE_ESCUELA, uuid));
        estudianteEscuelaRepository.delete(estudiante);
        return "Eliminación exitosa del estudiante con UUID: " + uuid;
    }

//    @Override
//    public Long contarEstudiantesActivosPorOrganizacion(String uuidOrganizacion) {
//        return estudianteEscuelaRepository.countByOrganizacionUuidAndActivoTrue(uuidOrganizacion);
//    }
}