package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.ReservaPeriodoDto;
import com.gamq.deportes.dto.mapper.CampoDeportivoMapper;
import com.gamq.deportes.dto.mapper.ReservaPeriodoMapper;
import com.gamq.deportes.model.Reserva;
import com.gamq.deportes.model.ReservaPeriodo;
import com.gamq.deportes.repository.ReservaPeriodoRepository;
import com.gamq.deportes.repository.ReservaRepository;
import com.gamq.deportes.service.ReservaPeriodoService;
import com.gamq.deportes.exception.NoResultException;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.deportes.exception.enums.EntityType.RESERVA_PERIODO;
import static com.gamq.deportes.exception.enums.EntityType.RESERVA;

@Component
public class ReservaPeriodoServiceImpl implements ReservaPeriodoService {

    @Autowired
    private ReservaPeriodoRepository reservaPeriodoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Override
    public ReservaPeriodoDto findByUuid(String uuid) {
        Optional<ReservaPeriodo> reservaPeriodoOptional = reservaPeriodoRepository.findByUuid(uuid);
        if (reservaPeriodoOptional.isPresent()) {
            return ReservaPeriodoMapper.toDto(reservaPeriodoOptional.get(),true);
        } else {
            throw new NotFoundException(RESERVA_PERIODO, uuid);
        }
    }

    @Override
    public ReservaPeriodoDto findByReservaUuid(String reservaUuid) {
        Optional<ReservaPeriodo> reservaPeriodoOptional = reservaPeriodoRepository.findByReservaUuid(reservaUuid);
        if (reservaPeriodoOptional.isPresent()) {
            return ReservaPeriodoMapper.toDto(reservaPeriodoOptional.get(),false);
        } else {
            throw new NotFoundException(RESERVA_PERIODO, reservaUuid);
        }
    }

    @Override
    public List<ReservaPeriodoDto> findByPeriodo(String periodo) {
        List<ReservaPeriodo> reservasPeriodo = reservaPeriodoRepository.findByPeriodo(periodo);
        if (!reservasPeriodo.isEmpty()) {
            return reservasPeriodo.stream()
                    .map(reserva -> ReservaPeriodoMapper.toDto(reserva, true))
                    .collect(Collectors.toList());
        } else {
            throw new NoResultException(RESERVA_PERIODO);
        }
    }


    @Override
    public List<ReservaPeriodoDto> findByContribuyenteUuid(String contribuyenteUuid) {
        List<ReservaPeriodo> reservasPeriodo = reservaPeriodoRepository.findByContribuyenteUuid(contribuyenteUuid);
        if (!reservasPeriodo.isEmpty()) {
            return reservasPeriodo.stream()
                    .map(reserva -> ReservaPeriodoMapper.toDto(reserva, true))
                    .collect(Collectors.toList());
        } else {
            throw new NoResultException(RESERVA_PERIODO);
        }
    }

    @Override
    public List<ReservaPeriodoDto> findByOrganizacionUuid(String organizacionUuid) {
        List<ReservaPeriodo> reservasPeriodo = reservaPeriodoRepository.findByOrganizacionUuid(organizacionUuid);
        if (!reservasPeriodo.isEmpty()) {
            return reservasPeriodo.stream()
                    .map(reserva -> ReservaPeriodoMapper.toDto(reserva, true))
                    .collect(Collectors.toList());
        } else {
            throw new NoResultException(RESERVA_PERIODO);
        }
    }

    @Override
    public List<ReservaPeriodoDto> obtenerTodos() {
        List<ReservaPeriodo> reservasPeriodo = reservaPeriodoRepository.findAll();
        if (!reservasPeriodo.isEmpty()) {
            return reservasPeriodo.stream()
                    .map(reserva -> ReservaPeriodoMapper.toDto(reserva, true))
                    .collect(Collectors.toList());
        } else {
            throw new NoResultException(RESERVA_PERIODO);
        }
    }

    @Override
    @Transactional
    public ReservaPeriodoDto crearReservaPeriodo(ReservaPeriodoDto reservaPeriodoDto) {
        if (reservaPeriodoDto != null) {
            // Verify reserva exists if provided
            if (reservaPeriodoDto.getReservaDto() != null && reservaPeriodoDto.getReservaDto().getUuid() != null) {
                Reserva reserva = reservaRepository.findByUuid(reservaPeriodoDto.getReservaDto().getUuid())
                        .orElseThrow(() -> new NotFoundException(RESERVA, reservaPeriodoDto.getReservaDto().getUuid()));

                ReservaPeriodo nuevaReservaPeriodo = ReservaPeriodoMapper.toEntity(reservaPeriodoDto);
                nuevaReservaPeriodo.setReserva(reserva);

                return ReservaPeriodoMapper.toDto(reservaPeriodoRepository.save(nuevaReservaPeriodo),false);
            } else {
                throw new NullReferenceException(RESERVA); // Fixed: Use EntityType instead of String
            }
        } else {
            throw new NullReferenceException(RESERVA_PERIODO);
        }
    }

    @Override
    @Transactional
    public ReservaPeriodoDto actualizarReservaPeriodo(ReservaPeriodoDto reservaPeriodoDto) {
        if (reservaPeriodoDto != null) {
            Optional<ReservaPeriodo> reservaPeriodoOptional = reservaPeriodoRepository.findByUuid(reservaPeriodoDto.getUuid());

            if (reservaPeriodoOptional.isPresent()) {
                ReservaPeriodo reservaPeriodoExistente = reservaPeriodoOptional.get();
                ReservaPeriodoMapper.updateEntityFromDto(reservaPeriodoDto, reservaPeriodoExistente);

                return ReservaPeriodoMapper.toDto(reservaPeriodoRepository.save(reservaPeriodoExistente),false);
            } else {
                throw new NotFoundException(RESERVA_PERIODO, reservaPeriodoDto.getUuid());
            }
        } else {
            throw new NullReferenceException(RESERVA_PERIODO);
        }
    }

    @Override
    @Transactional
    public String eliminarReservaPeriodo(String uuid) {
        ReservaPeriodo reservaPeriodo = reservaPeriodoRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(RESERVA_PERIODO, uuid));
        reservaPeriodoRepository.delete(reservaPeriodo);
        return "Eliminación exitosa de la reserva de periodo con UUID: " + uuid;
    }
}