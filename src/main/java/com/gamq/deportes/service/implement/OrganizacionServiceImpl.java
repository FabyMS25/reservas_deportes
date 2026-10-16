package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.OrganizacionDto;
import com.gamq.deportes.dto.OrganizacionRepresentanteDto;
import com.gamq.deportes.dto.mapper.OrganizacionMapper;
import com.gamq.deportes.model.Organizacion;
import com.gamq.deportes.repository.OrganizacionRepository;
import com.gamq.deportes.service.OrganizacionService;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrganizacionServiceImpl implements OrganizacionService {

    @Autowired
    private com.gamq.deportes.service.OrganizacionRepresentanteService representanteService;

    @Autowired
    private OrganizacionRepository organizacionRepository;

    @Override
    @Transactional(readOnly = true)
    public OrganizacionDto findByUuid(String uuid) {
        Optional<Organizacion> organizacionOptional = organizacionRepository.findByUuid(uuid);
        if (organizacionOptional.isPresent()) {
            return OrganizacionMapper.toDto(organizacionOptional.get(), true);
        } else {
            throw new NotFoundException(ORGANIZACION, uuid);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizacionDto findByNroNit(String nroNit) {
        Optional<Organizacion> organizacionOptional = organizacionRepository.findByNroNit(nroNit);
        if (organizacionOptional.isPresent()) {
            return OrganizacionMapper.toDto(organizacionOptional.get(), false);
        } else {
            throw new NotFoundException(ORGANIZACION, nroNit);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizacionDto findByEmail(String email) {
        Optional<Organizacion> organizacionOptional =  organizacionRepository.findByEmail(email);
        if (organizacionOptional.isPresent()) {
            return OrganizacionMapper.toDto(organizacionOptional.get(), false);
        } else {
            throw new NotFoundException(ORGANIZACION, email);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizacionDto> obtenerOrganizaciones() {
        List<Organizacion> organizaciones = organizacionRepository.findAll();

        if (organizaciones.isEmpty()) {
            return List.of();
        }

        return organizaciones.stream()
                .map(organizacion -> OrganizacionMapper.toDto(organizacion, true))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<OrganizacionDto> obtenerOrganizaciones(Pageable pageable) {
        return organizacionRepository.findAll(pageable)
                .map(organizacion -> OrganizacionMapper.toDto(organizacion, true));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizacionDto> findByNombreContaining(String nombre) {
        List<Organizacion> organizaciones = organizacionRepository.findByNombreContaining(nombre);

        return organizaciones.stream()
                .map(organizacion -> OrganizacionMapper.toDto(organizacion, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizacionDto> findByActivo(Boolean activo) {
        List<Organizacion> organizaciones = organizacionRepository.findByActivo(activo);

        return organizaciones.stream()
                .map(organizacion -> OrganizacionMapper.toDto(organizacion, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizacionDto> findByTipoOrganizacion(String tipoOrganizacion) {
        List<Organizacion> organizaciones = organizacionRepository.findByTipoOrganizacion(tipoOrganizacion);

        return organizaciones.stream()
                .map(organizacion -> OrganizacionMapper.toDto(organizacion, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizacionDto> findByRepresentanteUuid(String representanteUuid) {
        List<Organizacion> organizaciones = organizacionRepository.findByRepresentanteUuid(representanteUuid);

        return organizaciones.stream()
                .map(organizacion -> OrganizacionMapper.toDto(organizacion, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<OrganizacionDto> findByActivo(Boolean activo, Pageable pageable) {
        return organizacionRepository.findByActivo(activo, pageable)
                .map(organizacion -> OrganizacionMapper.toDto(organizacion, false));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrganizacionDto> findByNombreContaining(String nombre, Pageable pageable) {
        return organizacionRepository.findByNombreContaining(nombre, pageable)
                .map(organizacion -> OrganizacionMapper.toDto(organizacion, false));
    }

    @Override
    @Transactional
    public OrganizacionDto crearOrganizacion(OrganizacionDto organizacionDto) {
        if (organizacionDto == null) {
            throw new NullReferenceException(ORGANIZACION);
        }

        // Validate NIT uniqueness
        if (organizacionDto.getNroNit() != null &&
                organizacionRepository.existsByNroNit(organizacionDto.getNroNit())) {
            throw new IllegalArgumentException("Ya existe una organización con el NIT: " + organizacionDto.getNroNit());
        }

        // Validate email uniqueness
        if (organizacionDto.getEmail() != null &&
                organizacionRepository.existsByEmail(organizacionDto.getEmail())) {
            throw new IllegalArgumentException("Ya existe una organización con el email: " + organizacionDto.getEmail());
        }

        List<OrganizacionRepresentanteDto> representantes = representantesSolicitados(organizacionDto);
        if (representantes.isEmpty()) {
            throw new IllegalArgumentException("Agregue al menos un representante");
        }

        Organizacion nuevaOrganizacion = OrganizacionMapper.toEntity(organizacionDto);

        // Set default values
        if (nuevaOrganizacion.getActivo() == null) {
            nuevaOrganizacion.setActivo(true);
        }

        Organizacion organizacionGuardada = organizacionRepository.save(nuevaOrganizacion);
        representanteService.guardarRepresentantes(organizacionGuardada, representantes);
        return OrganizacionMapper.toDto(organizacionGuardada, true);
    }

    @Override
    @Transactional
    public OrganizacionDto actualizarOrganizacion(OrganizacionDto organizacionDto) {
        if (organizacionDto == null) {
            throw new NullReferenceException(ORGANIZACION);
        }

        Optional<Organizacion> organizacionOptional = organizacionRepository.findByUuid(organizacionDto.getUuid());

        if (organizacionOptional.isPresent()) {
            Organizacion organizacionExistente = organizacionOptional.get();

            // Validate NIT uniqueness (exclude current organization)
            if (organizacionDto.getNroNit() != null &&
                    !organizacionDto.getNroNit().equals(organizacionExistente.getNroNit()) &&
                    organizacionRepository.existsByNroNit(organizacionDto.getNroNit())) {
                throw new IllegalArgumentException("Ya existe una organización con el NIT: " + organizacionDto.getNroNit());
            }

            // Validate email uniqueness (exclude current organization)
            if (organizacionDto.getEmail() != null &&
                    !organizacionDto.getEmail().equals(organizacionExistente.getEmail()) &&
                    organizacionRepository.existsByEmail(organizacionDto.getEmail())) {
                throw new IllegalArgumentException("Ya existe una organización con el email: " + organizacionDto.getEmail());
            }
            List<OrganizacionRepresentanteDto> representantes = representantesSolicitados(organizacionDto);
            if (organizacionDto.getRepresentantesDto() != null && representantes.isEmpty()) {
                throw new IllegalArgumentException("Agregue al menos un representante");
            }

            // Update fields
            OrganizacionMapper.updateEntityFromDto(organizacionDto, organizacionExistente);

            Organizacion organizacionActualizada = organizacionRepository.save(organizacionExistente);
            if (organizacionDto.getRepresentantesDto() != null) {
                representanteService.guardarRepresentantes(organizacionActualizada, representantes);
            }
            return OrganizacionMapper.toDto(organizacionActualizada, true);
        } else {
            throw new NotFoundException(ORGANIZACION, organizacionDto.getUuid());
        }
    }

    @Override
    @Transactional
    public OrganizacionDto activarOrganizacion(String uuid) {
        Organizacion organizacion = organizacionRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(ORGANIZACION, uuid));

        organizacion.setActivo(true);

        Organizacion organizacionActualizada = organizacionRepository.save(organizacion);
        return OrganizacionMapper.toDto(organizacionActualizada, true);
    }

    @Override
    @Transactional
    public OrganizacionDto desactivarOrganizacion(String uuid) {
        Organizacion organizacion = organizacionRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(ORGANIZACION, uuid));

        organizacion.setActivo(false);

        Organizacion organizacionActualizada = organizacionRepository.save(organizacion);
        return OrganizacionMapper.toDto(organizacionActualizada, true);
    }

    @Override
    @Transactional
    public String eliminarOrganizacion(String uuid) {
        try {
        Organizacion organizacion = organizacionRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(ORGANIZACION, uuid));

        representanteService.eliminarPorOrganizacion(organizacion);
        organizacionRepository.delete(organizacion);
            return "Desactivación exitosa del ORGANIZACION";
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al intentar desactivar el RESERVA. Por favor, inténtelo de nuevo más tarde.", e);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return organizacionRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countByActivo(Boolean activo) {
        return organizacionRepository.countByActivo(activo);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countByTipoOrganizacion(String tipoOrganizacion) {
        return organizacionRepository.countByTipoOrganizacion(tipoOrganizacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getEstadisticasPorTipo() {
        return organizacionRepository.countByTipoOrganizacionGroup();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getEstadisticasPorActivo() {
        return organizacionRepository.countByActivoGroup();
    }

    private List<OrganizacionRepresentanteDto> representantesSolicitados(OrganizacionDto dto) {
        if (dto.getRepresentantesDto() != null) {
            return dto.getRepresentantesDto();
        }
        return java.util.Collections.emptyList();
    }
}
