package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.*;
import com.gamq.deportes.dto.mapper.*;
import com.gamq.deportes.model.*;
//import com.gamq.deportes.model.login.Login; // Make sure this import is correct
import com.gamq.deportes.model.login.login;
import com.gamq.deportes.repository.*;
import com.gamq.deportes.service.ContribuyenteService;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import com.gamq.deportes.exception.DuplicateTaxpayerException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;

import static com.gamq.deportes.exception.enums.EntityType.*;

@Component
public class ContribuyenteServiceImpl implements ContribuyenteService {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    ContribuyenteRepository contribuyentesRepository;

    @Autowired
    EstudianteEscuelaRepository estudianteEscuelaRepository;

    @Autowired
    EstudianteRepository estudianteRepository;

    @Autowired
    OrganizacionRepository organizacionRepository;

    @Autowired
    EntityManager entityManager;

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
        return contribuyentes.stream().map(contri -> {
            return ContribuyenteMapper.toDto(contri,true);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ContribuyenteDto crearContribuyente(ContribuyenteDto contribuyenteDto){
        if (contribuyenteDto != null) {
            validateRequiredRegistrationFields(contribuyenteDto);
            validateUniqueRegistrationFields(contribuyenteDto);
            contribuyenteDto.setPassword(encodePassword(contribuyenteDto.getPassword()));
            Contribuyente newContri = ContribuyenteMapper.toEntity(contribuyenteDto);
            Contribuyente contribuyenteGuardado;
            try {
                contribuyenteGuardado = contribuyentesRepository.saveAndFlush(newContri);
            } catch (DataIntegrityViolationException ex) {
                throw new DuplicateTaxpayerException(Collections.singletonList(
                        "alguno de sus datos de identificación"));
            }
            if (contribuyenteDto.getEstudianteEnEscuelas() != null) {
                crearEstudiantesParaContribuyente(contribuyenteGuardado,
                        contribuyenteDto.getEstudianteEnEscuelas());
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
                if (contribuyenteDto.getPassword() != null && !contribuyenteDto.getPassword().isBlank()) {
                    contribuyenteDto.setPassword(encodePassword(contribuyenteDto.getPassword()));
                }
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
        if (login == null || login.getUser() == null || login.getPass() == null) {
            throw new NotFoundException(CONTRIBUYENTE, login == null ? null : login.getUser());
        }

        Contribuyente contribuyente = contribuyentesRepository.findByUserName(login.getUser())
                .orElseThrow(() -> new NotFoundException(CONTRIBUYENTE, login.getUser()));

        String storedPassword = contribuyente.getPassword();
        boolean validPassword = isBcryptHash(storedPassword)
                ? passwordEncoder.matches(login.getPass(), storedPassword)
                : login.getPass().equals(storedPassword);

        if (!validPassword) {
            throw new NotFoundException(CONTRIBUYENTE, login.getUser());
        }

        // Transparently migrate legacy plaintext passwords after a successful login.
        if (!isBcryptHash(storedPassword)) {
            contribuyente.setPassword(passwordEncoder.encode(login.getPass()));
            contribuyentesRepository.save(contribuyente);
        }

        return ContribuyenteMapper.toDto(contribuyente, true);
    }

    @Override
    public boolean existsByNumeroDocumento(String numeroDocumento) {
        return hasText(numeroDocumento) && contribuyentesRepository.existsAnyByNumeroDocumento(numeroDocumento.trim());
    }

    @Override
    public boolean existsByEmail(String email) {
        return hasText(email) && contribuyentesRepository.existsAnyByEmail(email.trim());
    }

    @Override
    public boolean existsByUserName(String userName) {
        return hasText(userName) && contribuyentesRepository.existsAnyByUserName(userName.trim());
    }

    private void validateRequiredRegistrationFields(ContribuyenteDto dto) {
        List<String> missing = new ArrayList<>();
        if (!hasText(dto.getUserName())) missing.add("usuario");
        if (!hasText(dto.getPassword())) missing.add("contraseña");
        if (!hasText(dto.getTipoContribuyente())) missing.add("tipo de contribuyente");
        if (!hasText(dto.getTipoDocumento())) missing.add("tipo de documento");
        if (!hasText(dto.getNumeroDocumento())) missing.add("número de documento");
        if (!hasText(dto.getNombre())) missing.add("nombre");
        if (!hasText(dto.getPrimerApellido())) missing.add("primer apellido");
        if (!hasText(dto.getEmail())) missing.add("correo electrónico");
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Faltan campos requeridos: " + String.join(", ", missing) + ".");
        }
    }

    private void validateUniqueRegistrationFields(ContribuyenteDto dto) {
        List<String> duplicates = new ArrayList<>();
        if (contribuyentesRepository.existsAnyByNumeroDocumento(dto.getNumeroDocumento().trim()))
            duplicates.add("el número de documento " + dto.getNumeroDocumento().trim());
        if (contribuyentesRepository.existsAnyByUserName(dto.getUserName().trim()))
            duplicates.add("el usuario " + dto.getUserName().trim());
        if (contribuyentesRepository.existsAnyByEmail(dto.getEmail().trim()))
            duplicates.add("el correo " + dto.getEmail().trim());
        if (hasText(dto.getCodigoContribuyente())
                && contribuyentesRepository.existsAnyByCodigoContribuyente(dto.getCodigoContribuyente().trim()))
            duplicates.add("el código RUAT " + dto.getCodigoContribuyente().trim());
        if (hasText(dto.getNroNit()) && contribuyentesRepository.existsAnyByNroNit(dto.getNroNit().trim()))
            duplicates.add("el NIT " + dto.getNroNit().trim());
        if (!duplicates.isEmpty()) throw new DuplicateTaxpayerException(duplicates);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String encodePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        return isBcryptHash(rawPassword) ? rawPassword : passwordEncoder.encode(rawPassword);
    }

    private boolean isBcryptHash(String password) {
        return password != null && password.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
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
            if (estudianteDto.getOrganizacionDto() == null
                    || estudianteDto.getOrganizacionDto().getUuid() == null) {
                throw new IllegalArgumentException("La escuela es requerida para cada inscripción");
            }
            Organizacion organizacion = organizacionRepository.findByUuid(
                            estudianteDto.getOrganizacionDto().getUuid())
                    .orElseThrow(() -> new NotFoundException(ORGANIZACION,
                            estudianteDto.getOrganizacionDto().getUuid()));
            EstudianteEscuela nuevoEstudiante = EstudianteEscuelaMapper.toEntity(estudianteDto);
            nuevoEstudiante.setContribuyenteDto(contribuyente);
            nuevoEstudiante.setOrganizacionDto(organizacion);
            Estudiante persona = resolverPersonaEstudiante(estudianteDto);
            nuevoEstudiante.setEstudiante(persona);
            estudianteEscuelaRepository.saveAndFlush(nuevoEstudiante);
            entityManager.refresh(nuevoEstudiante);
            contribuyente.getEstudianteEnEscuelas().add(nuevoEstudiante);
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
        List<EstudianteEscuela> inscripcionesSinResponsable = new ArrayList<>();
        if (contribuyente.getEstudianteEnEscuelas() != null) {
            for (EstudianteEscuela estudianteExistente : contribuyente.getEstudianteEnEscuelas()) {
                if (estudiantesDtoMap.containsKey(estudianteExistente.getUuid())) {
                    // UPDATE existing estudiante
                    EstudianteEscuelaDto dto = estudiantesDtoMap.get(estudianteExistente.getUuid());
                    actualizarEstudianteExistente(estudianteExistente, dto);
                    estudiantesDtoMap.remove(estudianteExistente.getUuid());
                } else {
                    // Removing it from a contributor never deletes the student
                    // or closes the school enrollment; it only ends this
                    // operational responsibility.
                    inscripcionesSinResponsable.add(estudianteExistente);
                }
            }
        }
        for (EstudianteEscuela inscripcion : inscripcionesSinResponsable) {
            inscripcion.setContribuyenteDto(null);
            estudianteEscuelaRepository.save(inscripcion);
        }
        if (contribuyente.getEstudianteEnEscuelas() != null) {
            contribuyente.getEstudianteEnEscuelas().removeAll(inscripcionesSinResponsable);
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
        EstudianteEscuelaMapper.updateEntityFromDto(dto, estudianteExistente);
        if (dto.getOrganizacionDto() != null && dto.getOrganizacionDto().getUuid() != null) {
            if (!estudianteExistente.getOrganizacionDto().getUuid()
                    .equals(dto.getOrganizacionDto().getUuid())) {
                throw new IllegalArgumentException(
                        "No se puede cambiar la escuela de una inscripción. Dé de baja esta inscripción y cree otra");
            }
        }
        estudianteRepository.save(estudianteExistente.getEstudiante());
        estudianteEscuelaRepository.save(estudianteExistente);
    }

    private Estudiante resolverPersonaEstudiante(EstudianteEscuelaDto dto) {
        if (dto.getEstudianteUuid() != null && !dto.getEstudianteUuid().trim().isEmpty()) {
            return estudianteRepository.findByUuid(dto.getEstudianteUuid())
                    .orElseThrow(() -> new IllegalArgumentException("No existe el estudiante indicado"));
        }
        if (dto.getNumeroDocumento() != null && !dto.getNumeroDocumento().trim().isEmpty()) {
            Optional<Estudiante> existente = estudianteRepository.findByNumeroDocumento(
                    dto.getNumeroDocumento().trim());
            if (existente.isPresent()) return existente.get();
        }
        if (dto.getNombreCompleto() == null || dto.getNombreCompleto().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del estudiante es requerido");
        }
        return estudianteRepository.save(EstudianteEscuelaMapper.toEstudiante(dto));
    }
}
