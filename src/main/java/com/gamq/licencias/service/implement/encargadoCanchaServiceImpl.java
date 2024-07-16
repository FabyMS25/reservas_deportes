package com.gamq.licencias.service.implement;

import com.gamq.licencias.dto.mapper.encargadoCanchaMapper;
import com.gamq.licencias.dto.encargadoComplejoDto;
import com.gamq.licencias.model.canchas;
import com.gamq.licencias.model.complejo;
import com.gamq.licencias.model.encargadoComplejo;
import com.gamq.licencias.repository.complejoRepository;
import com.gamq.licencias.repository.encargadoCanchaRepository;
import com.gamq.licencias.service.encargadoCanchaService;
import com.gamq.licencias.exception.NoResultException;
import com.gamq.licencias.exception.NotFoundException;
import com.gamq.licencias.exception.NullReferenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.licencias.exception.enums.EntityType.*;

@Component
public class encargadoCanchaServiceImpl implements encargadoCanchaService {
    @Autowired
    encargadoCanchaRepository encargadoCanchaRepository;

    @Autowired
    complejoRepository complejoRepository;

    @Override
    public encargadoComplejoDto findByUuid(String uuid){

        Optional<encargadoComplejo> encargadoCanchaOptional = encargadoCanchaRepository.findByUuid(uuid);
        if (encargadoCanchaOptional.isPresent()) {
            return encargadoCanchaMapper.toDto(encargadoCanchaOptional.get());
        } else {
            throw new NotFoundException(ENCARGADOCANCHA , uuid);
        }

    }
    @Override
    public List<encargadoComplejoDto> obtenerEncargadosCanchas(){

        List<encargadoComplejo> encargadoComplejos = encargadoCanchaRepository.findAll();

        if (!encargadoComplejos.isEmpty()) {
            return encargadoComplejos.stream().map(encargadoCancha -> {
                return encargadoCanchaMapper.toDto(encargadoCancha);
            }).collect(Collectors.toList());
        } else {
            throw new NoResultException(ENCARGADOCANCHA);
        }
    }
    @Override
    public encargadoComplejoDto crearEncargadoCancha(encargadoComplejoDto encargadoComplejoDto){

        if (encargadoComplejoDto != null) {
            complejo complejo = getComplejo(encargadoComplejoDto.getComplejoDto().getUuid());
            encargadoComplejo newEncargadoComplejo = encargadoCanchaMapper.toEntity(encargadoComplejoDto);
            newEncargadoComplejo.setComplejo(complejo);
            return encargadoCanchaMapper.toDto(encargadoCanchaRepository.save(newEncargadoComplejo));
        } else {
            throw new NullReferenceException(CANCHAS);
        }
    }
    @Override
    public encargadoComplejoDto actualizarEncargadoCancha(encargadoComplejoDto encargadoComplejoDto){

        if (encargadoComplejoDto != null) {
            Optional<encargadoComplejo> encargadoCanchaOptional = encargadoCanchaRepository.findByUuid(encargadoComplejoDto.getUuid());

            if (encargadoCanchaOptional.isPresent()) {

                complejo complejo = getComplejo(encargadoComplejoDto.getComplejoDto().getUuid());

                encargadoComplejo encargadoComplejoModificado = encargadoCanchaMapper.toEntity(encargadoComplejoDto);
                encargadoComplejoModificado.setIdEncargadoCancha(encargadoCanchaOptional.get().getIdEncargadoCancha());

                encargadoComplejoModificado.setComplejo(complejo);
                return encargadoCanchaMapper.toDto(encargadoCanchaRepository.save(encargadoComplejoModificado));
            } else {
                throw new NotFoundException(ENCARGADOCANCHA, encargadoComplejoDto.getUuid());
            }
        } else {
            throw new NullReferenceException(ENCARGADOCANCHA);
        }
    }
    @Override
    public String eliminarEncargadoCancha(String uuid){

        return null;
    }

    private complejo getComplejo(String Uuid) {
        return complejoRepository.findByUuid(Uuid)
                .orElseThrow(() -> new NotFoundException(COMPLEJO, Uuid));
    }
}
