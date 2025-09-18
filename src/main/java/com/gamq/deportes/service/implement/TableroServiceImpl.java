package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.TableroDto;
import com.gamq.deportes.dto.mapper.TableroMapper;
import com.gamq.deportes.dto.mapper.TableroMapper;
import com.gamq.deportes.dto.mapper.TableroMapper;
import com.gamq.deportes.dto.mapper.TableroMapper;
import com.gamq.deportes.exception.NoResultException;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import com.gamq.deportes.model.*;
import com.gamq.deportes.model.Tablero;
import com.gamq.deportes.model.Tablero;
import com.gamq.deportes.repository.TableroRepository;
import com.gamq.deportes.service.TableroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.deportes.exception.enums.EntityType.*;
@Component
public class TableroServiceImpl implements TableroService {
    
    @Autowired
    TableroRepository tableroRepository;

    public TableroDto findByUuid(String uuid){
        Optional<Tablero> TableroOptional = tableroRepository.findByUuid(uuid);
        if (TableroOptional.isPresent()) {
            return TableroMapper.toDto(TableroOptional.get());
        } else {
            throw new NotFoundException(TABLERO , uuid);
        }
    }
    
    public List<TableroDto> obtenerTablero(){
        List<Tablero> Tableros = tableroRepository.findAll();

        if (!Tableros.isEmpty()) {
            return Tableros.stream().map(Tablero -> {
                return TableroMapper.toDto(Tablero);
            }).collect(Collectors.toList());
        } else {
            throw new NoResultException(TABLERO);
        }
    }
    public TableroDto crearTablero(TableroDto TableroDto){
        if (TableroDto != null) {
            Tablero newTablero = TableroMapper.toEntity(TableroDto);
            

            newTablero =tableroRepository.save(newTablero);
            return TableroMapper.toDto(newTablero);
        } else {
            throw new NullReferenceException(TABLERO);
        }
    }
    public TableroDto actualizarTablero(TableroDto TableroDto){
        if (TableroDto != null) {
            Optional<Tablero> TableroOptional = tableroRepository.findByUuid(TableroDto.getUuid());

            if (TableroOptional.isPresent()) {


                Tablero TableroModificado = TableroMapper.toEntity(TableroDto);
                TableroModificado.setUuid(TableroOptional.get().getUuid());

                return TableroMapper.toDto(tableroRepository.save(TableroModificado));
            } else {
                throw new NotFoundException(TABLERO, TableroDto.getUuid());
            }
        } else {
            throw new NullReferenceException(TABLERO);
        }
    }
}
