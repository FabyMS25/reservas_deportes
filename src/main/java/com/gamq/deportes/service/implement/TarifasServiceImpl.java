package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.TarifaCategoriaDto;
import com.gamq.deportes.dto.TarifaConceptoDto;
import com.gamq.deportes.dto.TarifaDto;
import com.gamq.deportes.dto.mapper.TarifaConceptoMapper;
import com.gamq.deportes.dto.mapper.TarifaMapper;

import com.gamq.deportes.exception.NoResultException;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import com.gamq.deportes.model.*;
import com.gamq.deportes.repository.*;
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
    private TarifaRepository tarifaRepository;
    @Autowired
    private TarifaCategoriaRepository tarifaCategoriaRepository;
    @Autowired
    private TarifaConceptoRepository tarifaConceptoRepository;
    @Autowired
    private CampoDeportivoRepository campoRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private ClasificadorRuatRepository clasificadorRuatRepository;

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

//    public List<TarifaDto> findTarifasByCampo(String uuidCampo) {
//        List<Tarifa> tarifas = tarifaRepository.findByCampoUuid(uuidCampo);
//
//        if (!tarifas.isEmpty()) {
//            return tarifas.stream()
//                    .map(tarifa -> TarifaMapper.toDto(tarifa, true))
//                    .collect(Collectors.toList());
//        } else {
//            throw new NoResultException(TARIFA);
//        }
//    }

    public TarifaDto crearTarifa(TarifaDto tarifaDto){
        if (tarifaDto != null) {
            Tarifa newTarifa = TarifaMapper.toEntity(tarifaDto);
            CampoDeportivo campo = obtenerCampo(tarifaDto.getCampoDeportivoDto().getUuid());
            newTarifa.setCampoDeportivo(campo);
            ClasificadorRuat clasificador = obtenerClasificadorRuat(tarifaDto.getClasificadorRuatDto().getUuid());
            newTarifa.setClasificadorRuat(clasificador);

            Tarifa tarifaGuardada = tarifaRepository.save(newTarifa);
//            List<TarifaCategoria> savedCategories = new ArrayList<>();
//            for (TarifaCategoriaDto tarifaCategoriaDto : TarifaDto.getCategoriaDtoList()) {
////                TarifaCategoria tarifaCategoria = new TarifaCategoria();
//                TarifaCategoria newTarifaCategorias = TarifaCategoriaMapper.toEntity(tarifaCategoriaDto);
//                CategoriaDeportiva categoria = obtenerCategoria(tarifaCategoriaDto.getCategoriaDto().getUuid());
//                newTarifaCategorias.setCategoria(categoria);
//                newTarifaCategorias.setTarifa(newTarifa);
//                savedCategories.add(tarifaCategoriaRepository.save(newTarifaCategorias));
//            }
//            newTarifa.setCategoriaDtoList(savedCategories);

            if (tarifaDto.getCategoriasDtoList() != null && !tarifaDto.getCategoriasDtoList().isEmpty()) {
                crearTarifaCategorias(tarifaGuardada, tarifaDto.getCategoriasDtoList());
            }
            if (tarifaDto.getConceptosDtos() != null && !tarifaDto.getConceptosDtos().isEmpty()) {
                crearTarifaConceptos(tarifaGuardada, tarifaDto.getConceptosDtos());
            }
            Tarifa tarifaCompleta = tarifaRepository.findByUuid(tarifaGuardada.getUuid())
                    .orElseThrow(() -> new NotFoundException(TARIFA, tarifaGuardada.getUuid()));

            return TarifaMapper.toDto(tarifaCompleta, true);
        } else {
            throw new NullReferenceException(TARIFA);
        }
    }


    @Transactional
    public TarifaDto actualizarTarifa(TarifaDto tarifaDto) {
        if (tarifaDto != null) {
                Tarifa tarifaExistente  = tarifaRepository.findByUuid(tarifaDto.getUuid())
                        .orElseThrow(() -> new NotFoundException(TARIFA, tarifaDto.getUuid()));

                TarifaMapper.updateEntityFromDto(tarifaDto, tarifaExistente);

            if (tarifaDto.getCampoDeportivoDto() != null) {
                CampoDeportivo campo = obtenerCampo(tarifaDto.getCampoDeportivoDto().getUuid());
                tarifaExistente.setCampoDeportivo(campo);
            }

            if (tarifaDto.getClasificadorRuatDto() != null) {
                ClasificadorRuat clasificador = obtenerClasificadorRuat(tarifaDto.getClasificadorRuatDto().getUuid());
                tarifaExistente.setClasificadorRuat(clasificador);
            }
            Tarifa tarifaActualizada = tarifaRepository.save(tarifaExistente);
//            tarifa.getCategoriaDtoList().clear();
//            tarifaCategoriaRepository.deleteByTarifa(tarifa);
//                tarifaCategoriaRepository.deleteAll(tarifa.getCategoriaDtoList() );
//            newTarifa.getCategoriaDtoList().clear();
//            newTarifa = tarifaRepository.save(newTarifa);
//            tarifaCategoriaRepository.deleteAll(newTarifa.getCategoriaDtoList());

//            for (TarifaCategoria categoriaDto : newTarifa.getCategoriaDtoList()) {
//                tarifaCategoriaRepository.delete(categoriaDto);
//            }
//            List<TarifaCategoria> savedCategories = new ArrayList<>();


//                for (TarifaCategoriaDto tarifaCategoriaDto : TarifaDto.getCategoriaDtoList()) {
////                TarifaCategoria tarifaCategoria = new TarifaCategoria();
//                    TarifaCategoria newTarifaCategorias = TarifaCategoriaMapper.toEntity(tarifaCategoriaDto);
////                    if(tarifaCategoriaDto.getUuid()==""){
//                        CategoriaDeportiva categoria = obtenerCategoria(tarifaCategoriaDto.getCategoriaDto().getUuid());
//                        newTarifaCategorias.setCategoria(categoria);
//                        newTarifaCategorias.setTarifa(newTarifa);
//                        newTarifaCategorias = tarifaCategoriaRepository.save(newTarifaCategorias);
////                    }
//                    savedCategories.add(newTarifaCategorias);
//                }
//                newTarifa.setCategoriaDtoList(savedCategories);

                if (tarifaDto.getCategoriasDtoList() != null) {
                    actualizarTarifaCategorias(tarifaActualizada, tarifaDto.getCategoriasDtoList());
                }

                if (tarifaDto.getConceptosDtos() != null) {
                    actualizarTarifaConceptos(tarifaActualizada, tarifaDto.getConceptosDtos());
                }

                Tarifa tarifaCompleta = tarifaRepository.findByUuid(tarifaActualizada.getUuid())
                        .orElseThrow(() -> new NotFoundException(TARIFA, tarifaActualizada.getUuid()));

                return TarifaMapper.toDto(tarifaCompleta, true);
            } else {
                throw new NullReferenceException(TARIFA);
            }
    }

    @Transactional
    @Override
    public String deleteTarifa(String uuid) {
        Optional<Tarifa> tarifaOptional = tarifaRepository.findByUuid(uuid);
        if (tarifaOptional.isPresent()) {
            Tarifa tarifa = tarifaOptional.get();

            if (!tarifa.getCategoriasList().isEmpty()) {
                for (TarifaCategoria tarifaCat : new ArrayList<>(tarifa.getCategoriasList())) {
                    if (tarifaCat != null && tarifaCat.getUuid() != null) {
                        tarifaCat.setEstado(true);
//                        tarifaCategoriaRepository.delete(tarifaCat);
                    }
                }
                tarifa.getCategoriasList().clear();
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

    private CategoriaDeportiva obtenerCategoria(String uuid) {
        return categoriaRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("CategoriaDeportiva not found with UUID: " + uuid));
    }
    public CampoDeportivo obtenerCampo(String uuid){
        return campoRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(CANCHAS, uuid));
    }
    private ClasificadorRuat obtenerClasificadorRuat(String uuid) {
        return clasificadorRuatRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(CLASIFICADOR_RUAT, uuid));
    }

    private void crearTarifaCategorias(Tarifa tarifa, List<TarifaCategoriaDto> categoriasDto) {
        for (TarifaCategoriaDto tarifaCatDto : categoriasDto) {
            TarifaCategoria tarifaCategoria = new TarifaCategoria();
            tarifaCategoria.setTarifa(tarifa);

            CategoriaDeportiva categoria = categoriaRepository.findByUuid(tarifaCatDto.getCategoriaDto().getUuid())
                    .orElseThrow(() -> new NotFoundException(CATEGORIA, tarifaCatDto.getCategoriaDto().getUuid()));
            tarifaCategoria.setCategoria(categoria);

            tarifaCategoriaRepository.save(tarifaCategoria);
        }
    }

    private void actualizarTarifaCategorias(Tarifa tarifa, List<TarifaCategoriaDto> categoriasDto) {
        // Soft delete existing categorias
        if (tarifa.getCategoriasList() != null) {
            for (TarifaCategoria tarifaCat : tarifa.getCategoriasList()) {
                tarifaCat.setEstado(true);
            }
        }

        // Create new categorias
        crearTarifaCategorias(tarifa, categoriasDto);
    }

    private void actualizarTarifaConceptos(Tarifa tarifa, List<TarifaConceptoDto> conceptosDto) {
        // Soft delete existing conceptos
        if (tarifa.getConceptos() != null) {
            for (TarifaConcepto concepto : tarifa.getConceptos()) {
                concepto.setEstado(true);
            }
        }

        // Create new conceptos
        crearTarifaConceptos(tarifa, conceptosDto);
    }

    private void crearTarifaConceptos(Tarifa tarifa, List<TarifaConceptoDto> conceptosDto) {
        for (TarifaConceptoDto conceptoDto : conceptosDto) {
            TarifaConcepto concepto = TarifaConceptoMapper.toEntity(conceptoDto);
            concepto.setTarifa(tarifa);
            tarifaConceptoRepository.save(concepto);
        }
    }
}
