package com.gamq.deportes.controller;

import com.gamq.deportes.dto.ReservaBorderouxDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.ReservaBorderouxService;
import com.gamq.deportes.service.implement.ReservaServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("denyAll()")
@RestController
@RequestMapping("/api/reservas-borderoux")
@Tag(name = "Liquidaciones Borderoux", description = "API independiente para pre-liquidaciones Borderoux")
public class ReservaBorderouxController {

    private final ReservaBorderouxService borderouxService;

    public ReservaBorderouxController(ReservaBorderouxService borderouxService) {
        this.borderouxService = borderouxService;
    }

    @GetMapping
    @PreAuthorize("@permissions.has('/reservations/borderoux','VER')")
    public Response obtener(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer tab,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String estadoReserva,
            @RequestParam(required = false) String organization,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Pageable pageable = PageRequest.of(page, size, ReservaServiceImpl.ordenListado(sortBy, sortDirection, "borderoux"));
        return Response.ok().setPayload(borderouxService.obtenerConFiltros(
                pageable, tab, searchTerm, estadoReserva, organization));
    }

    @GetMapping("/clasificador")
    @PreAuthorize("@permissions.has('/reservations/borderoux','CREAR')")
    public Response clasificador() {
        return Response.ok().setPayload(borderouxService.obtenerClasificador());
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("@permissions.reservation('VER',#p0)")
    public Response buscar(@PathVariable String uuid) {
        return Response.ok().setPayload(borderouxService.findByUuid(uuid));
    }

    @PostMapping
    @PreAuthorize("@permissions.borderouxBody('CREAR',#p0)")
    public Response crear(@RequestBody ReservaBorderouxDto reservaDto) {
        return Response.ok().setPayload(borderouxService.crear(reservaDto));
    }
}
