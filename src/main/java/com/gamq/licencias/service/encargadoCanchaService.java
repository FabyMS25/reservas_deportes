package com.gamq.licencias.service;

import com.gamq.licencias.dto.canchasDto;
import com.gamq.licencias.dto.encargadoCanchaDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface encargadoCanchaService {

    encargadoCanchaDto findByUuid(String uuid);
    List<encargadoCanchaDto> obtenerEncargadosCanchas();
    encargadoCanchaDto crearEncargadoCancha(encargadoCanchaDto encargadoCanchaDto);
    encargadoCanchaDto actualizarEncargadoCancha(encargadoCanchaDto encargadoCanchaDto);
    String eliminarEncargadoCancha(String uuid);


}
