package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.TarifaCategoriaDto;
import com.gamq.deportes.dto.mapper.TarifaCategoriaMapper;
import com.gamq.deportes.exception.NoResultException;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import com.gamq.deportes.model.CategoriaDeportiva;
import com.gamq.deportes.model.TarifaCategoria;
import com.gamq.deportes.model.Tarifa;
import com.gamq.deportes.repository.CategoriaRepository;
import com.gamq.deportes.repository.TarifaRepository;
import com.gamq.deportes.service.TarifaCategoriaService;
import com.gamq.deportes.repository.TarifaCategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
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
    TarifaRepository tarifasRepository;


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
            CategoriaDeportiva catgegoria = obtenerCategoria(TarifaCategoriaDto.getCategoriaDto().getUuid());
            Tarifa tarifa = obtenerTarifa(TarifaCategoriaDto.getTarifaDto().getUuid());
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

    public CategoriaDeportiva obtenerCategoria(String uuid){
        return categoriaRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(CATEGORIA, uuid));
    }
    public Tarifa obtenerTarifa(String uuid){
        return tarifasRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(TARIFA, uuid));
    }

    @Transactional
    @Override
    public String delete(String uuid) {
        Optional<TarifaCategoria> item = tarifaCategoriaRepository.findByUuid(uuid);
        if (item.isPresent()) {
            TarifaCategoria feeCategory = item.get();

//            List<TarifaCategoria> canchasAsociadas = tarifaCategoriaRepository.findByCategory(category.getUuid());
//            if (!canchasAsociadas.isEmpty()) {
//                return "No se pudo eliminar la CATEGORIA porque existen datos asociadas a esta misma";
//            }
            feeCategory.setEstado(true);
            tarifaCategoriaRepository.save(feeCategory);
//            tarifaCategoriaRepository.delete(feeCategory);

            return "Eliminación exitosa de categoria";
        } else {
            throw new NotFoundException(TARIFACATEGORIA, uuid);
        }
    }

}
