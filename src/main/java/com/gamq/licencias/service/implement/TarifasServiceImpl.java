package com.gamq.licencias.service.implement;

import com.gamq.licencias.dto.TarifaCategoriaDto;
import com.gamq.licencias.dto.TarifaDto;
import com.gamq.licencias.dto.mapper.TarifaCategoriaMapper;
import com.gamq.licencias.dto.mapper.TarifaMapper;

import com.gamq.licencias.exception.NoResultException;
import com.gamq.licencias.exception.NotFoundException;
import com.gamq.licencias.exception.NullReferenceException;
import com.gamq.licencias.model.Categoria;
import com.gamq.licencias.model.TarifaCategoria;
import com.gamq.licencias.model.Tarifas;
import com.gamq.licencias.model.canchas;
import com.gamq.licencias.repository.CategoriaRepository;
import com.gamq.licencias.repository.TarifaCategoriaRepository;
import com.gamq.licencias.repository.TarifasRepository;
import com.gamq.licencias.repository.canchasRepository;
import com.gamq.licencias.service.TarifasService;
import com.gamq.licencias.service.canchasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.licencias.exception.enums.EntityType.*;


@Component
public class TarifasServiceImpl implements TarifasService {

    @Autowired
    private TarifasRepository tarifaRepository;

    @Autowired
    private TarifaCategoriaRepository tarifaCategoriaRepository;

    @Autowired
    private canchasRepository canchasRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    public TarifaDto findByUuid(String uuid){
        Optional<Tarifas> TarifasOptional = tarifaRepository.findByUuid(uuid);
        if (TarifasOptional.isPresent()) {
            return TarifaMapper.toDto(TarifasOptional.get(),true);
        } else {
            throw new NotFoundException(TARIFA, uuid);
        }


    }
    public List<TarifaDto> obtenerTarifas(){
        List<Tarifas> Tarifass = tarifaRepository.findAll();

        if (!Tarifass.isEmpty()) {
            return Tarifass.stream().map(tarifa -> {
                return TarifaMapper.toDto(tarifa, true);
            }).collect(Collectors.toList());
        } else {
            throw new NoResultException(TARIFA);
        }

    }
    public TarifaDto crearTarifa(TarifaDto TarifaDto){
        if (TarifaDto != null) {
            Tarifas newTarifa = TarifaMapper.toEntity(TarifaDto);
            canchas cancha = obtenerCancha(TarifaDto.getCanchasDto().getUuid());
            newTarifa.setCancha(cancha);
            newTarifa =tarifaRepository.save(newTarifa);

            List<TarifaCategoria> savedCategories = new ArrayList<>();
            for (TarifaCategoriaDto tarifaCategoriaDto : TarifaDto.getCategoriaDtoList()) {
//                TarifaCategoria tarifaCategoria = new TarifaCategoria();
                TarifaCategoria newTarifaCategorias = TarifaCategoriaMapper.toEntity(tarifaCategoriaDto);
                Categoria categoria = obtenerCategoria(tarifaCategoriaDto.getCategoriaDto().getUuid());
                newTarifaCategorias.setCategoria(categoria);
                newTarifaCategorias.setTarifa(newTarifa);
                savedCategories.add(tarifaCategoriaRepository.save(newTarifaCategorias));
            }
            newTarifa.setCategoriaDtoList(savedCategories);

            return TarifaMapper.toDto(newTarifa,true);
        } else {
            throw new NullReferenceException(TARIFA);
        }
    }

    private Categoria obtenerCategoria(String uuid) {
        return categoriaRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Categoria not found with UUID: " + uuid));
    }

    @Transactional
    public TarifaDto actualizarTarifa(TarifaDto TarifaDto) {
        if (TarifaDto != null) {
                Tarifas newTarifa = tarifaRepository.findByUuid(TarifaDto.getUuid())
                        .orElseThrow(() -> new NotFoundException(TARIFA, TarifaDto.getUuid()));

                canchas cancha = obtenerCancha(TarifaDto.getCanchasDto().getUuid());

                newTarifa.setCosto(TarifaDto.getCosto());
                newTarifa.setDesde(TarifaDto.getDesde());
                newTarifa.setHasta(TarifaDto.getHasta());
                newTarifa.setDescripcion(TarifaDto.getDescripcion());
                newTarifa.setCancha(cancha);

//            tarifa.getCategoriaDtoList().clear();
//            tarifaCategoriaRepository.deleteByTarifa(tarifa);
//                tarifaCategoriaRepository.deleteAll(tarifa.getCategoriaDtoList() );
            newTarifa.getCategoriaDtoList().clear();
            newTarifa = tarifaRepository.save(newTarifa);
//            tarifaCategoriaRepository.deleteAll(newTarifa.getCategoriaDtoList());

//            for (TarifaCategoria categoriaDto : newTarifa.getCategoriaDtoList()) {
//                tarifaCategoriaRepository.delete(categoriaDto);
//            }
            List<TarifaCategoria> savedCategories = new ArrayList<>();


                for (TarifaCategoriaDto tarifaCategoriaDto : TarifaDto.getCategoriaDtoList()) {
//                TarifaCategoria tarifaCategoria = new TarifaCategoria();
                    TarifaCategoria newTarifaCategorias = TarifaCategoriaMapper.toEntity(tarifaCategoriaDto);
//                    if(tarifaCategoriaDto.getUuid()==""){
                        Categoria categoria = obtenerCategoria(tarifaCategoriaDto.getCategoriaDto().getUuid());
                        newTarifaCategorias.setCategoria(categoria);
                        newTarifaCategorias.setTarifa(newTarifa);
                        newTarifaCategorias = tarifaCategoriaRepository.save(newTarifaCategorias);
//                    }
                    savedCategories.add(newTarifaCategorias);
                }
                newTarifa.setCategoriaDtoList(savedCategories);

                return TarifaMapper.toDto(newTarifa,true);
            } else {
                throw new NullReferenceException(TARIFA);
            }
    }

    public canchas obtenerCancha(String uuid){
        return canchasRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(CANCHAS, uuid));
    }

    @Transactional
    @Override
    public String deleteTarifa(String uuid) {
        Optional<Tarifas> tarifaOptional = tarifaRepository.findByUuid(uuid);
        if (tarifaOptional.isPresent()) {
            Tarifas tarifa = tarifaOptional.get();

            if (!tarifa.getCategoriaDtoList().isEmpty()) {
                for (TarifaCategoria tarifaCat : new ArrayList<>(tarifa.getCategoriaDtoList())) {
                    if (tarifaCat != null && tarifaCat.getUuid() != null) {
                        tarifaCat.setEstado(true);
//                        tarifaCategoriaRepository.delete(tarifaCat);
                    }
                }
                tarifa.getCategoriaDtoList().clear();
//                tarifa.setEstado(true);
//                tarifaRepository.save(tarifa);
            }

            tarifaRepository.delete(tarifa);
//            return TarifaMapper.toDto(tarifa, false);
            return "Eliminacion exitosa";
        } else {
            throw new NotFoundException(TARIFA, uuid);
        }
    }

}
