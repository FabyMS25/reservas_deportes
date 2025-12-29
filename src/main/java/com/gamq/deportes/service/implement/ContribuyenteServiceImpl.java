package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.*;
import com.gamq.deportes.dto.mapper.*;
import com.gamq.deportes.model.*;
//import com.gamq.deportes.model.login.Login; // Make sure this import is correct
import com.gamq.deportes.model.login.login;
import com.gamq.deportes.repository.*;
import com.gamq.deportes.service.ContribuyenteService;
import com.gamq.deportes.exception.NoResultException;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

import static com.gamq.deportes.exception.enums.EntityType.*;

@Component
public class ContribuyenteServiceImpl implements ContribuyenteService {

    @Autowired
    ContribuyenteRepository contribuyentesRepository;

    @Autowired
    EstudianteEscuelaRepository estudianteEscuelaRepository;

    @Autowired
    OrganizacionRepository organizacionRepository;

    @Override
    public ContribuyenteDto findByUuid(String uuid){
        Optional<Contribuyente> contriOptional = contribuyentesRepository.findByUuid(uuid);
        if (contriOptional.isPresent()) {
            return ContribuyenteMapper.toDto(contriOptional.get(),true);
        } else {
            throw new NotFoundException(CONTRIBUYENTE , uuid);
        }
    }

    @Override
    public ContribuyenteDto findByEmail(String email){
        Optional<Contribuyente> contriOptional = contribuyentesRepository.findByEmail(email);
        if (contriOptional.isPresent()) {
            return ContribuyenteMapper.toDto(contriOptional.get(),true);
        } else {
            throw new NotFoundException(CONTRIBUYENTE , email);
        }
    }

    @Override
    public List<ContribuyenteDto> obtenerContribuyentes(){
        List<Contribuyente> contribuyentes = contribuyentesRepository.findAll();
        if (!contribuyentes.isEmpty()) {
            return contribuyentes.stream().map(contri -> {
                return ContribuyenteMapper.toDto(contri,true);
            }).collect(Collectors.toList());
        } else {
            throw new NoResultException(CONTRIBUYENTE);
        }
    }

    @Override
    @Transactional
    public ContribuyenteDto crearContribuyente(ContribuyenteDto contribuyenteDto){
        if (contribuyenteDto != null) {
            Contribuyente newContri = ContribuyenteMapper.toEntity(contribuyenteDto);
            Contribuyente contribuyenteGuardado = contribuyentesRepository.save(newContri);
            if (contribuyenteDto.getOrganizacionesRepresentadas() != null && !contribuyenteDto.getOrganizacionesRepresentadas().isEmpty()) {
                crearOrganizacionesParaContribuyente(contribuyenteGuardado, contribuyenteDto.getOrganizacionesRepresentadas());
            }
            if (contribuyenteDto.getEstudianteEnEscuelas() != null && !contribuyenteDto.getEstudianteEnEscuelas().isEmpty()) {
                crearEstudiantesParaContribuyente(contribuyenteGuardado, contribuyenteDto.getEstudianteEnEscuelas());
            }
            return ContribuyenteMapper.toDto(contribuyenteGuardado, true);
        } else {
            throw new NullReferenceException(CONTRIBUYENTE);
        }
    }

    @Override
    @Transactional
    public ContribuyenteDto actualizarContribuyente(ContribuyenteDto contribuyenteDto){
        if (contribuyenteDto != null) {
            Optional<Contribuyente> contribuyenteOptional = contribuyentesRepository.findByUuid(contribuyenteDto.getUuid());

            if (contribuyenteOptional.isPresent()) {
                Contribuyente contribuyenteExistente = contribuyenteOptional.get();
                ContribuyenteMapper.updateEntityFromDto(contribuyenteDto, contribuyenteExistente);
                Contribuyente contribuyenteActualizado = contribuyentesRepository.save(contribuyenteExistente);
                if (contribuyenteDto.getOrganizacionesRepresentadas() != null) {
                    actualizarOrganizaciones(contribuyenteActualizado, contribuyenteDto.getOrganizacionesRepresentadas());
                }
                if (contribuyenteDto.getEstudianteEnEscuelas() != null) {
                    actualizarEstudiantes(contribuyenteActualizado, contribuyenteDto.getEstudianteEnEscuelas());
                }
                Contribuyente contribuyenteCompleto = contribuyentesRepository.findByUuid(contribuyenteActualizado.getUuid())
                        .orElseThrow(() -> new NotFoundException(CONTRIBUYENTE, contribuyenteActualizado.getUuid()));
                return ContribuyenteMapper.toDto(contribuyenteCompleto, true);
            } else {
                throw new NotFoundException(CONTRIBUYENTE, contribuyenteDto.getUuid());
            }
        } else {
            throw new NullReferenceException(CONTRIBUYENTE);
        }
    }

