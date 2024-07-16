package com.gamq.licencias.service.implement;

import com.gamq.licencias.dto.CategoriaDto;
import com.gamq.licencias.dto.mapper.CategoriaMapper;
import com.gamq.licencias.exception.NoResultException;
import com.gamq.licencias.exception.NotFoundException;
import com.gamq.licencias.exception.NullReferenceException;
import com.gamq.licencias.model.Categoria;
import com.gamq.licencias.model.canchas;
import com.gamq.licencias.repository.CategoriaRepository;
import com.gamq.licencias.repository.canchasRepository;
import com.gamq.licencias.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.licencias.exception.enums.EntityType.CANCHAS;
import static com.gamq.licencias.exception.enums.EntityType.CATEGORIA;

@Component
public class CategoriaServiceImpl implements CategoriaService {

    @Autowired
    CategoriaRepository categoriaRepository;

    public CategoriaDto findByUuid(String uuid){
        Optional<Categoria> CategoriaOptional = categoriaRepository.findByUuid(uuid);
        if (CategoriaOptional.isPresent()) {
            return CategoriaMapper.toDto(CategoriaOptional.get());
        } else {
            throw new NotFoundException(CATEGORIA , uuid);
        }


    }
    public List<CategoriaDto> obtenerCategorias(){
        List<Categoria> Categorias = categoriaRepository.findAll();

        if (!Categorias.isEmpty()) {
            return Categorias.stream().map(Categoria -> {
                return CategoriaMapper.toDto(Categoria);
            }).collect(Collectors.toList());
        } else {
            throw new NoResultException(CATEGORIA);
        }

    }
    public CategoriaDto crearCategoria(CategoriaDto CategoriaDto){

        if (CategoriaDto != null) {

            Categoria newCategoria = CategoriaMapper.toEntity(CategoriaDto);
            newCategoria =categoriaRepository.save(newCategoria);
            return CategoriaMapper.toDto(newCategoria);
        } else {
            throw new NullReferenceException(CATEGORIA);
        }
    }

    public CategoriaDto actualizarCategoria(CategoriaDto CategoriaDto){
        if (CategoriaDto != null) {
            Optional<Categoria> CategoriaOptional = categoriaRepository.findByUuid(CategoriaDto.getUuid());

            if (CategoriaOptional.isPresent()) {
                Categoria CategoriaModificado = CategoriaMapper.toEntity(CategoriaDto);
                CategoriaModificado.setIdCategoria(CategoriaOptional.get().getIdCategoria());

                return CategoriaMapper.toDto(categoriaRepository.save(CategoriaModificado));
            } else {
                throw new NotFoundException(CATEGORIA, CategoriaDto.getUuid());
            }
        } else {
            throw new NullReferenceException(CATEGORIA);
        }
    }

}
