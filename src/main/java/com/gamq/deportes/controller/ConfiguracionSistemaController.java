package com.gamq.deportes.controller;

import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.dto.ConfiguracionSistemaDto;
import com.gamq.deportes.service.ConfiguracionSistemaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/configuracionSistema")
@Tag(name = "Complejos", description = "Complejos del GAMQ ")
public class ConfiguracionSistemaController {

    @Autowired
    private ConfiguracionSistemaService configuracionSistemaService;
    @Operation(summary = "Ver las Configuraciones del Sistema")
    @GetMapping()
    public Response obtenerConfiguraciones() {
        try{
            List<ConfiguracionSistemaDto> configuracionesDtos = configuracionSistemaService.obtenerConfiguraciones();
            return Response.ok().setPayload(configuracionesDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Listar Complejos")
    @GetMapping("/{uuid}")
    public Response buscarConfiguracionPorUuid(@PathVariable("uuid") String uuidConfiguracion) {
        try{
            ConfiguracionSistemaDto configuracionDto = configuracionSistemaService.findByUuid(uuidConfiguracion);
            return Response.ok().setPayload(configuracionDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Buscar Configuracion por Clave")
    @GetMapping("/clave/{clave}")
    public Response buscarConfiguracionPorClave(@Parameter(description = "Clave para búsqueda de configuración")
                                                @PathVariable("clave") String clave) {
        try{
            ConfiguracionSistemaDto configuracionDto = configuracionSistemaService.findByClave(clave);
            return Response.ok().setPayload(configuracionDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Buscar Configuracion por Categoria")
    @GetMapping("/categoria/{categoria}")
    public Response buscarConfiguracionesPorCategoria(@Parameter(description = "Categoría para búsqueda de configuraciones")
                                                      @PathVariable("categoria") String categoria) {
        try{
            List<ConfiguracionSistemaDto> configuracionesDtos = configuracionSistemaService.findByCategoria(categoria);
            return Response.ok().setPayload(configuracionesDtos);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Buscar Configuracion por AplicaA")
    @GetMapping("/aplicaA/{aplicaA}")
    public Response buscarConfiguracionesPorAplicaA(@Parameter(description = "Campo aplicaA para búsqueda de configuraciones")
                                                    @PathVariable("aplicaA") String aplicaA) {
        try{
            List<ConfiguracionSistemaDto> configuracionesDtos = configuracionSistemaService.findByAplicaA(aplicaA);
            return Response.ok().setPayload(configuracionesDtos);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Buscar Configuracion por uuid")
    @GetMapping("/referencia/{referenciaUuid}")
    public Response buscarConfiguracionesPorReferencia(@Parameter(description = "UUID de referencia para búsqueda de configuraciones")
                                                       @PathVariable("referenciaUuid") String referenciaUuid) {
        try{
            List<ConfiguracionSistemaDto> configuracionesDtos = configuracionSistemaService.findByReferenciaUuid(referenciaUuid);
            return Response.ok().setPayload(configuracionesDtos);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Buscar Configuracion Modificable por usuario")
    @GetMapping("/modificables/usuario")
    public Response obtenerConfiguracionesModificablesPorUsuario() {
        try{
            List<ConfiguracionSistemaDto> configuracionesDtos = configuracionSistemaService.obtenerConfiguracionesModificablesPorUsuario();
            return Response.ok().setPayload(configuracionesDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Lista Configuracion por Categorias")
    @GetMapping("/categorias")
    public Response obtenerCategorias() {
        try{
            List<String> categorias = configuracionSistemaService.obtenerCategorias();
            return Response.ok().setPayload(categorias);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Crear Configuracion ")
    @PostMapping()
    public Response crearConfiguracion(@RequestBody ConfiguracionSistemaDto configuracionDto) {
        try{
            return Response.ok().setPayload(configuracionSistemaService.crearConfiguracion(configuracionDto));
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Actualizar Configuracion ")
    @PutMapping()
    public Response actualizarConfiguracion(@RequestBody ConfiguracionSistemaDto configuracionDto) {
        try{
            return Response.ok().setPayload(configuracionSistemaService.actualizarConfiguracion(configuracionDto));
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Actualizar Configuracion por clave")
    @PutMapping("/clave/{clave}")
    public Response actualizarConfiguracionPorClave(@Parameter(description = "Clave de la configuración a actualizar")
                                                    @PathVariable("clave") String clave,
                                                    @RequestBody ConfiguracionSistemaDto configuracionDto) {
        try{
            return Response.ok().setPayload(configuracionSistemaService.actualizarConfiguracionPorClave(clave, configuracionDto));
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Eliminar Configuracion por uuid")
    @DeleteMapping("/{uuid}")
    public Response eliminarConfiguracion(@Parameter(description = "UUID para eliminar configuración")
                                          @PathVariable("uuid") String uuid) {
        try{
            return Response.ok().setPayload(configuracionSistemaService.eliminarConfiguracion(uuid));
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Desactivar Configuracion por uuid")
    @PutMapping("/desactivar/{uuid}")
    public Response desactivarConfiguracion(@Parameter(description = "UUID para desactivar configuración")
                                            @PathVariable("uuid") String uuid) {
        try{
            return Response.ok().setPayload(configuracionSistemaService.desactivarConfiguracion(uuid));
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
}