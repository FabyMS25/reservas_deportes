package com.gamq.deportes.service;

import com.gamq.deportes.dto.TableroDto;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface TableroService {
    TableroDto findByUuid(String uuid);
    List<TableroDto> obtenerTablero();
    TableroDto crearTablero(TableroDto TableroDto);
    TableroDto actualizarTablero(TableroDto TableroDto);
}
