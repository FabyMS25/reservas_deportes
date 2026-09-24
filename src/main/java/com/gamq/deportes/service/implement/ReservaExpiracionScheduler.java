package com.gamq.deportes.service.implement;

import com.gamq.deportes.model.Reserva;
import com.gamq.deportes.dto.ReservaRevisionPage;
import com.gamq.deportes.repository.ReservaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/** Retained for future use; only the scheduling annotation is disabled. */
@Service
public class ReservaExpiracionScheduler {
    private final ReservaRepository reservas;
    private final ReservaRuatRevisionService revision;
    private long lastId;

    public ReservaExpiracionScheduler(ReservaRepository reservas, ReservaRuatRevisionService revision) {
        this.reservas = reservas;
        this.revision = revision;
    }

    // @org.springframework.scheduling.annotation.Scheduled(fixedDelayString = "${gamq.reservas.expiracion.fixed-delay-ms:60000}")
    public synchronized void procesarExpiraciones() {
        final long after = lastId;
        Specification<Reserva> scope = (root, query, cb) -> cb.and(
                cb.greaterThan(root.get("idReserva"), after),
                root.get("estadoReserva").in("SOLICITADA", "PENDIENTE", "PAGADA", "EN_CURSO"));
        PageRequest request = PageRequest.of(0, 20, Sort.by("idReserva"));
        // Rotating batches prevent permanently unpaid rows from starving later rows.
        ReservaRevisionPage<Long> checked = (ReservaRevisionPage<Long>) revision.revisarPagina(() -> reservas.findAll(scope, request),
                r -> r, Reserva::getIdReserva);
        // The returned page is re-queried after changes; it can contain NEW rows.
        // Advance only past the reservations actually attempted in this batch.
        lastId = checked.getRevision().getResultados().stream()
                .filter(result -> result.getReservaDto() != null && result.getReservaDto().getIdReserva() != null)
                .mapToLong(result -> result.getReservaDto().getIdReserva()).max().orElse(0);
    }
}
