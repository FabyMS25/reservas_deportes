package com.gamq.deportes.controller;


import com.gamq.deportes.dto.PassResetsDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.PassResetsService;
import io.swagger.v3.oas.annotations.Parameter;
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
            List<PassResetsDto> PassResetsDtos =  passResetsService.obtenerPassResets();
            return Response.ok().setPayload(PassResetsDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }

    @GetMapping("/{uuid}")
    public Response buscarConstruccionPorUuid( @PathVariable("uuid") String uuidCanchas) {
        try{
            PassResetsDto PassResetsDto = passResetsService.findByUuid(uuidCanchas);
            return Response.ok().setPayload(PassResetsDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }

    @PostMapping()
    public Response crearPass(@RequestBody PassResetsDto PassResetsDto) {
        try{
            return Response.ok().setPayload(passResetsService.crearPassResets(PassResetsDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

    @PutMapping()
    public Response actualizarPass(@RequestBody PassResetsDto PassResetsDto) {
        try{
            return Response.ok().setPayload(passResetsService.actualizarPassResets(PassResetsDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

}
