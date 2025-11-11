package com.gamq.deportes.service;

import com.gamq.deportes.dto.EstudianteEscuelaDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface EstudianteEscuelaService {

    EstudianteEscuelaDto findByUuid(String uuid);
    EstudianteEscuelaDto findByMatricula(String matricula);
    EstudianteEscuelaDto findByNumeroDocumento(String numeroDocumento);
    List<EstudianteEscuelaDto> obtenerEstudiantes();
    List<EstudianteEscuelaDto> findByContribuyenteUuid(String uuidContribuyente);
//    List<EstudianteEscuelaDto> findByOrganizacionUuid(String uuidOrganizacion);
    List<EstudianteEscuelaDto> findByActivoTrue();
//    List<EstudianteEscuelaDto> findByActivoTrueAndOrganizacionUuid(String uuidOrganizacion);
    EstudianteEscuelaDto crearEstudiante(EstudianteEscuelaDto estudianteDto);
    EstudianteEscuelaDto actualizarEstudiante(EstudianteEscuelaDto estudianteDto);
    EstudianteEscuelaDto darDeBajaEstudiante(String uuid);
    EstudianteEscuelaDto reactivarEstudiante(String uuid);
    String eliminarEstudiante(String uuid);
//    Long contarEstudiantesActivosPorOrganizacion(String uuidOrganizacion);
}