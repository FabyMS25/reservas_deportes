package com.gamq.deportes.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import com.gamq.deportes.dto.TarifaDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.TarifasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@PreAuthorize("denyAll()")
@RestController
@RequestMapping("/api/tarifas")
@Tag(name = "Tarifas", description = "API para gestion de tarifas de toma de servicios GAMQ")
public class TarifasController {
    @Autowired
    TarifasService tarifasService;

    @Operation(summary = "Obtener Tarifas")
    @GetMapping()
    @PreAuthorize("@permissions.lookup('Campos Deportivos')")
    public Response obtenerTarifas() {
        try{
            List<TarifaDto> TarifaDtos =  tarifasService.obtenerTarifas();
            return Response.ok().setPayload(TarifaDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Obtener Tarifa por uuid")
    @GetMapping("/{uuid}")
    @PreAuthorize("@permissions.lookup('Campos Deportivos')")
    public Response buscarTarifaPorUuid( @PathVariable("uuid") String uuidCanchas) {
        try{
            TarifaDto TarifaDto = tarifasService.findByUuid(uuidCanchas);
            return Response.ok().setPayload(TarifaDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Crear Tarifa")
    @PostMapping()
    @PreAuthorize("@permissions.has('Campos Deportivos','CREAR')")
    public Response crearTarifa(@RequestBody TarifaDto TarifaDto) {
        try{
            return Response.ok().setPayload(tarifasService.crearTarifa(TarifaDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Actualizar Tarifa")
    @PutMapping()
    @PreAuthorize("@permissions.has('Campos Deportivos','ACTUALIZAR')")
    public Response actualizarTarifa(@RequestBody TarifaDto TarifaDto) {
        try{
            return Response.ok().setPayload(tarifasService.actualizarTarifa(TarifaDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Borrar Tarifa por uuid")
    @DeleteMapping("/{uuid}")
    @PreAuthorize("@permissions.has('Campos Deportivos','ANULAR')")
    public Response deleteTarifa(@Parameter(description = "Uuid para eliminar tarifa")
                                         @PathVariable("uuid") String uuid) {
        return Response.ok().setPayload(tarifasService.deleteTarifa(uuid));
    }
}
