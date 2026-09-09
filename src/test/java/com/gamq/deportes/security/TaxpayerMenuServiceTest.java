package com.gamq.deportes.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class TaxpayerMenuServiceTest {
    @Test void onlyTwoSchoolPermissionsDependOnRepresentativeStatus() throws Exception {
        TaxpayerMenuService service = new TaxpayerMenuService(new ObjectMapper());
        Set<String> ordinary = PermissionCatalog.fromMenu(service.forRepresentative(false), "SIS-013", "DEPORTESAPP");
        Set<String> representative = PermissionCatalog.fromMenu(service.forRepresentative(true), "SIS-013", "DEPORTESAPP");
        Set<String> difference = new HashSet<>(representative); difference.removeAll(ordinary);
        assertEquals(Set.of("sch:c", "sch:u"), difference);
        assertTrue(representative.containsAll(ordinary));
        assertTrue(ordinary.containsAll(Set.of("stu:c", "stu:u", "stu:off", "stu:on", "sch:v", "sch:a")));
        assertFalse(ordinary.contains("cat:c"));
        assertFalse(ordinary.contains("sch:c"));
        assertFalse(ordinary.contains("sch:u"));
        assertTrue(service.forRepresentative(true).toString().contains("6ba7f3f5-cc94-4df6-b73c-9afc26056dd3"));
    }
    @Test void modifyingOneResponseCannotChangeAnotherUsersMenu() throws Exception {
        TaxpayerMenuService service = new TaxpayerMenuService(new ObjectMapper());
        service.forRepresentative(true).removeAll();
        assertFalse(service.forRepresentative(true).isEmpty());
    }
}
