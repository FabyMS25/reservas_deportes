package com.gamq.deportes.security;

import com.gamq.deportes.model.EncargadoComplejo;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Subquery;

/** Apply ownership before both LIMIT/OFFSET and the count query. */
@Component
public class ReservationListScope {
    private final PermissionAuthorizationService permissions;

    public ReservationListScope(PermissionAuthorizationService permissions) {
        this.permissions = permissions;
    }

    public <T> Specification<T> children() {
        DeportesPrincipal principal = permissions.principal();
        boolean staff = permissions.staff();
        boolean admin = permissions.admin();
        return (root, query, cb) -> {
            if (principal == null) return cb.disjunction();
            if (admin) return cb.conjunction();
            var reservation = root.join("reserva");
            if (staff) {
                var assignments = query.subquery(String.class);
                var assignment = assignments.from(EncargadoComplejo.class);
                assignments.select(assignment.get("complejo").get("uuid"))
                        .where(cb.equal(assignment.get("userUuid"), principal.getSubject()),
                                cb.isTrue(assignment.get("activo")));
                return reservation.get("campoDeportivo").get("complejo").get("uuid").in(assignments);
            }
                return cb.equal(reservation.join("contribuyente", JoinType.LEFT).get("uuid"), principal.getSubject());
        };
    }

    /** Scope for monthly school rows. A contributor can see reservations registered
     * for them, enrollments they sponsor, or schools they represent. This scope is
     * applied to both the page content and Spring Data's count query. */
    public Specification<com.gamq.deportes.model.ReservaPeriodo> periodos() {
        DeportesPrincipal principal = permissions.principal();
        boolean staff = permissions.staff();
        boolean admin = permissions.admin();
        return (root, query, cb) -> {
            if (principal == null) return cb.disjunction();
            if (admin) return cb.conjunction();
            var reservation = root.join("reserva");
            if (staff) {
                Subquery<String> assignments = query.subquery(String.class);
                var assignment = assignments.from(EncargadoComplejo.class);
                assignments.select(assignment.get("complejo").get("uuid"))
                        .where(cb.equal(assignment.get("userUuid"), principal.getSubject()),
                                cb.isTrue(assignment.get("activo")));
                return reservation.get("campoDeportivo").get("complejo").get("uuid").in(assignments);
            }
            Subquery<Long> representative = query.subquery(Long.class);
            var link = representative.from(com.gamq.deportes.model.OrganizacionRepresentante.class);
            representative.select(link.get("id").get("idOrganizacion"))
                    .where(cb.equal(link.get("contribuyente").get("uuid"), principal.getSubject()),
                            cb.equal(link.get("organizacion"), reservation.get("organizacion")));
            return cb.or(
                    cb.equal(reservation.join("contribuyente", JoinType.LEFT).get("uuid"), principal.getSubject()),
                    cb.equal(root.join("estudianteEscuela", JoinType.LEFT)
                            .join("contribuyenteDto", JoinType.LEFT).get("uuid"), principal.getSubject()),
                    cb.exists(representative)
            );
        };
    }

    /** Scope for the root Reserva list; applied before both page content and count. */
    public Specification<com.gamq.deportes.model.Reserva> reservations() {
        DeportesPrincipal principal = permissions.principal();
        boolean staff = permissions.staff();
        boolean admin = permissions.admin();
        return (root, query, cb) -> {
            if (principal == null) return cb.disjunction();
            if (admin) return cb.conjunction();
            if (staff) {
                Subquery<String> assignments = query.subquery(String.class);
                var assignment = assignments.from(EncargadoComplejo.class);
                assignments.select(assignment.get("complejo").get("uuid"))
                        .where(cb.equal(assignment.get("userUuid"), principal.getSubject()),
                                cb.isTrue(assignment.get("activo")));
                return root.get("campoDeportivo").get("complejo").get("uuid").in(assignments);
            }
                return cb.equal(root.join("contribuyente", JoinType.LEFT).get("uuid"), principal.getSubject());
        };
    }
}
