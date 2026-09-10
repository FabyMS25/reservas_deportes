package com.gamq.deportes.security;

import com.gamq.deportes.model.ReservaRecurrente;
import org.junit.jupiter.api.Test;
import javax.persistence.criteria.*;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class ReservationListScopeTest {
    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void taxpayerListsAndCountsUseOnlyTheOwnerEvenForRepresentatives() {
        PermissionAuthorizationService permissions = mock(PermissionAuthorizationService.class);
        when(permissions.principal()).thenReturn(new DeportesPrincipal("daniela", "CONTRIBUYENTE", false));
        Root<ReservaRecurrente> root = mock(Root.class);
        Join reservation = mock(Join.class);
        Join owner = mock(Join.class);
        Path uuid = mock(Path.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery query = mock(CriteriaQuery.class);
        Predicate owned = mock(Predicate.class);
        when(root.join("reserva")).thenReturn(reservation);
        when(reservation.join("contribuyente", JoinType.LEFT)).thenReturn(owner);
        when(owner.get("uuid")).thenReturn(uuid);
        when(cb.equal(uuid, "daniela")).thenReturn(owned);

        assertSame(owned, new ReservationListScope(permissions).children().toPredicate((Root) root, query, cb));
        verify(reservation, never()).join("organizacion", JoinType.LEFT);
        verifyNoInteractions(query);
        verify(cb).equal(uuid, "daniela");
    }
}
