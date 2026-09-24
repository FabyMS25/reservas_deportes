package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.ReservaTarifaDetalleDto;
import com.gamq.deportes.dto.mapper.ReservaTarifaDetalleMapper;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import com.gamq.deportes.model.Reserva;
import com.gamq.deportes.model.ReservaTarifaDetalle;
import com.gamq.deportes.model.Tarifa;
import com.gamq.deportes.model.TarifaConcepto;
import com.gamq.deportes.repository.ReservaRepository;
import com.gamq.deportes.repository.ReservaTarifaDetalleRepository;
import com.gamq.deportes.repository.TarifaConceptoRepository;
import com.gamq.deportes.repository.TarifaRepository;
import com.gamq.deportes.service.ReservaTarifaDetalleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.deportes.exception.enums.EntityType.*;

@Component
public class ReservaTarifaDetalleServiceImpl implements ReservaTarifaDetalleService {
    @Autowired
    private ReservaTarifaDetalleRepository reservaTarifaDetalleRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private TarifaRepository tarifaRepository;

    @Autowired
    private TarifaConceptoRepository tarifaConceptoRepository;

    @Override
    public ReservaTarifaDetalleDto findByUuid(String uuid) {
        Optional<ReservaTarifaDetalle> detalleOptional = reservaTarifaDetalleRepository.findByUuid(uuid);
        if (detalleOptional.isPresent()) {
            return ReservaTarifaDetalleMapper.toDto(detalleOptional.get(), true);
        } else {
            throw new NotFoundException(RESERVA_TARIFA_DETALLE, uuid);
        }
    }

    @Override
    public List<ReservaTarifaDetalleDto> obtenerReservaTarifaDetalles() {
        List<ReservaTarifaDetalle> detalles = reservaTarifaDetalleRepository.findAll();
        return detalles.stream()
                .map(detalle -> ReservaTarifaDetalleMapper.toDto(detalle, true))
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservaTarifaDetalleDto> findByReservaUuid(String reservaUuid) {
        List<ReservaTarifaDetalle> detalles = reservaTarifaDetalleRepository.findByReservaUuid(reservaUuid);
        return detalles.stream()
                .map(detalle -> ReservaTarifaDetalleMapper.toDto(detalle, true))
                .collect(Collectors.toList());
    }

    @Override
    public ReservaTarifaDetalleDto crearReservaTarifaDetalle(ReservaTarifaDetalleDto reservaTarifaDetalleDto) {
        if (reservaTarifaDetalleDto != null) {
            ReservaTarifaDetalle detalle = ReservaTarifaDetalleMapper.toEntity(reservaTarifaDetalleDto);
            detalle.setReserva(obtenerReserva(reservaTarifaDetalleDto.getReservaDto().getUuid()));

            if (reservaTarifaDetalleDto.getTarifaDto() != null && reservaTarifaDetalleDto.getTarifaDto().getUuid() != null) {
                detalle.setTarifa(obtenerTarifa(reservaTarifaDetalleDto.getTarifaDto().getUuid()));
            }

            if (reservaTarifaDetalleDto.getTarifaConceptoDto() != null
                    && reservaTarifaDetalleDto.getTarifaConceptoDto().getUuid() != null) {
                detalle.setTarifaConcepto(obtenerTarifaConcepto(reservaTarifaDetalleDto.getTarifaConceptoDto().getUuid()));
            }

            ReservaTarifaDetalle detalleGuardado = reservaTarifaDetalleRepository.save(detalle);
            return ReservaTarifaDetalleMapper.toDto(detalleGuardado, true);
        } else {
            throw new NullReferenceException(RESERVA_TARIFA_DETALLE);
        }
    }

    @Override
    public ReservaTarifaDetalleDto actualizarReservaTarifaDetalle(ReservaTarifaDetalleDto reservaTarifaDetalleDto) {
        if (reservaTarifaDetalleDto != null) {
            Optional<ReservaTarifaDetalle> detalleOptional = reservaTarifaDetalleRepository.findByUuid(reservaTarifaDetalleDto.getUuid());

            if (detalleOptional.isPresent()) {
                ReservaTarifaDetalle detalle = detalleOptional.get();
                validarEdicionSinTasa(detalle.getReserva());
                if (reservaTarifaDetalleDto.getReservaDto() != null && reservaTarifaDetalleDto.getReservaDto().getUuid() != null) {
                    validarEdicionSinTasa(obtenerReserva(reservaTarifaDetalleDto.getReservaDto().getUuid()));
                }
                ReservaTarifaDetalleMapper.updateEntityFromDto(reservaTarifaDetalleDto, detalle);

                if (reservaTarifaDetalleDto.getReservaDto() != null && reservaTarifaDetalleDto.getReservaDto().getUuid() != null) {
                    detalle.setReserva(obtenerReserva(reservaTarifaDetalleDto.getReservaDto().getUuid()));
                }

                if (reservaTarifaDetalleDto.getTarifaDto() != null && reservaTarifaDetalleDto.getTarifaDto().getUuid() != null) {
                    detalle.setTarifa(obtenerTarifa(reservaTarifaDetalleDto.getTarifaDto().getUuid()));
                } else {
                    detalle.setTarifa(null);
                }

                if (reservaTarifaDetalleDto.getTarifaConceptoDto() != null
                        && reservaTarifaDetalleDto.getTarifaConceptoDto().getUuid() != null) {
                    detalle.setTarifaConcepto(obtenerTarifaConcepto(reservaTarifaDetalleDto.getTarifaConceptoDto().getUuid()));
                } else {
                    detalle.setTarifaConcepto(null);
                }

                return ReservaTarifaDetalleMapper.toDto(reservaTarifaDetalleRepository.save(detalle), true);
            } else {
                throw new NotFoundException(RESERVA_TARIFA_DETALLE, reservaTarifaDetalleDto.getUuid());
            }
        } else {
            throw new NullReferenceException(RESERVA_TARIFA_DETALLE);
        }
    }

    @Transactional
    @Override
    public String delete(String uuid) {
        Optional<ReservaTarifaDetalle> item = reservaTarifaDetalleRepository.findByUuid(uuid);
        if (item.isPresent()) {
            ReservaTarifaDetalle detalle = item.get();
            detalle.setEstado(true);
            reservaTarifaDetalleRepository.save(detalle);
            return "Eliminación exitosa de detalle de tarifa de reserva";
        } else {
            throw new NotFoundException(RESERVA_TARIFA_DETALLE, uuid);
        }
    }

    private Reserva obtenerReserva(String uuid) {
        return reservaRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(RESERVA, uuid));
    }

    private void validarEdicionSinTasa(Reserva reserva) {
        if (reserva != null && reserva.getNumeroTasa() != null && !reserva.getNumeroTasa().isBlank()) {
            throw new IllegalStateException("No se puede editar el detalle tarifario de una reserva con tasa registrada");
        }
    }

    private Tarifa obtenerTarifa(String uuid) {
        return tarifaRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(TARIFA, uuid));
    }

    private TarifaConcepto obtenerTarifaConcepto(String uuid) {
        return tarifaConceptoRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(TARIFA_CONCEPTO, uuid));
    }
}
