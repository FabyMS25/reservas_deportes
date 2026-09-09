package com.gamq.deportes.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamq.deportes.config.SecurityConfig;
import com.gamq.deportes.controller.CategoriaController;
import com.gamq.deportes.controller.SuperficieController;
import com.gamq.deportes.controller.ClasificadorRuatController;
import com.gamq.deportes.controller.complejoController;
import com.gamq.deportes.service.SuperficieService;
import com.gamq.deportes.service.ClasificadorRuatService;
import com.gamq.deportes.service.ComplejoService;
import com.gamq.deportes.controller.canchasController;
import com.gamq.deportes.controller.encargadoComplejoController;
import com.gamq.deportes.controller.TarifasController;
import com.gamq.deportes.controller.TarifasCategoriaController;
import com.gamq.deportes.controller.ScheduleExceptionController;
import com.gamq.deportes.controller.ScheduleOperationalController;
import com.gamq.deportes.service.CampoService;
import com.gamq.deportes.service.EncargadoComplejoService;
import com.gamq.deportes.service.TarifasService;
import com.gamq.deportes.service.TarifaCategoriaService;
import com.gamq.deportes.service.ScheduleExceptionService;
import com.gamq.deportes.service.ScheduleOperationalService;
import com.gamq.deportes.dto.CategoriaDeportivaDto;
import com.gamq.deportes.exception.GlobalExceptionHandler;
import com.gamq.deportes.service.CategoriaService;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.*;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import java.util.Set;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CategoryPermissionHttpTest {
    private AnnotationConfigWebApplicationContext context;
    private MockMvc mvc;
    private DeportesTokenService tokens;
    private CategoriaService categories;

    @Configuration @EnableWebMvc
    @org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
    @Import({SecurityConfig.class, CategoriaController.class, SuperficieController.class,
            ClasificadorRuatController.class, complejoController.class, canchasController.class,
            encargadoComplejoController.class, TarifasController.class, TarifasCategoriaController.class,
            ScheduleExceptionController.class, ScheduleOperationalController.class, GlobalExceptionHandler.class})
    static class TestConfig {
        @Bean ObjectMapper mapper() { return new ObjectMapper(); }
        @Bean DeportesTokenService tokens(ObjectMapper mapper) {
            return new DeportesTokenService(mapper, "test-only-secret-at-least-thirty-two-characters", 28800);
        }
        @Bean DeportesTokenFilter tokenFilter(DeportesTokenService tokens) { return new DeportesTokenFilter(tokens); }
        @Bean CategoriaService categories() { return mock(CategoriaService.class); }
        @Bean SuperficieService surfaces() { return mock(SuperficieService.class); }
        @Bean ClasificadorRuatService classifiers() { return mock(ClasificadorRuatService.class); }
        @Bean ComplejoService complexes() { return mock(ComplejoService.class); }
        @Bean CampoService fields() { return mock(CampoService.class); }
        @Bean EncargadoComplejoService staff() { return mock(EncargadoComplejoService.class); }
        @Bean TarifasService tariffs() { return mock(TarifasService.class); }
        @Bean TarifaCategoriaService tariffCategories() { return mock(TarifaCategoriaService.class); }
        @Bean ScheduleExceptionService exceptions() { return mock(ScheduleExceptionService.class); }
        @Bean ScheduleOperationalService schedules() { return mock(ScheduleOperationalService.class); }
        @Bean(name = "permissions") PermissionAuthorizationService permissions() {
            return new PermissionAuthorizationService(null, null, null, null, null, null, null, null, null, null);
        }
    }
    @BeforeEach void setup() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext()); context.register(TestConfig.class); context.refresh();
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(context.getBean(FilterChainProxy.class)).build();
        tokens = context.getBean(DeportesTokenService.class); categories = context.getBean(CategoriaService.class);
        when(categories.crearCategoria(any())).thenReturn(new CategoriaDeportivaDto());
    }
    @AfterEach void close() { context.close(); }
    private String bearer(String... keys) { return "Bearer " + tokens.create("user", "STAFF", false, Set.of(keys)); }

    @Test void anonymousWriteIs401AndReadOnlyWriteIs403WithoutInvokingService() throws Exception {
        mvc.perform(post("/api/categorias").contentType("application/json").content("{}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/categorias").header("Authorization", bearer("cat:v"))
                .contentType("application/json").accept("application/json").content("{}"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("code").value("FORBIDDEN"));
        verify(categories, never()).crearCategoria(any());
    }
    @Test void correctCreatePermissionReachesService() throws Exception {
        mvc.perform(post("/api/categorias").header("Authorization", bearer("cat:c"))
                .contentType("application/json").content("{}")).andExpect(status().isOk());
        verify(categories).crearCategoria(any());
    }
    @Test void bookingLookupRemainsAvailableWithoutCategoryAdministrationPermission() throws Exception {
        mvc.perform(get("/api/categorias").header("Authorization", bearer("map:v")))
                .andExpect(status().isOk());
        mvc.perform(delete("/api/categorias/example").header("Authorization", bearer("map:v")))
                .andExpect(status().isForbidden());
    }
    @Test void onlyAnularAuthorizesTheExistingDeleteEndpoint() throws Exception {
        mvc.perform(delete("/api/categorias/example").header("Authorization", bearer("cat:u")))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/categorias/example").header("Authorization", bearer("cat:a")))
                .andExpect(status().isOk());
        verify(categories).delete("example");
    }

    @Test void eachCatalogRequiresItsOwnActionPermission() throws Exception {
        String[][] catalogs = {{"superficie", "surface"}, {"clasificadores", "class"}, {"complejos", "complex"},
                {"campoDeportivo", "field"}, {"sports-fields", "field"}, {"tarifas", "field"},
                {"tarifacategoria", "field"}, {"horarioFijo", "field"}, {"horarioExcepcion", "field"}, {"encargado", "staff"}};
        for (String[] catalog : catalogs) {
            for (String action : new String[]{"c", "u", "a"}) {
                String path = "/api/" + catalog[0] + (action.equals("a") ? "/example" : "");
                org.springframework.http.HttpMethod method = action.equals("c") ? org.springframework.http.HttpMethod.POST
                        : action.equals("u") ? org.springframework.http.HttpMethod.PUT : org.springframework.http.HttpMethod.DELETE;
                mvc.perform(request(method, path).header("Authorization", bearer("cat:c"))
                        .contentType("application/json").content("{}")).andExpect(status().isForbidden());
                mvc.perform(request(method, path).header("Authorization", bearer(catalog[1] + ":" + action))
                        .contentType("application/json").content("{}")).andExpect(status().isOk());
            }
        }
    }
}
