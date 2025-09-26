package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.TarifaCategoriaDto;
import com.gamq.deportes.dto.TarifaDto;
import com.gamq.deportes.dto.mapper.TarifaCategoriaMapper;
import com.gamq.deportes.dto.mapper.TarifaMapper;

import com.gamq.deportes.exception.NoResultException;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import com.gamq.deportes.model.Categoria;
import com.gamq.deportes.model.TarifaCategoria;
import com.gamq.deportes.model.Tarifa;
import com.gamq.deportes.model.CampoDeportivo;
import com.gamq.deportes.repository.CategoriaRepository;
import com.gamq.deportes.repository.TarifaCategoriaRepository;
import com.gamq.deportes.repository.TarifasRepository;
import com.gamq.deportes.repository.CampoDeportivoRepository;
import com.gamq.deportes.service.TarifasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.deportes.exception.enums.EntityType.*;


@Component
public class TarifasServiceImpl implements TarifasService {

    @Autowired
    private TarifasRepository tarifaRepository;

    @Autowired
    private TarifaCategoriaRepository tarifaCategoriaRepository;

    @Autowired
    private CampoDeportivoRepository canchasRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    public TarifaDto findByUuid(String uuid){
        Optional<Tarifa> TarifasOptional = tarifaRepository.findByUuid(uuid);
        if (TarifasOptional.isPresent()) {
            return TarifaMapper.toDto(TarifasOptional.get(),true);
        } else {
            throw new NotFoundException(TARIFA, uuid);
        }


    }
    public List<TarifaDto> obtenerTarifas(){
        List<Tarifa> Tarifass = tarifaRepository.findAll();

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
            Tarifa newTarifa = TarifaMapper.toEntity(TarifaDto);
            CampoDeportivo cancha = obtenerCancha(TarifaDto.getCampoDeportivoDto().getUuid());
            newTarifa.setCampoDeportivo(cancha);
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
                Tarifa newTarifa = tarifaRepository.findByUuid(TarifaDto.getUuid())
                        .orElseThrow(() -> new NotFoundException(TARIFA, TarifaDto.getUuid()));

                CampoDeportivo cancha = obtenerCancha(TarifaDto.getCampoDeportivoDto().getUuid());

                newTarifa.setCosto(TarifaDto.getCosto());
                newTarifa.setDesde(TarifaDto.getDesde());
                newTarifa.setHasta(TarifaDto.getHasta());
                newTarifa.setDescripcion(TarifaDto.getDescripcion());
                newTarifa.setCampoDeportivo(cancha);

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

    public CampoDeportivo obtenerCancha(String uuid){
        return canchasRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(CANCHAS, uuid));
    }

    @Transactional
    @Override
    public String deleteTarifa(String uuid) {
        Optional<Tarifa> tarifaOptional = tarifaRepository.findByUuid(uuid);
        if (tarifaOptional.isPresent()) {
            Tarifa tarifa = tarifaOptional.get();

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
