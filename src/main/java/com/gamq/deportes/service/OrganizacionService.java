package com.gamq.deportes.service;

import com.gamq.deportes.dto.OrganizacionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface OrganizacionService {

    OrganizacionDto findByUuid(String uuid);
    OrganizacionDto findByNroNit(String nroNit);
    OrganizacionDto findByEmail(String email);

    List<OrganizacionDto> obtenerOrganizaciones();
    Page<OrganizacionDto> obtenerOrganizaciones(Pageable pageable);
    List<OrganizacionDto> findByNombreContaining(String nombre);
    List<OrganizacionDto> findByActivo(Boolean activo);
    List<OrganizacionDto> findByTipoOrganizacion(String tipoOrganizacion);
    List<OrganizacionDto> findByRepresentanteUuid(String representanteUuid);
    Page<OrganizacionDto> findByActivo(Boolean activo, Pageable pageable);
    Page<OrganizacionDto> findByNombreContaining(String nombre, org.springframework.data.domain.Pageable pageable);
    OrganizacionDto crearOrganizacion(OrganizacionDto organizacionDto);
    OrganizacionDto actualizarOrganizacion(OrganizacionDto organizacionDto);
    OrganizacionDto activarOrganizacion(String uuid);
    OrganizacionDto desactivarOrganizacion(String uuid);
    String eliminarOrganizacion(String uuid);

    boolean existsByEmail(String email);
    Long countByActivo(Boolean activo);
    Long countByTipoOrganizacion(String tipoOrganizacion);
    List<Object[]> getEstadisticasPorTipo();
    List<Object[]> getEstadisticasPorActivo();
}
