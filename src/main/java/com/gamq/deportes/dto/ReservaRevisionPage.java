package com.gamq.deportes.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@Getter
public class ReservaRevisionPage<T> extends PageImpl<T> {
    private final Resumen revision;

    public ReservaRevisionPage(Page<T> page, Resumen revision) {
        super(page.getContent(), page.getPageable(), page.getTotalElements());
        this.revision = revision;
    }

    @Getter
    public static class Resumen {
        private final List<Resultado> resultados = new ArrayList<>();
        private final String alcance = "RESERVAS_REVISABLES_AUTORIZADAS";
    
        public long getRevisadas() {
            return resultados.stream().filter(r -> !"OMITIDA".equals(r.getResultado())).count();
        }
        public long getActualizadas() {
            return resultados.stream().filter(r -> "ACTUALIZADA".equals(r.getResultado())).count();
        }
        public long getSinCambios() {
            return resultados.stream().filter(r -> "SIN_CAMBIOS".equals(r.getResultado())).count();
        }
        public long getFallidas() {
            return resultados.stream().filter(r -> "ERROR".equals(r.getResultado())).count();
        }
        public long getOmitidas() {
            return resultados.stream().filter(r -> "OMITIDA".equals(r.getResultado())).count();
        }
        public boolean isCompleta() { return getFallidas() == 0 && getOmitidas() == 0; }
    }
    
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Resultado {
        private ReservaDto reservaDto;
        private String estadoAnterior;
        private String resultado = "SIN_CAMBIOS";
        private String motivo;
        private String ruat = "NO_REQUERIDO";
        // Original upstream content, kept separate from local diagnostics.
        private com.fasterxml.jackson.databind.JsonNode respuestaRuat;
        private Integer estadoHttpRuat;
        private String detalleLocal;
    }
}
