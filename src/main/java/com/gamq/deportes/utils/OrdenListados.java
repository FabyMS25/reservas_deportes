package com.gamq.deportes.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Orden por defecto de los listados: el registro mas reciente primero.
 *
 * <p>Se usa la fecha de creacion como criterio principal y la llave primaria como
 * desempate para que la paginacion sea estable (evita filas repetidas o saltadas
 * cuando dos registros comparten la misma marca de tiempo).</p>
 */
public final class OrdenListados {

    private OrdenListados() {
    }

    /**
     * Mas recientes primero para entidades con columna {@code fecha_creacion}.
     *
     * @param idPropiedad nombre de la propiedad de la llave primaria de la entidad.
     */
    public static Sort masRecientes(String idPropiedad) {
        return Sort.by(Sort.Order.desc("fechaCreacion"), Sort.Order.desc(idPropiedad));
    }

    /**
     * Mas recientes primero para entidades sin columna {@code fecha_creacion};
     * la llave primaria identifica el orden de insercion.
     *
     * @param idPropiedad nombre de la propiedad de la llave primaria de la entidad.
     */
    public static Sort masRecientesPorId(String idPropiedad) {
        return Sort.by(Sort.Order.desc(idPropiedad));
    }

    /**
     * Mas recientes primero para entidades cuya fecha relevante no se llama
     * {@code fechaCreacion} (por ejemplo auditorias ordenadas por fecha de accion).
     *
     * @param fechaPropiedad nombre de la propiedad de fecha.
     * @param idPropiedad    nombre de la propiedad de la llave primaria.
     */
    public static Sort masRecientesPor(String fechaPropiedad, String idPropiedad) {
        return Sort.by(Sort.Order.desc(fechaPropiedad), Sort.Order.desc(idPropiedad));
    }

    /**
     * Aplica el orden por defecto a una pagina cuando el cliente no envio un
     * criterio de ordenamiento explicito. Si el cliente ya definio un orden, se
     * respeta su eleccion.
     *
     * @param pageable pagina solicitada.
     * @param orden    orden por defecto.
     */
    public static Pageable conOrdenPorDefecto(Pageable pageable, Sort orden) {
        if (pageable == null) {
            return PageRequest.of(0, 20, orden);
        }
        if (pageable.getSort().isSorted()) {
            return pageable;
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), orden);
    }
}
