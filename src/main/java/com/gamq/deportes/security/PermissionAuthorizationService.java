package com.gamq.deportes.security;

import com.gamq.deportes.dto.*;
import com.gamq.deportes.model.*;
import com.gamq.deportes.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Checks the server-signed snapshot, never the browser's menu. No Usuarios calls. */
@Component("permissions")
public class PermissionAuthorizationService {
    private final ReservaRepository reservations;
    private final ReservaPuntualRepository punctual;
    private final ReservaRecurrenteRepository recurrent;
    private final ReservaPeriodoRepository periods;
    private final OrganizacionRepository organizations;
    private final EncargadoComplejoRepository assignments;
    private final CampoDeportivoRepository fields;
    private final NotificacionRepository notifications;
    private final ReservaTarifaDetalleRepository details;
    private final PagoRepository payments;

    public PermissionAuthorizationService(ReservaRepository reservations, ReservaPuntualRepository punctual,
            ReservaRecurrenteRepository recurrent, ReservaPeriodoRepository periods,
            OrganizacionRepository organizations, EncargadoComplejoRepository assignments,
            CampoDeportivoRepository fields, NotificacionRepository notifications,
            ReservaTarifaDetalleRepository details, PagoRepository payments) {
        this.reservations = reservations; this.punctual = punctual; this.recurrent = recurrent;
        this.periods = periods; this.organizations = organizations; this.assignments = assignments;
        this.fields = fields; this.notifications = notifications; this.details = details; this.payments = payments;
    }

