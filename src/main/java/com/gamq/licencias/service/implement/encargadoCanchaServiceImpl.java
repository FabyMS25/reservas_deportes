package com.gamq.licencias.service.implement;

import com.gamq.licencias.dto.mapper.encargadoCanchaMapper;
import com.gamq.licencias.dto.encargadoCanchaDto;
import com.gamq.licencias.model.canchas;
import com.gamq.licencias.model.encargadoComplejo;
import com.gamq.licencias.repository.canchasRepository;
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

import static com.gamq.licencias.exception.enums.EntityType.CANCHAS;
import static com.gamq.licencias.exception.enums.EntityType.ENCARGADOCANCHA;
@Component
public class encargadoCanchaServiceImpl implements encargadoCanchaService {
    @Autowired
    encargadoCanchaRepository encargadoCanchaRepository;

    @Autowired
    canchasRepository canchasRepository;

    @Override
    public encargadoCanchaDto findByUuid(String uuid){

        Optional<encargadoComplejo> encargadoCanchaOptional = encargadoCanchaRepository.findByUuid(uuid);
        if (encargadoCanchaOptional.isPresent()) {
            return encargadoCanchaMapper.toDto(encargadoCanchaOptional.get());
        } else {
            throw new NotFoundException(ENCARGADOCANCHA , uuid);
        }

    }
    @Override
    public List<encargadoCanchaDto> obtenerEncargadosCanchas(){

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
    public encargadoCanchaDto crearEncargadoCancha(encargadoCanchaDto encargadoCanchaDto){

        if (encargadoCanchaDto != null) {
            canchas cancha = getCancha(encargadoCanchaDto.getCanchasDto().getUuid());
            encargadoComplejo newEncargadoComplejo = encargadoCanchaMapper.toEntity(encargadoCanchaDto);
            newEncargadoComplejo.setCancha(cancha);
            return encargadoCanchaMapper.toDto(encargadoCanchaRepository.save(newEncargadoComplejo));
        } else {
            throw new NullReferenceException(CANCHAS);
        }
    }
    @Override
    public encargadoCanchaDto actualizarEncargadoCancha(encargadoCanchaDto encargadoCanchaDto){

        if (encargadoCanchaDto != null) {
            Optional<encargadoComplejo> encargadoCanchaOptional = encargadoCanchaRepository.findByUuid(encargadoCanchaDto.getUuid());

            if (encargadoCanchaOptional.isPresent()) {

                canchas cancha = getCancha(encargadoCanchaDto.getCanchasDto().getUuid());

                encargadoComplejo encargadoComplejoModificado = encargadoCanchaMapper.toEntity(encargadoCanchaDto);
                encargadoComplejoModificado.setIdEncargadoCancha(encargadoCanchaOptional.get().getIdEncargadoCancha());

                encargadoComplejoModificado.setCancha(cancha);
                return encargadoCanchaMapper.toDto(encargadoCanchaRepository.save(encargadoComplejoModificado));
            } else {
                throw new NotFoundException(CANCHAS, encargadoCanchaDto.getUuid());
            }
        } else {
            throw new NullReferenceException(CANCHAS);
        }
    }
    @Override
    public String eliminarEncargadoCancha(String uuid){

        return null;
    }

    private canchas getCancha(String Uuid) {
        return canchasRepository.findByUuid(Uuid)
                .orElseThrow(() -> new NotFoundException(CANCHAS, Uuid));
    }
}
