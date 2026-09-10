package com.gamq.deportes.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.dto.CampoDeportivoDto;
import com.gamq.deportes.service.CampoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;

@PreAuthorize("denyAll()")
@RestController
@RequestMapping({"/api/campoDeportivo", "/api/sports-fields"})
@Tag(name = "Canchas", description = "Canchas deportivas del GAMQ ")
public class canchasController {
    @Autowired
    private CampoService canchasService;
    @Operation(summary = "Listar las Canchas")
    @GetMapping()
    @PreAuthorize("@permissions.lookup('/settings/fields')")
    public Response obtenerCanchas() {
        try{
            List<CampoDeportivoDto> canchasDtos =  canchasService.obtenerCampos();
            return Response.ok().setPayload(canchasDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Cancha por uuid")
    @GetMapping("/{uuid}")
    @PreAuthorize("@permissions.lookup('/settings/fields')")
    public Response buscarConstruccionPorUuid( @PathVariable("uuid") String uuidCanchas) {
        try{
            CampoDeportivoDto canchasDto = canchasService.findByUuid(uuidCanchas);
            return Response.ok().setPayload(canchasDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Canchas por Complejo")
    @GetMapping("/complejo/{uuid}")
    @PreAuthorize("@permissions.lookup('/settings/fields')")
    public Response buscarCanchasPorComplejo(@Parameter(description = "Uuid de Complejo para busqueda de Canchas")
                                             @PathVariable("uuid") String uuidComplejo) {
        return Response.ok().setPayload(canchasService.findCampoByComplejo(uuidComplejo));
    }
    @Operation(summary = "Crear Cancha")
    @PostMapping()
    @PreAuthorize("@permissions.has('/settings/fields','CREAR')")
    public Response crearCancha(@RequestBody CampoDeportivoDto canchasDto) {
        try{
            return Response.ok().setPayload(canchasService.crearCampo(canchasDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Actualizar Cancha")
    @PutMapping()
    @PreAuthorize("@permissions.has('/settings/fields','ACTUALIZAR')")
    public Response actualizarConstruccion(@RequestBody CampoDeportivoDto canchasDto) {
        try{
            return Response.ok().setPayload(canchasService.actualizarCampo(canchasDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Eliminar Cancha por uuid")
    @DeleteMapping("/{uuid}")
    @PreAuthorize("@permissions.has('/settings/fields','ANULAR')")
    public Response deleteField(@Parameter(description = "Uuid para eliminar cancha")
                                 @PathVariable("uuid") String uuid) {
        return Response.ok().setPayload(canchasService.eliminarCampos(uuid));
    }
}
