package com.gamq.licencias.service.implement;

import com.gamq.licencias.dto.mapper.pagosMapper;
import com.gamq.licencias.dto.mapper.pagosMapper;
import com.gamq.licencias.dto.mapper.pagosMapper;

import com.gamq.licencias.dto.pagosDto;
import com.gamq.licencias.model.*;
import com.gamq.licencias.model.pagos;
import com.gamq.licencias.model.pagos;
import com.gamq.licencias.model.pagos;


import com.gamq.licencias.repository.pagosRepository;
import com.gamq.licencias.repository.reservasRepository;
import com.gamq.licencias.service.pagoService;
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
public class pagoServiceImpl implements pagoService {

    @Autowired
    pagosRepository pagosRepository;

    @Autowired
    reservasRepository reservasRepository;

    public pagosDto findByUuid(String uuid){

        Optional<pagos> pagoOptional = pagosRepository.findByUuid(uuid);
        if (pagoOptional.isPresent()) {
            return pagosMapper.toDto(pagoOptional.get(), true);
        } else {
            throw new NotFoundException(PAGOS , uuid);
        }

    }
    public List<pagosDto> obtenerPagos(){
        List<pagos> pagos = pagosRepository.findAll();

        if (!pagos.isEmpty()) {
            return pagos.stream().map(pag -> {
                return pagosMapper.toDto(pag, true);
            }).collect(Collectors.toList());
        } else {
            throw new NoResultException(PAGOS);
        }
    }

    @Override
    public List<pagosDto> buscarPorFechaPago(Date fechaIni, Date fechaFin){

        List<pagos> pagos = pagosRepository.findByFechaPago(fechaIni, fechaFin);

        if (!pagos.isEmpty()) {
            return pagos.stream().map(pagos1 -> {
                return pagosMapper.toDto(pagos1, true);
            }).collect(Collectors.toList());
        } else {
            throw new NoResultException(PAGOS);
        }
    }
    public pagosDto crearPago(pagosDto pagosDto){

        if (pagosDto != null) {

            pagos newPago = pagosMapper.toEntity(pagosDto);
            reservas reservas= obtenerReserva(pagosDto.getReservasDto().getUuid());
            newPago.setReservas(reservas);
            return pagosMapper.toDto(pagosRepository.save(newPago), true);
        } else {
            throw new NullReferenceException(PAGOS);
        }
    }
    public pagosDto actualizarPago(pagosDto pagosDto){

        if (pagosDto != null) {
            Optional<pagos> pagosOptional = pagosRepository.findByUuid(pagosDto.getUuid());

            if (pagosOptional.isPresent()) {
                pagos pagosModificado = pagosMapper.toEntity(pagosDto);
                pagosModificado.setIdPago(pagosOptional.get().getIdPago());
                reservas reservas= obtenerReserva(pagosDto.getReservasDto().getUuid());
                pagosModificado.setReservas(reservas);

                return pagosMapper.toDto(pagosRepository.save(pagosModificado),true);
            } else {
                throw new NotFoundException(PAGOS, pagosDto.getUuid());
            }
        } else {
            throw new NullReferenceException(PAGOS);
        }
    }
    public String eliminarPago(String uuid){

        pagos pagos = pagosRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(PAGOS, uuid));
        pagosRepository.delete(pagos);

        return "Eliminacion exitosa";
    }

    public reservas obtenerReserva(String uuid){
        return reservasRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(RESERVAS, uuid));
    }
}
