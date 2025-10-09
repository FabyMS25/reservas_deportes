package com.gamq.deportes.controller;

import com.gamq.deportes.dto.OrganizacionDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.OrganizacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizaciones")
@Tag(name = "Organizaciones", description = "API para gestión de organizaciones")
public class OrganizacionController {

    @Autowired
    private OrganizacionService organizacionService;

    @Operation(summary = "Obtener todas las organizaciones")
    @GetMapping()
    public Response obtenerOrganizaciones(
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size) {
        try {
            if (page >= 0 && size > 0) {
                Pageable pageable = PageRequest.of(page, size);
                Page<OrganizacionDto> organizacionesPage = organizacionService.obtenerOrganizaciones(pageable);
                return Response.ok().setPayload(organizacionesPage);
            } else {
                List<OrganizacionDto> organizacionesDtos = organizacionService.obtenerOrganizaciones();
                return Response.ok().setPayload(organizacionesDtos);
            }
        } catch (Exception ex) {
            return Response.exception().setPayload("Error al obtener organizaciones: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar organización por UUID")
    @GetMapping("/{uuid}")
    public Response buscarOrganizacionPorUuid(
            @Parameter(description = "UUID de la organización")
            @PathVariable("uuid") String uuid) {
        try {
            OrganizacionDto organizacionDto = organizacionService.findByUuid(uuid);
            return Response.ok().setPayload(organizacionDto);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Organización no encontrada: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar organización por NIT")
    @GetMapping("/nit/{nroNit}")
    public Response buscarOrganizacionPorNit(
            @Parameter(description = "Número de NIT")
            @PathVariable("nroNit") String nroNit) {
        try {
            OrganizacionDto organizacionDto = organizacionService.findByNroNit(nroNit);
            return Response.ok().setPayload(organizacionDto);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Organización no encontrada: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar organización por email")
    @GetMapping("/email/{email}")
    public Response buscarOrganizacionPorEmail(
            @Parameter(description = "Email de la organización")
            @PathVariable("email") String email) {
        try {
            OrganizacionDto organizacionDto = organizacionService.findByEmail(email);
            return Response.ok().setPayload(organizacionDto);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Organización no encontrada: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar organizaciones por nombre")
    @GetMapping("/nombre/{nombre}")
    public Response buscarOrganizacionesPorNombre(
            @Parameter(description = "Nombre o parte del nombre")
            @PathVariable("nombre") String nombre) {
        try {
            List<OrganizacionDto> organizacionesDtos = organizacionService.findByNombreContaining(nombre);
            return Response.ok().setPayload(organizacionesDtos);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar organizaciones por nombre: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar organizaciones por tipo")
    @GetMapping("/tipo/{tipoOrganizacion}")
    public Response buscarOrganizacionesPorTipo(
            @Parameter(description = "Tipo de organización")
            @PathVariable("tipoOrganizacion") String tipoOrganizacion) {
        try {
            List<OrganizacionDto> organizacionesDtos = organizacionService.findByTipoOrganizacion(tipoOrganizacion);
            return Response.ok().setPayload(organizacionesDtos);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar organizaciones por tipo: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar organizaciones por estado activo")
    @GetMapping("/activas/{activo}")
    public Response buscarOrganizacionesPorActivo(
            @Parameter(description = "Estado activo (true/false)")
            @PathVariable("activo") Boolean activo) {
        try {
            List<OrganizacionDto> organizacionesDtos = organizacionService.findByActivo(activo);
            return Response.ok().setPayload(organizacionesDtos);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar organizaciones por estado activo: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar organizaciones por representante")
    @GetMapping("/representante/{representanteUuid}")
    public Response buscarOrganizacionesPorRepresentante(
            @Parameter(description = "UUID del representante")
            @PathVariable("representanteUuid") String representanteUuid) {
        try {
            List<OrganizacionDto> organizacionesDtos = organizacionService.findByRepresentanteUuid(representanteUuid);
            return Response.ok().setPayload(organizacionesDtos);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar organizaciones por representante: " + ex.getMessage());
        }
    }

//    @Operation(summary = "Verificar si existe organización por NIT")
//    @GetMapping("/existe/nit/{nroNit}")
//    public Response verificarExistenciaPorNit(
//            @Parameter(description = "Número de NIT")
//            @PathVariable("nroNit") String nroNit) {
//        try {
//            boolean existe = organizacionService.existsByNroNit(nroNit);
//            return Response.ok().setPayload(existe);
//        } catch (Exception ex) {
//            return Response.serverError().setPayload("Error al verificar existencia: " + ex.getMessage());
//        }
//    }

    @Operation(summary = "Verificar si existe organización por email")
    @GetMapping("/existe/email/{email}")
    public Response verificarExistenciaPorEmail(
            @Parameter(description = "Email de la organización")
            @PathVariable("email") String email) {
        try {
            boolean existe = organizacionService.existsByEmail(email);
            return Response.ok().setPayload(existe);
        } catch (Exception ex) {
            return Response.exception().setPayload("Error al verificar existencia: " + ex.getMessage());
        }
    }

    @Operation(summary = "Crear nueva organización")
    @PostMapping()
    public Response crearOrganizacion(@RequestBody OrganizacionDto organizacionDto) {
        try {
            OrganizacionDto nuevaOrganizacion = organizacionService.crearOrganizacion(organizacionDto);
            return Response.ok().setPayload(nuevaOrganizacion);
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al crear organización: " + ex.getMessage());
        }
    }

    @Operation(summary = "Actualizar organización existente")
    @PutMapping()
    public Response actualizarOrganizacion(@RequestBody OrganizacionDto organizacionDto) {
        try {
            OrganizacionDto organizacionActualizada = organizacionService.actualizarOrganizacion(organizacionDto);
            return Response.ok().setPayload(organizacionActualizada);
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al actualizar organización: " + ex.getMessage());
        }
    }

    @Operation(summary = "Activar organización")
    @PutMapping("/{uuid}/activar")
    public Response activarOrganizacion(
            @Parameter(description = "UUID de la organización")
            @PathVariable("uuid") String uuid) {
        try {
            OrganizacionDto organizacionActivada = organizacionService.activarOrganizacion(uuid);
            return Response.ok().setPayload(organizacionActivada);
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al activar organización: " + ex.getMessage());
        }
    }

    @Operation(summary = "Desactivar organización")
    @PutMapping("/{uuid}/desactivar")
    public Response desactivarOrganizacion(
            @Parameter(description = "UUID de la organización")
            @PathVariable("uuid") String uuid) {
        try {
            OrganizacionDto organizacionDesactivada = organizacionService.desactivarOrganizacion(uuid);
            return Response.ok().setPayload(organizacionDesactivada);
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al desactivar organización: " + ex.getMessage());
        }
    }

    @Operation(summary = "Eliminar organización")
    @DeleteMapping("/{uuid}")
    public Response eliminarOrganizacion(
            @Parameter(description = "UUID de la organización")
            @PathVariable("uuid") String uuid) {
        try {
            organizacionService.eliminarOrganizacion(uuid);
            return Response.ok().setPayload("Organización eliminada exitosamente");
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al eliminar organización: " + ex.getMessage());
        }
    }

//    @Operation(summary = "Contar organizaciones por estado activo")
//    @GetMapping("/contador/activas/{activo}")
//    public Response contarOrganizacionesPorActivo(
//            @Parameter(description = "Estado activo (true/false)")
//            @PathVariable("activo") Boolean activo) {
//        try {
//            Long cantidad = organizacionService.countByActivo(activo);
//            return Response.ok().setPayload(cantidad);
//        } catch (Exception ex) {
//            return Response.serverError().setPayload("Error al contar organizaciones: " + ex.getMessage());
//        }
//    }
//
//    @Operation(summary = "Contar organizaciones por tipo")
//    @GetMapping("/contador/tipo/{tipoOrganizacion}")
//    public Response contarOrganizacionesPorTipo(
//            @Parameter(description = "Tipo de organización")
//            @PathVariable("tipoOrganizacion") String tipoOrganizacion) {
//        try {
//            Long cantidad = organizacionService.countByTipoOrganizacion(tipoOrganizacion);
//            return Response.ok().setPayload(cantidad);
//        } catch (Exception ex) {
//            return Response.serverError().setPayload("Error al contar organizaciones: " + ex.getMessage());
//        }
//    }

    @Operation(summary = "Obtener estadísticas por tipo de organización")
    @GetMapping("/estadisticas/tipo")
    public Response obtenerEstadisticasPorTipo() {
        try {
            List<Object[]> estadisticas = organizacionService.getEstadisticasPorTipo();
            return Response.ok().setPayload(estadisticas);
        } catch (Exception ex) {
            return Response.exception().setPayload("Error al obtener estadísticas: " + ex.getMessage());
        }
    }

    @Operation(summary = "Obtener estadísticas por estado activo")
    @GetMapping("/estadisticas/activo")
    public Response obtenerEstadisticasPorActivo() {
        try {
            List<Object[]> estadisticas = organizacionService.getEstadisticasPorActivo();
            return Response.ok().setPayload(estadisticas);
        } catch (Exception ex) {
            return Response.exception().setPayload("Error al obtener estadísticas: " + ex.getMessage());
        }
    }
}