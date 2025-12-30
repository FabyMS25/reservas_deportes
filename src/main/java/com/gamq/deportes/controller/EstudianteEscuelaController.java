package com.gamq.deportes.controller;

import com.gamq.deportes.dto.EstudianteEscuelaDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.EstudianteEscuelaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estudiante-escuela")
@Tag(name = "Estudiante de Escuela", description = "Estudiantes que estan en una escuela del GAMQ ")
public class EstudianteEscuelaController {

    @Autowired
    private EstudianteEscuelaService estudianteEscuelaService;
    @Operation(summary = "Listar Estudiantes")
    @GetMapping()
    public Response obtenerEstudiantes() {
        try {
            List<EstudianteEscuelaDto> estudianteDtos = estudianteEscuelaService.obtenerEstudiantes();
            return Response.ok().setPayload(estudianteDtos);
        } catch (Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Buscar Estudiante por uuid")
    @GetMapping("/{uuid}")
    public Response buscarEstudiantePorUuid(@PathVariable("uuid") String uuidEstudiante) {
        try {
            EstudianteEscuelaDto estudianteDto = estudianteEscuelaService.findByUuid(uuidEstudiante);
            return Response.ok().setPayload(estudianteDto);
        } catch (Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Buscar Estudiante por matricula")
    @GetMapping("/matricula/{matricula}")
    public Response buscarEstudiantePorMatricula(@PathVariable("matricula") String matricula) {
        try {
            EstudianteEscuelaDto estudianteDto = estudianteEscuelaService.findByMatricula(matricula);
            return Response.ok().setPayload(estudianteDto);
        } catch (Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Buscar Estudiante por UUID  de Contribuyente")
    @GetMapping("/contribuyente/{uuidContribuyente}")
    public Response buscarEstudiantesPorContribuyente(@PathVariable("uuidContribuyente") String uuidContribuyente) {
        try {
            List<EstudianteEscuelaDto> estudiantes = estudianteEscuelaService.findByContribuyenteUuid(uuidContribuyente);
            return Response.ok().setPayload(estudiantes);
        } catch (Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }

//    @GetMapping("/organizacion/{uuidOrganizacion}")
//    public Response buscarEstudiantesPorOrganizacion(@PathVariable("uuidOrganizacion") String uuidOrganizacion) {
//        try {
//            List<EstudianteEscuelaDto> estudiantes = estudianteEscuelaService.findByOrganizacionUuid(uuidOrganizacion);
//            return Response.ok().setPayload(estudiantes);
//        } catch (Exception ex) {
//            return Response.notFound().setPayload(ex.getMessage());
//        }
//    }
@   Operation(summary = "Buscar Estudiante por matricula")
    @GetMapping("/activos")
    public Response obtenerEstudiantesActivos() {
        try {
            List<EstudianteEscuelaDto> estudiantes = estudianteEscuelaService.findByActivoTrue();
            return Response.ok().setPayload(estudiantes);
        } catch (Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Crear Estudiante")
    @PostMapping()
    public Response crearEstudiante(@RequestBody EstudianteEscuelaDto estudianteDto) {
        try {
            return Response.ok().setPayload(estudianteEscuelaService.crearEstudiante(estudianteDto));
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Actualizar Estudiante")
    @PutMapping()
    public Response actualizarEstudiante(@RequestBody EstudianteEscuelaDto estudianteDto) {
        try {
            return Response.ok().setPayload(estudianteEscuelaService.actualizarEstudiante(estudianteDto));
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Borrar Estudiante o dar de baja")
    @PutMapping("/{uuid}/baja")
    public Response darDeBajaEstudiante(@PathVariable("uuid") String uuid) {
        try {
            return Response.ok().setPayload(estudianteEscuelaService.darDeBajaEstudiante(uuid));
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Reactivar Estudiante")
    @PutMapping("/{uuid}/reactivar")
    public Response reactivarEstudiante(@PathVariable("uuid") String uuid) {
        try {
            return Response.ok().setPayload(estudianteEscuelaService.reactivarEstudiante(uuid));
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Eliminar Estudiante")
    @DeleteMapping("/{uuid}")
    public Response eliminarEstudiante(@PathVariable("uuid") String uuid) {
        try {
            return Response.ok().setPayload(estudianteEscuelaService.eliminarEstudiante(uuid));
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
}