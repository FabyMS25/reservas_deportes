package com.gamq.deportes.service;

import com.gamq.deportes.dto.ConfiguracionSistemaDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface ConfiguracionSistemaService {

    ConfiguracionSistemaDto findByUuid(String uuid);
    ConfiguracionSistemaDto findByClave(String clave);

    List<ConfiguracionSistemaDto> findByCategoria(String categoria);
    List<ConfiguracionSistemaDto> findByAplicaA(String aplicaA);
    List<ConfiguracionSistemaDto> findByReferenciaUuid(String referenciaUuid);

    List<ConfiguracionSistemaDto> obtenerConfiguraciones();
    List<ConfiguracionSistemaDto> obtenerConfiguracionesActivas();
    List<ConfiguracionSistemaDto> obtenerConfiguracionesModificablesPorUsuario();

    ConfiguracionSistemaDto crearConfiguracion(ConfiguracionSistemaDto configuracionDto);
    ConfiguracionSistemaDto actualizarConfiguracion(ConfiguracionSistemaDto configuracionDto);
    ConfiguracionSistemaDto actualizarConfiguracionPorClave(String clave, ConfiguracionSistemaDto configuracionDto);

    String eliminarConfiguracion(String uuid);
    String desactivarConfiguracion(String uuid);

    // Métodos utilitarios para obtener valores específicos
    String obtenerValorTexto(String clave, String valorPorDefecto);
    Double obtenerValorNumerico(String clave, Double valorPorDefecto);
    Boolean obtenerValorBoolean(String clave, Boolean valorPorDefecto);
    Map<String, Object> obtenerConfiguracionesPorClaves(List<String> claves);

    // Métodos para categorías
    List<String> obtenerCategorias();
    Map<String, List<ConfiguracionSistemaDto>> obtenerConfiguracionesAgrupadasPorCategoria();
}
