package com.gamq.licencias.controller;
import com.gamq.licencias.dto.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/canchasDeporte")
public class canchasDeporteController {

    @Autowired
    canchasDeporteService canchasDepService;

    @GetMapping()
    public Response obtenerCanchas() {
        try{
            List<canchasDeporteDto> canchasDeporteDtos =  canchasDepService.obtenerCanchasDeporte();
            return Response.ok().setPayload(canchasDeporteDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }

    @GetMapping("/{uuid}")
    public Response buscarConstruccionPorUuid( @PathVariable("uuid") String uuidCanchas) {
        try{
            canchasDeporteDto canchasDeporteDto = canchasDepService.findByUuid(uuidCanchas);
            return Response.ok().setPayload(canchasDeporteDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }

    @PostMapping()
    public Response crearConstruccion(@RequestBody canchasDeporteDto canchasDeporteDto) {
        try{
            return Response.ok().setPayload(canchasDepService.crearCanchaDeporte(canchasDeporteDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

    @PutMapping()
    public Response actualizarConstruccion(@RequestBody canchasDeporteDto canchasDeporteDto) {
        try{
            return Response.ok().setPayload(canchasDepService.actualizarCanchaDeporte(canchasDeporteDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

}
