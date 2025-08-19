package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.ReservasProgramadasDto;
import com.gamq.deportes.dto.mapper.ReservasProgramadasMapper;
import com.gamq.deportes.exception.NoResultException;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import com.gamq.deportes.model.*;
import com.gamq.deportes.repository.*;
import com.gamq.deportes.service.ReservasProgramadasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.deportes.exception.enums.EntityType.*;


@Component
public class ReservasProgramadasServiceImpl  implements ReservasProgramadasService {

    @Autowired
    ReservasProgramadasRepository reservasProgramadasRepository;
    @Autowired
    TableroRepository tableroRepository;
    @Autowired
    contribuyentesRepository contribuyentesRepository;


    public ReservasProgramadasDto findByUuid(String uuid){
        Optional<ReservasProgramadas> reservasProgramadasOptional = reservasProgramadasRepository.findByUuid(uuid);
        if (reservasProgramadasOptional.isPresent()) {
            return ReservasProgramadasMapper.toDto(reservasProgramadasOptional.get());
        } else {
            throw new NotFoundException(RESERVASPROGRAMADAS , uuid);
        }
    }


    public List<ReservasProgramadasDto> obtenerReservasProgramadas(){
        List<ReservasProgramadas> ReservasProgramadass = reservasProgramadasRepository.findAll();

        if (!ReservasProgramadass.isEmpty()) {
            return ReservasProgramadass.stream().map(ReservasProgramadas -> {
                return ReservasProgramadasMapper.toDto(ReservasProgramadas);
            }).collect(Collectors.toList());
        } else {
            throw new NoResultException(RESERVASPROGRAMADAS);
        }

    }
    public ReservasProgramadasDto crearReservasProgramadas(ReservasProgramadasDto reservasProgramadasDto){

        if (reservasProgramadasDto != null) {
            ReservasProgramadas newReserva = ReservasProgramadasMapper.toEntity(reservasProgramadasDto);
            contribuyentes contri= obtenerContribuyente(reservasProgramadasDto.getContribuyenteDto().getUuid());
            Tablero tablero= obtenerTablero(reservasProgramadasDto.getTableroDto().getUuid());
            newReserva.setContribuyente(contri);
            newReserva.setTablero(tablero);

            newReserva =reservasProgramadasRepository.save(newReserva);
            return ReservasProgramadasMapper.toDto(newReserva);
        } else {
            throw new NullReferenceException(RESERVASPROGRAMADAS);
        }
    }

    public ReservasProgramadasDto actualizarReservasProgramadas(ReservasProgramadasDto ReservasProgramadasDto){
        if (ReservasProgramadasDto != null) {
            Optional<ReservasProgramadas> ReservasProgramadasOptional = reservasProgramadasRepository.findByUuid(ReservasProgramadasDto.getUuid());

            if (ReservasProgramadasOptional.isPresent()) {

                contribuyentes contri= obtenerContribuyente(ReservasProgramadasDto.getContribuyenteDto().getUuid());
                Tablero tablero= obtenerTablero(ReservasProgramadasDto.getTableroDto().getUuid());
                ReservasProgramadas ReservasProgramadasModificado = ReservasProgramadasMapper.toEntity(ReservasProgramadasDto);
                ReservasProgramadasModificado.setUuid(ReservasProgramadasOptional.get().getUuid());
                ReservasProgramadasModificado.setContribuyente(contri);
                ReservasProgramadasModificado.setTablero(tablero);
                return ReservasProgramadasMapper.toDto(reservasProgramadasRepository.save(ReservasProgramadasModificado));
            } else {
                throw new NotFoundException(RESERVASPROGRAMADAS, ReservasProgramadasDto.getUuid());
            }
        } else {
            throw new NullReferenceException(RESERVASPROGRAMADAS);
        }
    }

    public contribuyentes obtenerContribuyente(String uuid ){
        return contribuyentesRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(CONTRIBUYENTE, uuid));
    }
    public Tablero obtenerTablero(String uuid){
        return tableroRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(TABLERO, uuid));
    }
    /*public String eliminarReservasProgramadas(String uuid){
        ReservasProgramadas ReservasProgramadas = ReservasProgramadasRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(ReservasProgramadas, uuid));
        ReservasProgramadasRepository.delete(ReservasProgramadas);

        return "Eliminacion exitosa";

    }*/


}
