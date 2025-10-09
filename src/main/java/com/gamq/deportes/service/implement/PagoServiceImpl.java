package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.mapper.PagoMapper;

import com.gamq.deportes.dto.PagoDto;
import com.gamq.deportes.model.*;
import com.gamq.deportes.model.Pago;


import com.gamq.deportes.repository.PagoRepository;
import com.gamq.deportes.repository.ReservaRepository;
import com.gamq.deportes.service.PagoService;
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
public class PagoServiceImpl implements PagoService {

    @Autowired
    PagoRepository pagosRepository;

    @Autowired
    ReservaRepository reservasRepository;

    public PagoDto findByUuid(String uuid){

        Optional<Pago> pagoOptional = pagosRepository.findByUuid(uuid);
        if (pagoOptional.isPresent()) {
            return PagoMapper.toDto(pagoOptional.get(), true);
        } else {
            throw new NotFoundException(PAGOS , uuid);
        }

    }
    public List<PagoDto> obtenerPagos(){
        List<Pago> pagos = pagosRepository.findAll();

        if (!pagos.isEmpty()) {
            return pagos.stream().map(pag -> {
                return PagoMapper.toDto(pag, true);
            }).collect(Collectors.toList());
        } else {
            throw new NoResultException(PAGOS);
        }
    }

    @Override
    public List<PagoDto> buscarPorFechaPago(Date fechaIni, Date fechaFin){

        List<Pago> pagos = pagosRepository.findByFechaPago(fechaIni, fechaFin);

        if (!pagos.isEmpty()) {
            return pagos.stream().map(pagos1 -> {
                return PagoMapper.toDto(pagos1, true);
            }).collect(Collectors.toList());
        } else {
            throw new NoResultException(PAGOS);
        }
    }
    public PagoDto crearPago(PagoDto pagosDto){

        if (pagosDto != null) {

            Pago newPago = PagoMapper.toEntity(pagosDto);
            Reserva reservas= obtenerReserva(pagosDto.getReservaDto().getUuid());
            newPago.setReserva(reservas);
            return PagoMapper.toDto(pagosRepository.save(newPago), true);
        } else {
            throw new NullReferenceException(PAGOS);
        }
    }
    public PagoDto actualizarPago(PagoDto pagosDto){

        if (pagosDto != null) {
            Optional<Pago> pagosOptional = pagosRepository.findByUuid(pagosDto.getUuid());

            if (pagosOptional.isPresent()) {
                Pago pagosModificado = PagoMapper.toEntity(pagosDto);
                Reserva reservas= obtenerReserva(pagosDto.getReservaDto().getUuid());
                pagosModificado.setReserva(reservas);

                return PagoMapper.toDto(pagosRepository.save(pagosModificado),true);
            } else {
                throw new NotFoundException(PAGOS, pagosDto.getUuid());
            }
        } else {
            throw new NullReferenceException(PAGOS);
        }
    }
    public String eliminarPago(String uuid){

        Pago pagos = pagosRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(PAGOS, uuid));
        pagosRepository.delete(pagos);

        return "Eliminacion exitosa";
    }

    public Reserva obtenerReserva(String uuid){
        return reservasRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(RESERVA, uuid));
    }
}
