package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.TarifaCategoriaDto;
import com.gamq.deportes.dto.mapper.TarifaCategoriaMapper;
import com.gamq.deportes.exception.NoResultException;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import com.gamq.deportes.model.Categoria;
import com.gamq.deportes.model.TarifaCategoria;
import com.gamq.deportes.model.Tarifas;
import com.gamq.deportes.repository.CategoriaRepository;
import com.gamq.deportes.repository.TarifasRepository;
import com.gamq.deportes.service.TarifaCategoriaService;
import com.gamq.deportes.repository.TarifaCategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.deportes.exception.enums.EntityType.*;

@Component
public class TarifaCategoriaServiceImpl implements TarifaCategoriaService {
    @Autowired
    TarifaCategoriaRepository tarifaCategoriaRepository;

    @Autowired
    CategoriaRepository categoriaRepository;

    @Autowired
    TarifasRepository tarifasRepository;


    public TarifaCategoriaDto findByUuid(String uuid){
        Optional<TarifaCategoria> TarifaCategoriasOptional = tarifaCategoriaRepository.findByUuid(uuid);
        if (TarifaCategoriasOptional.isPresent()) {
            return TarifaCategoriaMapper.toDto(TarifaCategoriasOptional.get(),true);
        } else {
            throw new NotFoundException(TARIFACATEGORIA, uuid);
        }
    }

    public List<TarifaCategoriaDto> obtenerTarifaCategorias(){
        List<TarifaCategoria> TarifaCategoriass =tarifaCategoriaRepository.findAll();
        if (!TarifaCategoriass.isEmpty()) {
            return TarifaCategoriass.stream().map(TarifaCatego -> {
                return TarifaCategoriaMapper.toDto(TarifaCatego,true);
            }).collect(Collectors.toList());
        } else {
            throw new NoResultException(TARIFACATEGORIA);
        }

    }

    public TarifaCategoriaDto crearTarifaCategoria(TarifaCategoriaDto TarifaCategoriaDto){
        if (TarifaCategoriaDto != null) {
            TarifaCategoria newTarifaCategorias = TarifaCategoriaMapper.toEntity(TarifaCategoriaDto);
            Categoria catgegoria = obtenerCategoria(TarifaCategoriaDto.getCategoriaDto().getUuid());
            Tarifas tarifa = obtenerTarifa(TarifaCategoriaDto.getTarifaDto().getUuid());
            newTarifaCategorias.setCategoria(catgegoria);
            newTarifaCategorias.setTarifa(tarifa);
            newTarifaCategorias =tarifaCategoriaRepository.save(newTarifaCategorias);
            return TarifaCategoriaMapper.toDto(newTarifaCategorias,true);
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

                return TarifaCategoriaMapper.toDto(tarifaCategoriaRepository.save(TarifaCategoriasModificado),true);
            } else {
                throw new NotFoundException(TARIFACATEGORIA,TarifaCategoriaDto.getUuid());
            }
        } else {
            throw new NullReferenceException(TARIFACATEGORIA);
        }
    }

    public Categoria obtenerCategoria(String uuid){
        return categoriaRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(CATEGORIA, uuid));
    }
    public Tarifas obtenerTarifa(String uuid){
        return tarifasRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(TARIFA, uuid));
    }

    @Override
    public TarifaCategoriaDto deleteTarifaCategory(String uuid) {
        Optional<TarifaCategoria> TarifaCategoriasOptional = tarifaCategoriaRepository.findByUuid(uuid);
        if (TarifaCategoriasOptional.isPresent()) {
            TarifaCategoria tarifaCategory = TarifaCategoriasOptional.get();
            tarifaCategoriaRepository.delete(tarifaCategory);
            return TarifaCategoriaMapper.toDto(tarifaCategory,false);
        } else {
            throw new NotFoundException(TARIFACATEGORIA,uuid);
        }
    }
}
