package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.ReservaDto;
import com.gamq.deportes.dto.mapper.reservasMapper;
import com.gamq.deportes.model.Reserva;
import com.gamq.deportes.model.CampoDeportivo;
import com.gamq.deportes.model.contribuyentes;
import com.gamq.deportes.model.pagos;
import com.gamq.deportes.repository.CampoDeportivoRepository;
import com.gamq.deportes.repository.contribuyentesRepository;
import com.gamq.deportes.repository.pagosRepository;
import com.gamq.deportes.repository.reservasRepository;
import com.gamq.deportes.service.reservasService;
import com.gamq.deportes.exception.NoResultException;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.deportes.exception.enums.EntityType.*;
@Component
public class reservasServiceImpl implements reservasService {
    @Autowired
    reservasRepository reservasRepository;
    @Autowired
    CampoDeportivoRepository canchasRepository;
    @Autowired
    contribuyentesRepository contribuyentesRepository;
    @Autowired
    pagosRepository pagosRepository;
    @Override
    public ReservaDto findByUuid(String uuid){

        Optional<Reserva> reservasOptional = reservasRepository.findByUuid(uuid);
        if (reservasOptional.isPresent()) {
            return reservasMapper.toDto(reservasOptional.get());
        } else {
            throw new NotFoundException(RESERVAS , uuid);
        }

    }
    @Override
    public List<ReservaDto> obtenerReservas(){

        List<Reserva> reservas = reservasRepository.findAll();

        if (!reservas.isEmpty()) {
            return reservas.stream().map(reserva -> {
                return reservasMapper.toDto(reserva);
            }).collect(Collectors.toList());
        } else {
            throw new NoResultException(RESERVAS);
        }
    }
    @Override
    public List<ReservaDto> buscarPorFechaPago(Date fechaIni, Date fechaFin){

        List<Reserva> reservas = reservasRepository.findByFechaReserva(fechaIni, fechaFin);

        if (!reservas.isEmpty()) {
            return reservas.stream().map(reserva -> {
                return reservasMapper.toDto(reserva);
            }).collect(Collectors.toList());
        } else {
            throw new NoResultException(PAGOS);
        }
    }

//    public List<ReservaDto> findReservaByCancha(String uuidCancha){
//        List<Reserva> Reserva = reservasRepository.findReservasByCancha(uuidCancha);
//        if (!Reserva.isEmpty()) {
//            return Reserva.stream().map(reserva -> {
//                return reservasMapper.toDto(reserva);
//            }).collect(Collectors.toList());
//        } else {
//            throw new NoResultException(RESERVAS);
//        }
//    }
    public List<ReservaDto> findReservaByCancha(String uuidCancha) {
        try {
            List<Reserva> reservas = reservasRepository.findReservasByCancha(uuidCancha);

            return reservas.stream()
                    .map(reserva -> reservasMapper.toDto(reserva))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving Reserva for cancha: " + uuidCancha, e);
        }
    }
    @Override
    public ReservaDto crearReserva(ReservaDto reservasDto){

        if (reservasDto != null) {
            CampoDeportivo cancha = getCancha(reservasDto.getCampoDeportivoDto().getUuid());
            contribuyentes contri= getContri(reservasDto.getContribuyenteDto().getUuid());


            Reserva newReserva = reservasMapper.toEntity(reservasDto);
            newReserva.setCampoDeportivo(cancha);
            newReserva.setContribuyente(contri);

            return reservasMapper.toDto(reservasRepository.save(newReserva));
        } else {
            throw new NullReferenceException(RESERVAS);
        }
    }
    @Override
    public ReservaDto actualizarReserva(ReservaDto reservasDto){

        if (reservasDto != null) {
            Optional<Reserva> reservasOptional = reservasRepository.findByUuid(reservasDto.getUuid());

            if (reservasOptional.isPresent()) {

                CampoDeportivo cancha = getCancha(reservasDto.getCampoDeportivoDto().getUuid());
                contribuyentes contri= getContri(reservasDto.getContribuyenteDto().getUuid());
                //pagos pago=getPago(ReservaDto.getPagosDto().getUuid());

                Reserva reservasModificado = reservasMapper.toEntity(reservasDto);
                reservasModificado.setIdReserva(reservasOptional.get().getIdReserva());

                reservasModificado.setCampoDeportivo(cancha);
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
    private CampoDeportivo getCancha(String Uuid) {
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
