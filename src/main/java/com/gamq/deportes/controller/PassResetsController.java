package com.gamq.deportes.controller;


import com.gamq.deportes.dto.PassDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.PassResetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/passresets")
public class PassResetsController {
    
    @Autowired
    PassResetsService passResetsService;
    
    @GetMapping()
    public Response obtenerPass() {
        try{
            List<PassDto> passDtos =  passResetsService.obtenerPassResets();
            return Response.ok().setPayload(passDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }

    @GetMapping("/{uuid}")
    public Response buscarConstruccionPorUuid( @PathVariable("uuid") String uuidCanchas) {
        try{
            PassDto PassDto = passResetsService.findByUuid(uuidCanchas);
            return Response.ok().setPayload(PassDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }

    @PostMapping()
    public Response crearPass(@RequestBody PassDto PassDto) {
        try{
            return Response.ok().setPayload(passResetsService.crearPassResets(PassDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

    @PutMapping()
    public Response actualizarPass(@RequestBody PassDto PassDto) {
        try{
            return Response.ok().setPayload(passResetsService.actualizarPassResets(PassDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

}
