package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.TableroDto;
import com.gamq.deportes.dto.NotificacionDto;
import com.gamq.deportes.dto.mapper.TableroMapper;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import com.gamq.deportes.model.Tablero;
import com.gamq.deportes.model.Reserva;
import com.gamq.deportes.model.ReservaRecurrente;
import com.gamq.deportes.repository.ReservaRecurrenteRepository;
import com.gamq.deportes.repository.TableroRepository;
import com.gamq.deportes.service.TableroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TableroServiceImpl implements TableroService {
    
    @Autowired
    TableroRepository tableroRepository;

    @Autowired
    ReservaRecurrenteRepository reservaRecurrenteRepository;

    @Autowired
    NotificacionDestinatariosReservaService destinatariosService;

    @Transactional(readOnly = true)
    public TableroDto findByUuid(String uuid){
        Optional<Tablero> TableroOptional = tableroRepository.findByUuid(uuid);
        if (TableroOptional.isPresent()) {
            return TableroMapper.toDto(TableroOptional.get());
        } else {
            throw new NotFoundException(TABLERO , uuid);
        }
    }
    
    @Transactional(readOnly = true)
    public List<TableroDto> obtenerTablero(){
        List<Tablero> Tableros = tableroRepository.findAll();

        return Tableros.stream().map(Tablero -> {
            return TableroMapper.toDto(Tablero);
        }).collect(Collectors.toList());
    }
    public TableroDto crearTablero(TableroDto TableroDto){
        if (TableroDto != null) {
            Tablero newTablero = TableroMapper.toEntity(TableroDto);
            

            newTablero =tableroRepository.save(newTablero);
            return TableroMapper.toDto(newTablero);
        } else {
            throw new NullReferenceException(TABLERO);
        }
    }
    public TableroDto actualizarTablero(TableroDto TableroDto){
        if (TableroDto != null) {
            Optional<Tablero> TableroOptional = tableroRepository.findByUuid(TableroDto.getUuid());

            if (TableroOptional.isPresent()) {
                Tablero tableroExistente = TableroOptional.get();
                boolean horarioCambiado = !Arrays.deepEquals(
                        tableroExistente.getHorario(), TableroDto.getHorario());
                tableroExistente.setHorario(TableroDto.getHorario());
                tableroExistente.setEstado(TableroDto.isEstado());
                Tablero guardado = tableroRepository.save(tableroExistente);

                if (horarioCambiado) {
                    reservaRecurrenteRepository.findByTableroUuid(guardado.getUuid())
                            .stream()
                            .findFirst()
                            .ifPresent(this::notificarCambioHorario);
                }
                return TableroMapper.toDto(guardado);
            } else {
                throw new NotFoundException(TABLERO, TableroDto.getUuid());
            }
        } else {
            throw new NullReferenceException(TABLERO);
        }
    }

    private void notificarCambioHorario(ReservaRecurrente recurrente) {
        Reserva reserva = recurrente.getReserva();
        if (reserva == null || reserva.getContribuyente() == null) return;

        NotificacionDto comun = base(recurrente,
                ReservaNotificacionFormatter.mensaje(reserva, "El horario recurrente fue actualizado"));
        destinatariosService.publicar(
                "NOTIFICAR_CAMBIO_HORARIO", reserva, comun, comun, null);
    }

    private NotificacionDto base(ReservaRecurrente recurrente, String mensaje) {
        return new NotificacionDto()
                .setTipo("NOTIFICAR_CAMBIO_HORARIO")
                .setTitulo(ReservaNotificacionFormatter.titulo("Cambio de horario", recurrente.getReserva()))
                .setMensaje(mensaje)
                .setPrioridad("INFO")
                .setCanal("APP")
                .setReferenciaTipo("RESERVA")
                .setReferenciaUuid(recurrente.getUuid())
                .setAccionUrl("/reservations/recurrent");
    }
}
