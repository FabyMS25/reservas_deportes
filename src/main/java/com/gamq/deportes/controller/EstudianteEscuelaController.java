package com.gamq.deportes.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import com.gamq.deportes.dto.EstudianteEscuelaDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.EstudianteEscuelaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("denyAll()")
@RestController
@RequestMapping("/api/estudiante-escuela")
@Tag(name = "Estudiante de Escuela", description = "Estudiantes que estan en una escuela del GAMQ ")
public class EstudianteEscuelaController {

    @Autowired
    private EstudianteEscuelaService estudianteEscuelaService;
    @Operation(summary = "Listar Estudiantes")
    @GetMapping()
    @PreAuthorize("@permissions.has('/students','VER')")
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
    @PreAuthorize("@permissions.has('/students','VER')")
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
    @PreAuthorize("@permissions.has('/students','VER')")
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
    @PreAuthorize("@permissions.has('/students','VER')")
    public Response buscarEstudiantesPorContribuyente(@PathVariable("uuidContribuyente") String uuidContribuyente) {
        try {
            List<EstudianteEscuelaDto> estudiantes = estudianteEscuelaService.findByContribuyenteUuid(uuidContribuyente);
            return Response.ok().setPayload(estudiantes);
        } catch (Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }

    @GetMapping("/organizacion/{uuidOrganizacion}")
    @PreAuthorize("@permissions.has('/students','VER')")
    public Response buscarEstudiantesPorOrganizacion(@PathVariable("uuidOrganizacion") String uuidOrganizacion) {
        try {
            return Response.ok().setPayload(
                    estudianteEscuelaService.findByOrganizacionUuid(uuidOrganizacion));
        } catch (Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }

    @GetMapping("/organizacion/{uuidOrganizacion}/activos")
    @PreAuthorize("@permissions.has('/students','VER')")
    public Response buscarEstudiantesActivosPorOrganizacion(
            @PathVariable("uuidOrganizacion") String uuidOrganizacion) {
        try {
            return Response.ok().setPayload(
                    estudianteEscuelaService.findByActivoTrueAndOrganizacionUuid(uuidOrganizacion));
        } catch (Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
@   Operation(summary = "Buscar Estudiante por matricula")
    @GetMapping("/activos")
    @PreAuthorize("@permissions.has('/students','VER')")
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
    @PreAuthorize("@permissions.has('/students','CREAR')")
    public Response crearEstudiante(@RequestBody EstudianteEscuelaDto estudianteDto) {
        try {
            return Response.ok().setPayload(estudianteEscuelaService.crearEstudiante(estudianteDto));
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Actualizar Estudiante")
    @PutMapping()
    @PreAuthorize("@permissions.has('/students','ACTUALIZAR')")
    public Response actualizarEstudiante(@RequestBody EstudianteEscuelaDto estudianteDto) {
        try {
            return Response.ok().setPayload(estudianteEscuelaService.actualizarEstudiante(estudianteDto));
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Borrar Estudiante o dar de baja")
    @PutMapping("/{uuid}/baja")
    @PreAuthorize("@permissions.has('/students','DAR DE BAJA')")
    public Response darDeBajaEstudiante(
            @PathVariable("uuid") String uuid,
            @RequestBody(required = false) EstudianteEscuelaDto solicitud) {
        try {
            String motivo = solicitud == null ? null : solicitud.getMotivoBaja();
            return Response.ok().setPayload(estudianteEscuelaService.darDeBajaEstudiante(uuid, motivo));
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Reactivar Estudiante")
    @PutMapping("/{uuid}/reactivar")
    @PreAuthorize("@permissions.has('/students','HABILITAR')")
    public Response reactivarEstudiante(@PathVariable("uuid") String uuid) {
        try {
            return Response.ok().setPayload(estudianteEscuelaService.reactivarEstudiante(uuid));
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Quitar responsable de una inscripción sin dar de baja al estudiante")
    @PutMapping("/{uuid}/quitar-responsable")
    @PreAuthorize("@permissions.has('/students','ACTUALIZAR')")
    public Response quitarResponsable(@PathVariable("uuid") String uuid) {
        try {
            return Response.ok().setPayload(estudianteEscuelaService.quitarResponsable(uuid));
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Eliminar Estudiante")
    @DeleteMapping("/{uuid}")
    @PreAuthorize("@permissions.has('/students','ANULAR')")
    public Response eliminarEstudiante(@PathVariable("uuid") String uuid) {
        try {
            return Response.ok().setPayload(estudianteEscuelaService.eliminarEstudiante(uuid));
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
}
