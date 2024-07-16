package com.gamq.licencias.service.implement;

import com.gamq.licencias.dto.TarifaCategoriaDto;
import com.gamq.licencias.dto.mapper.TarifaCategoriaMapper;
import com.gamq.licencias.exception.NoResultException;
import com.gamq.licencias.exception.NotFoundException;
import com.gamq.licencias.exception.NullReferenceException;
import com.gamq.licencias.model.TarifaCategoria;
import com.gamq.licencias.service.TarifaCategoriaService;
import com.gamq.licencias.repository.TarifaCategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.licencias.exception.enums.EntityType.*;

public class TarifaCategoriaServiceImpl implements TarifaCategoriaService {
    @Autowired
    TarifaCategoriaRepository tarifaCategoriaRepository;

    public TarifaCategoriaDto findByUuid(String uuid){
        Optional<TarifaCategoria> TarifaCategoriasOptional = tarifaCategoriaRepository.findByUuid(uuid);
        if (TarifaCategoriasOptional.isPresent()) {
            return TarifaCategoriaMapper.toDto(TarifaCategoriasOptional.get());
        } else {
            throw new NotFoundException(TARIFACATEGORIA, uuid);
        }


    }
    public List<TarifaCategoriaDto> obtenerTarifaCategorias(){
        List<TarifaCategoria> TarifaCategoriass =tarifaCategoriaRepository.findAll();

        if (!TarifaCategoriass.isEmpty()) {
            return TarifaCategoriass.stream().map(TarifaCatego -> {
                return TarifaCategoriaMapper.toDto(TarifaCatego);
            }).collect(Collectors.toList());
        } else {
            throw new NoResultException(TARIFACATEGORIA);
        }

    }
    public TarifaCategoriaDto crearTarifaCategoria(TarifaCategoriaDto TarifaCategoriaDto){

        if (TarifaCategoriaDto != null) {

            TarifaCategoria newTarifaCategorias = TarifaCategoriaMapper.toEntity(TarifaCategoriaDto);
            newTarifaCategorias =tarifaCategoriaRepository.save(newTarifaCategorias);
            return TarifaCategoriaMapper.toDto(newTarifaCategorias);
        } else {
            throw new NullReferenceException(TARIFACATEGORIA);
        }
    }

    public TarifaCategoriaDto actualizarTarifaCategoria(TarifaCategoriaDto TarifaCategoriaDto){
        if (TarifaCategoriaDto != null) {
            Optional<TarifaCategoria> TarifaCategoriasOptional = tarifaCategoriaRepository.findByUuid(TarifaCategoriaDto.getUuid());

            if (TarifaCategoriasOptional.isPresent()) {
                TarifaCategoria TarifaCategoriasModificado = TarifaCategoriaMapper.toEntity(TarifaCategoriaDto);
                TarifaCategoriasModificado.setIdTarifaCategoria(TarifaCategoriasOptional.get().getIdTarifaCategoria());

                return TarifaCategoriaMapper.toDto(tarifaCategoriaRepository.save(TarifaCategoriasModificado));
            } else {
                throw new NotFoundException(TARIFACATEGORIA,TarifaCategoriaDto.getUuid());
            }
        } else {
            throw new NullReferenceException(TARIFACATEGORIA);
        }
    }


}
