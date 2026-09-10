package com.gamq.deportes.security;

import com.gamq.deportes.model.EncargadoComplejo;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import javax.persistence.criteria.JoinType;

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
            // Personal reservation lists never expand through organization membership.
            return cb.equal(reservation.join("contribuyente", JoinType.LEFT).get("uuid"), principal.getSubject());
        };
    }
}
