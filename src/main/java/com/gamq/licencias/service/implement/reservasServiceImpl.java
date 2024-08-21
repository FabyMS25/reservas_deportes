package com.gamq.licencias.service.implement;

import com.gamq.licencias.dto.mapper.reservasMapper;
import com.gamq.licencias.dto.reservasDto;
import com.gamq.licencias.model.contribuyentes;
import com.gamq.licencias.model.canchas;
import com.gamq.licencias.model.pagos;
import com.gamq.licencias.model.reservas;
import com.gamq.licencias.repository.canchasRepository;
import com.gamq.licencias.repository.contribuyentesRepository;
import com.gamq.licencias.repository.pagosRepository;
import com.gamq.licencias.repository.reservasRepository;
import com.gamq.licencias.service.reservasService;
import com.gamq.licencias.exception.NoResultException;
import com.gamq.licencias.exception.NotFoundException;
import com.gamq.licencias.exception.NullReferenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.licencias.exception.enums.EntityType.*;
@Component
public class reservasServiceImpl implements reservasService {
    @Autowired
    reservasRepository reservasRepository;
    @Autowired
    canchasRepository canchasRepository;
    @Autowired
    contribuyentesRepository contribuyentesRepository;
    @Autowired
    pagosRepository pagosRepository;
    @Override
    public reservasDto findByUuid(String uuid){

        Optional<reservas> reservasOptional = reservasRepository.findByUuid(uuid);
        if (reservasOptional.isPresent()) {
            return reservasMapper.toDto(reservasOptional.get());
        } else {
            throw new NotFoundException(RESERVAS , uuid);
        }

    }
    @Override
    public List<reservasDto> obtenerReservas(){

        List<reservas> reservas = reservasRepository.findAll();

        if (!reservas.isEmpty()) {
            return reservas.stream().map(reserva -> {
                return reservasMapper.toDto(reserva);
            }).collect(Collectors.toList());
        } else {
            throw new NoResultException(RESERVAS);
        }
    }
    @Override
    public List<reservasDto> buscarPorFechaPago(Date fechaIni, Date fechaFin){

        List<reservas> reservas = reservasRepository.findByFechaReserva(fechaIni, fechaFin);

        if (!reservas.isEmpty()) {
            return reservas.stream().map(reserva -> {
                return reservasMapper.toDto(reserva);
            }).collect(Collectors.toList());
        } else {
            throw new NoResultException(PAGOS);
        }
    }
    @Override
    public reservasDto crearReserva(reservasDto reservasDto){

        if (reservasDto != null) {
            canchas cancha = getCancha(reservasDto.getCanchasDto().getUuid());
            contribuyentes contri= getContri(reservasDto.getContribuyenteDto().getUuid());


            reservas newReserva = reservasMapper.toEntity(reservasDto);
            newReserva.setCancha(cancha);
            newReserva.setContribuyente(contri);

            return reservasMapper.toDto(reservasRepository.save(newReserva));
        } else {
            throw new NullReferenceException(RESERVAS);
        }
    }
    @Override
    public reservasDto actualizarReserva(reservasDto reservasDto){

        if (reservasDto != null) {
            Optional<reservas> reservasOptional = reservasRepository.findByUuid(reservasDto.getUuid());

            if (reservasOptional.isPresent()) {

                canchas cancha = getCancha(reservasDto.getCanchasDto().getUuid());
                contribuyentes contri= getContri(reservasDto.getContribuyenteDto().getUuid());
                //pagos pago=getPago(reservasDto.getPagosDto().getUuid());

                reservas reservasModificado = reservasMapper.toEntity(reservasDto);
                reservasModificado.setIdReserva(reservasOptional.get().getIdReserva());

                reservasModificado.setCancha(cancha);
                reservasModificado.setContribuyente(contri);
                //reservasModificado.setPago(pago);
                return reservasMapper.toDto(reservasRepository.save(reservasModificado));
            } else {
                throw new NotFoundException(CANCHAS, reservasDto.getUuid());
            }
        } else {
            throw new NullReferenceException(CANCHAS);
        }
    }
    @Override
    public String eliminarReserva(String uuid){

        return null;
    }
    private canchas getCancha(String Uuid) {
        return canchasRepository.findByUuid(Uuid)
                .orElseThrow(() -> new NotFoundException(CANCHAS, Uuid));
    }
    private contribuyentes getContri(String Uuid) {
        return contribuyentesRepository.findByUuid(Uuid)
                .orElseThrow(() -> new NotFoundException(CONTRIBUYENTE, Uuid));
    }
    private pagos getPago(String Uuid) {
        return pagosRepository.findByUuid(Uuid)
                .orElseThrow(() -> new NotFoundException(PAGOS, Uuid));
    }
}
