package com.gamq.deportes.controller;

import com.gamq.deportes.dto.TarifaDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.TarifasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/tarifas")
@Tag(name = "Tarifas", description = "API para gestion de tarifas de toma de servicios GAMQ")
public class TarifasController {
    @Autowired
    TarifasService tarifasService;

    @Operation(summary = "Obtener Tarifas")
    @GetMapping()
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
    public Response crearTarifa(@RequestBody TarifaDto TarifaDto) {
        try{
            return Response.ok().setPayload(tarifasService.crearTarifa(TarifaDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Actualizar Tarifa")
    @PutMapping()
    public Response actualizarTarifa(@RequestBody TarifaDto TarifaDto) {
        try{
            return Response.ok().setPayload(tarifasService.actualizarTarifa(TarifaDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Borrar Tarifa por uuid")
    @DeleteMapping("/{uuid}")
    public Response deleteTarifa(@Parameter(description = "Uuid para eliminar tarifa")
                                         @PathVariable("uuid") String uuid) {
        return Response.ok().setPayload(tarifasService.deleteTarifa(uuid));
    }
}