    public DeportesPrincipal principal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof DeportesPrincipal
                ? (DeportesPrincipal) authentication.getPrincipal() : null;
    }

    public boolean has(String module, String action) {
        DeportesPrincipal p = principal();
        String key = PermissionCatalog.key(module, action);
        return p != null && !key.isEmpty() && p.getPermissions().contains(key);
    }

    public boolean staff() { return principal() != null && "STAFF".equals(principal().getUserType()); }
    public boolean admin() { return staff() && principal().isAdmin(); }
    public boolean self(String uuid) { return principal() != null && principal().getSubject().equals(uuid); }

    public boolean organizationLookup() {
        return has("/organizations", "VER") || has("/students", "VER") || anyReservation("CREAR");
    }

    public boolean anyReservation(String action) {
        return has("/reservations/one-time", action) || has("/reservations/recurrent", action)
                || has("/reservations/schools", action);
    }

    public boolean reservationType(String action, String type) { return has(moduleForType(type), action); }

    /** Shared lookup data is needed by booking forms even without the administration menu. */
    public boolean lookup(String module) {
        return has(module, "VER") || anyReservation("VER") || anyReservation("CREAR")
                || has("/inicio/taxpayer", "VER") || has("/inicio/employee", "VER");
    }

    @Transactional(readOnly = true)
    public boolean reservation(String action, String uuid) {
        return uuid != null && reservations.findByUuid(uuid).map(r -> allowedReservation(action, r)).orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean reservationNumber(String action, String number) {
        return number != null && reservations.findByNumeroReserva(number)
                .map(r -> allowedReservation(action, r)).orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean reservationBody(String action, ReservaDto dto) {
        if (dto == null) return false;
        Reserva original = null;
        if (!"CREAR".equals(action)) {
            if (!reservation(action, dto.getUuid())) return false;
            original = reservations.findByUuid(dto.getUuid()).orElse(null);
            // A payload cannot switch to another reservation type to bypass its action policy.
            if (original == null || (dto.getTipoReserva() != null
                    && !dto.getTipoReserva().equalsIgnoreCase(original.getTipoReserva()))) return false;
        }
        String type = dto.getTipoReserva();
        if (type == null && dto.getUuid() != null)
            type = reservations.findByUuid(dto.getUuid()).map(Reserva::getTipoReserva).orElse(null);
        if (!has(moduleForType(type), action)) return false;
        if ("recurrente".equalsIgnoreCase(type) && !dto.isEsReservaGratuita()
                && Boolean.FALSE.equals(dto.getCancelacionAutomatica())
                && (original == null || !Boolean.FALSE.equals(original.getCancelacionAutomatica()))
                && !has("/reservations/recurrent", "CREAR-POSTPAGO")) return false;
        if (staff()) return dto.getCampoDeportivoDto() == null
                ? !"CREAR".equals(action) : fieldScope(dto.getCampoDeportivoDto().getUuid());
        // Existing owner and destination ownership must both pass.
        boolean own = dto.getContribuyenteDto() != null ? self(dto.getContribuyenteDto().getUuid())
                : original != null && original.getContribuyente() != null && self(original.getContribuyente().getUuid());
        boolean representative = dto.getOrganizacionDto() != null ? represents(dto.getOrganizacionDto().getUuid())
                : original != null && original.getOrganizacion() != null && represents(original.getOrganizacion().getUuid());
        if ("periodo".equalsIgnoreCase(type) && ("CREAR".equals(action) || "ACTUALIZAR".equals(action)))
            return representative;
        return own || representative;
    }

    @Transactional(readOnly = true)
    public boolean child(String kind, String action, String uuid) {
        if (uuid == null) return false;
        switch (kind) {
            case "puntual": return punctual.findByUuid(uuid).map(r -> allowedReservation(action, r.getReserva())).orElse(false);
            case "recurrente": return recurrent.findByUuid(uuid).map(r -> allowedReservation(action, r.getReserva())).orElse(false);
            case "periodo": return periods.findByUuid(uuid).map(r -> allowedReservation(action, r.getReserva())).orElse(false);
            case "detalle": return details.findByUuid(uuid).map(r -> allowedReservation(action, r.getReserva())).orElse(false);
            case "pago": return payments.findByUuid(uuid).map(r -> allowedReservation(action, r.getReserva())).orElse(false);
            default: return false;
        }
    }

    @Transactional(readOnly = true)
    public boolean childBody(String kind, String action, Object body) {
        ReservaDto reservation = null; String uuid = null;
        if (body instanceof ReservaPuntualDto) { reservation = ((ReservaPuntualDto) body).getReservaDto(); uuid = ((ReservaPuntualDto) body).getUuid(); }
        if (body instanceof ReservaRecurrenteDto) { reservation = ((ReservaRecurrenteDto) body).getReservaDto(); uuid = ((ReservaRecurrenteDto) body).getUuid(); }
        if (body instanceof ReservaPeriodoDto) { reservation = ((ReservaPeriodoDto) body).getReservaDto(); uuid = ((ReservaPeriodoDto) body).getUuid(); }
        if (body instanceof ReservaTarifaDetalleDto) { reservation = ((ReservaTarifaDetalleDto) body).getReservaDto(); uuid = ((ReservaTarifaDetalleDto) body).getUuid(); }
        if (body instanceof PagoDto) { reservation = ((PagoDto) body).getReservaDto(); uuid = ((PagoDto) body).getUuid(); }
        if (!"CREAR".equals(action) && !child(kind, action, uuid)) return false;
        // Child writes attach to an existing reservation: check its real type and owner.
        if (reservation == null) return !"CREAR".equals(action);
        return reservation(action, reservation.getUuid()) && reservations.findByUuid(reservation.getUuid())
                .map(r -> "detalle".equals(kind) || "pago".equals(kind) || kind.equalsIgnoreCase(r.getTipoReserva()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean represents(String organizationUuid) {
        return principal() != null && organizationUuid != null && organizations.findByUuid(organizationUuid)
                .map(o -> o.tieneRepresentante(principal().getSubject())).orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean punctualUpdate(ReservaPuntualDto dto) {
        if (dto == null) return false;
        if (childBody("puntual", "ACTUALIZAR", dto)) return true;
        if (!child("puntual", "CAMBIAR-HORARIO", dto.getUuid())) return false;
        return punctual.findByUuid(dto.getUuid()).map(r ->
                java.util.Objects.equals(r.getCantidadPersonas(), dto.getCantidadPersonas())
                && java.util.Objects.equals(r.getPrecioBaseAplicado(), dto.getPrecioBaseAplicado())
                && java.util.Objects.equals(r.getCategoria(), dto.getCategoria())
                && java.util.Objects.equals(r.getSuperficie(), dto.getSuperficie())
                && java.util.Objects.equals(r.getPeriodo(), dto.getPeriodo())
                && java.util.Objects.equals(r.getObservaciones(), dto.getObservaciones())
                && java.util.Objects.equals(r.getFechaUsoReal(), dto.getFechaUsoReal())
                && (dto.getReservaDto() == null || r.getReserva().getUuid().equals(dto.getReservaDto().getUuid())))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean ruatRate(String action, String number) {
        return number != null && reservations.findByNumeroTasa(number)
                .map(r -> allowedReservation(action, r)).orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean ticket(String kind, String uuid) {
        if (uuid == null) return false;
        java.util.Optional<Reserva> direct = reservations.findByUuid(uuid);
        if (direct.isPresent()) return kind.equalsIgnoreCase(direct.get().getTipoReserva())
                && allowedReservation("GENERAR TICKET", direct.get());
        return child(kind, "GENERAR TICKET", uuid);
    }

    private boolean allowedReservation(String action, Reserva r) {
        if (r == null || !has(moduleForType(r.getTipoReserva()), action)) return false;
        if (staff()) return r.getCampoDeportivo() != null && fieldScope(r.getCampoDeportivo().getUuid());
        boolean representative = r.getOrganizacion() != null && represents(r.getOrganizacion().getUuid());
        if ("periodo".equalsIgnoreCase(r.getTipoReserva())
                && ("CREAR".equals(action) || "ACTUALIZAR".equals(action))) return representative;
        return representative || (r.getContribuyente() != null && self(r.getContribuyente().getUuid()));
    }

    @Transactional(readOnly = true)
    public boolean fieldScope(String uuid) {
        if (admin()) return true;
        if (!staff() || uuid == null) return false;
        return assignments.findByUserUuid(principal().getSubject()).filter(a -> Boolean.TRUE.equals(a.getActivo()))
                .filter(a -> a.getComplejo() != null)
                .map(a -> fields.findByUuid(uuid).map(f -> f.getComplejo() != null
                        && a.getComplejo().getUuid().equals(f.getComplejo().getUuid())).orElse(false)).orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean notification(String uuid) {
        return uuid != null && notifications.findByUuid(uuid).map(n -> self(n.getUsuarioUuid())).orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean board(String action, String uuid) {
        if (uuid == null) return false;
        java.util.List<ReservaRecurrente> linked = recurrent.findByTableroUuid(uuid);
        return !linked.isEmpty() && linked.stream().allMatch(r -> allowedReservation(action, r.getReserva()));
    }

    public static String moduleForType(String type) {
        if (type == null) return "";
        switch (type.toLowerCase(java.util.Locale.ROOT)) {
            case "puntual": return "/reservations/one-time";
            case "recurrente": return "/reservations/recurrent";
            case "periodo": return "/reservations/schools";
            default: return "";
        }
    }
}