    @Override
    public String eliminarContribuyente(String uuid){
        Contribuyente contribuyentes = contribuyentesRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(CONTRIBUYENTE, uuid));
        contribuyentesRepository.delete(contribuyentes);
        return "Eliminacion exitosa";
    }

    public ContribuyenteDto loginContribuyente(login login){

        if ((login.getUser()!= null)&&(login.getPass()!= null)) {
            Optional<Contribuyente> contribuyenteOptional = contribuyentesRepository.Login(login.getUser(), login.getPass());

            return ContribuyenteMapper.toDto(contribuyenteOptional.get(),true);
        } else {
            throw new NotFoundException(CONTRIBUYENTE, login.getUser());

        }
    }
    @Override
    @Transactional
    public String delete(String uuid) {
        Optional<Contribuyente> found = contribuyentesRepository.findByUuid(uuid);
        if (found.isPresent()) {
            Contribuyente contribuyente = found.get();
            contribuyentesRepository.delete(contribuyente);
            return "Eliminacion exitosa";
        } else {
            throw new NotFoundException(CONTRIBUYENTE, uuid);
        }
    }

    // ==================== ORGANIZACIONES MANAGEMENT ====================

    private void crearOrganizacionesParaContribuyente(Contribuyente contribuyente, List<OrganizacionDto> organizacionesDto) {
        for (OrganizacionDto organizacionDto : organizacionesDto) {
            Organizacion nuevaOrganizacion = OrganizacionMapper.toEntity(organizacionDto);
            nuevaOrganizacion.setRepresentante(contribuyente);
            organizacionRepository.save(nuevaOrganizacion);
        }
    }

    private void actualizarOrganizaciones(Contribuyente contribuyente, List<OrganizacionDto> organizacionesDto) {
        // Build map of incoming organizaciones by UUID
        Map<String, OrganizacionDto> organizacionesDtoMap = new HashMap<>();
        for (OrganizacionDto dto : organizacionesDto) {
            if (dto.getUuid() != null && !dto.getUuid().isEmpty()) {
                organizacionesDtoMap.put(dto.getUuid(), dto);
            }
        }

        // Process existing organizaciones
        List<Organizacion> organizacionesToDelete = new ArrayList<>();
        if (contribuyente.getOrganizacionesRepresentadas() != null) {
            for (Organizacion organizacionExistente : contribuyente.getOrganizacionesRepresentadas()) {
                if (organizacionesDtoMap.containsKey(organizacionExistente.getUuid())) {
                    // UPDATE existing organizacion
                    OrganizacionDto dto = organizacionesDtoMap.get(organizacionExistente.getUuid());
                    actualizarOrganizacionExistente(organizacionExistente, dto);
                    organizacionesDtoMap.remove(organizacionExistente.getUuid());
                } else {
                    // Mark for deletion (not in the incoming list)
                    organizacionesToDelete.add(organizacionExistente);
                }
            }
        }

        // Soft delete organizaciones not in the incoming list
        for (Organizacion organizacion : organizacionesToDelete) {
            organizacion.setEstado(true);
            organizacionRepository.save(organizacion);
        }

        // CREATE new organizaciones (remaining in map or without UUID)
        List<OrganizacionDto> nuevasOrganizaciones = new ArrayList<>();
        for (OrganizacionDto dto : organizacionesDto) {
            if (dto.getUuid() == null || dto.getUuid().isEmpty() || organizacionesDtoMap.containsKey(dto.getUuid())) {
                nuevasOrganizaciones.add(dto);
            }
        }
        if (!nuevasOrganizaciones.isEmpty()) {
            crearOrganizacionesParaContribuyente(contribuyente, nuevasOrganizaciones);
        }
    }

    private void actualizarOrganizacionExistente(Organizacion organizacionExistente, OrganizacionDto dto) {
        // Update all fields from DTO
        organizacionExistente.setNombre(dto.getNombre());
        organizacionExistente.setTipoOrganizacion(dto.getTipoOrganizacion());
        organizacionExistente.setPersoneriaJuridica(dto.getPersoneriaJuridica());
        organizacionExistente.setTelefono(dto.getTelefono());
        organizacionExistente.setEmail(dto.getEmail());
        organizacionExistente.setDireccion(dto.getDireccion());
        organizacionExistente.setNroNit(dto.getNroNit());
        organizacionExistente.setEntrenador(dto.getEntrenador());
        organizacionExistente.setDisciplina(dto.getDisciplina());
        organizacionExistente.setCodigoEscuela(dto.getCodigoEscuela());
        organizacionExistente.setRequierePagoMensualidad(dto.getRequierePagoMensualidad());
        organizacionExistente.setTarifaMensual(dto.getTarifaMensual());
        organizacionExistente.setPermiteReservasGratuitas(dto.getPermiteReservasGratuitas());
        organizacionExistente.setActivo(dto.getActivo());

        organizacionRepository.save(organizacionExistente);
    }

    // ==================== ESTUDIANTES MANAGEMENT ====================

    private void crearEstudiantesParaContribuyente(Contribuyente contribuyente, List<EstudianteEscuelaDto> estudiantesDto) {
        for (EstudianteEscuelaDto estudianteDto : estudiantesDto) {
            EstudianteEscuela nuevoEstudiante = EstudianteEscuelaMapper.toEntity(estudianteDto);
            nuevoEstudiante.setContribuyenteDto(contribuyente);
            estudianteEscuelaRepository.save(nuevoEstudiante);
        }
    }

    private void actualizarEstudiantes(Contribuyente contribuyente, List<EstudianteEscuelaDto> estudiantesDto) {
        // Build map of incoming estudiantes by UUID
        Map<String, EstudianteEscuelaDto> estudiantesDtoMap = new HashMap<>();
        for (EstudianteEscuelaDto dto : estudiantesDto) {
            if (dto.getUuid() != null && !dto.getUuid().isEmpty()) {
                estudiantesDtoMap.put(dto.getUuid(), dto);
            }
        }
        List<EstudianteEscuela> estudiantesToDelete = new ArrayList<>();
        if (contribuyente.getEstudianteEnEscuelas() != null) {
            for (EstudianteEscuela estudianteExistente : contribuyente.getEstudianteEnEscuelas()) {
                if (estudiantesDtoMap.containsKey(estudianteExistente.getUuid())) {
                    // UPDATE existing estudiante
                    EstudianteEscuelaDto dto = estudiantesDtoMap.get(estudianteExistente.getUuid());
                    actualizarEstudianteExistente(estudianteExistente, dto);
                    estudiantesDtoMap.remove(estudianteExistente.getUuid());
                } else {
                    // Mark for deletion (not in the incoming list)
                    estudiantesToDelete.add(estudianteExistente);
                }
            }
        }
        for (EstudianteEscuela estudiante : estudiantesToDelete) {
            estudiante.setEstado(true);
            estudianteEscuelaRepository.save(estudiante);
        }

        // CREATE new estudiantes (remaining in map or without UUID)
        List<EstudianteEscuelaDto> nuevosEstudiantes = new ArrayList<>();
        for (EstudianteEscuelaDto dto : estudiantesDto) {
            if (dto.getUuid() == null || dto.getUuid().isEmpty() || estudiantesDtoMap.containsKey(dto.getUuid())) {
                nuevosEstudiantes.add(dto);
            }
        }
        if (!nuevosEstudiantes.isEmpty()) {
            crearEstudiantesParaContribuyente(contribuyente, nuevosEstudiantes);
        }
    }

    private void actualizarEstudianteExistente(EstudianteEscuela estudianteExistente, EstudianteEscuelaDto dto) {
        estudianteExistente.setNombreCompleto(dto.getNombreCompleto());
        estudianteExistente.setFechaNacimiento(dto.getFechaNacimiento());
        estudianteExistente.setGenero(dto.getGenero());
        estudianteExistente.setNumeroDocumento(dto.getNumeroDocumento());
        estudianteExistente.setTelefono(dto.getTelefono());
        estudianteExistente.setEmail(dto.getEmail());
        estudianteExistente.setFechaInscripcion(dto.getFechaInscripcion());
        estudianteExistente.setMatricula(dto.getMatricula());
        estudianteExistente.setActivo(dto.getActivo());
        estudianteEscuelaRepository.save(estudianteExistente);
    }
}