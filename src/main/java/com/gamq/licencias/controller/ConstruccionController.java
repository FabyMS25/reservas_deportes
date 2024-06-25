package com.gamq.licencias.controller;

import com.gamq.licencias.dto.ConstruccionDto;
import com.gamq.licencias.dto.response.Response;
import com.gamq.licencias.service.ConstruccionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/Construcciones")
public class ConstruccionController {
    @Autowired
    private ConstruccionService construccionService;

      @Operation(summary = "Busca un actividad economica por su Uuid")
    @GetMapping("/{uuid}")
    public Response buscarConstruccionPorUuid(@Parameter(description = "Uuid para busqueda de actividad economica")
                                                    @PathVariable("uuid") String uuidConstruccion) {
        try{
            ConstruccionDto construccionDto = construccionService.findByUuid(uuidConstruccion);
            return Response.ok().setPayload(construccionDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }

}
