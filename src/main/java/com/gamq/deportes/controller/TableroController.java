package com.gamq.deportes.controller;


import com.gamq.deportes.dto.TableroDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.TableroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tablero")
public class TableroController {
    @Autowired
    private TableroService TableroService;

    @GetMapping()
    public Response obtenerTablero() {
        try{
            List<TableroDto> TableroDtos =  TableroService.obtenerTablero();
            return Response.ok().setPayload(TableroDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }

    @GetMapping("/{uuid}")
    public Response buscarReservasPorUuid( @PathVariable("uuid") String uuidTablero) {
        try{
            TableroDto TableroDto = TableroService.findByUuid(uuidTablero);
            return Response.ok().setPayload(TableroDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }


    @PostMapping()
    public Response crearReserva(@RequestBody TableroDto TableroDto) {
        try{
            return Response.ok().setPayload(TableroService.crearTablero(TableroDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

    @PutMapping()
    public Response actualizarReserva(@RequestBody TableroDto TableroDto) {
        try{
            return Response.ok().setPayload(TableroService.actualizarTablero(TableroDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
}
