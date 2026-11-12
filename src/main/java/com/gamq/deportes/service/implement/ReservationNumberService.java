package com.gamq.deportes.service.implement;

import com.gamq.deportes.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Year;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class ReservationNumberService {

    private static final int MAX_ATTEMPTS = 1000;
    private static final String CONSECUTIVO_PREFIX = "reserva-";
    // Same business zone used by the payment policy, so the gestion does not
    // change near midnight on New Year's Eve.
    private static final ZoneId ZONA_BOLIVIA = ZoneId.of("America/La_Paz");

    @Autowired
    private ReservaRepository reservaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private static final Map<String, String> TIPO_PREFIX_MAP = Map.of(
            "puntual", "P",
            "recurrente", "R",
            "periodo", "E",
            "borderoux", "B"
    );

    /**
     * Get prefix for reservation type
     * Throws exception if tipo is null or invalid
     */
    private String getPrefixForTipo(String tipoReserva) {
        if (tipoReserva == null || tipoReserva.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de reserva es requerido");
        }

        String tipoLower = tipoReserva.toLowerCase().trim();
        if (!TIPO_PREFIX_MAP.containsKey(tipoLower)) {
            throw new IllegalArgumentException(
                    "Tipo de reserva inválido. Debe ser: puntual, recurrente, periodo o borderoux");
        }

        return TIPO_PREFIX_MAP.get(tipoLower);
    }

    /**
     * Previews the next reservation number without consuming it.
     */
    @Transactional(readOnly = true)
    public String previewNextReservationNumber(String tipoReserva) {
        String prefix = getPrefixForTipo(tipoReserva);
        int gestion = Year.now(ZONA_BOLIVIA).getValue();
        long nextNumber = currentCounter(tipoReserva, gestion) + 1;
        return format(prefix, nextNumber, gestion);
    }

    /**
     * Generates and consumes the next reservation number atomically.
     */
    public String generateNextReservationNumber(String tipoReserva) {
        String prefix = getPrefixForTipo(tipoReserva);
        int gestion = Year.now(ZONA_BOLIVIA).getValue();
        long nextNumber = nextCounter(tipoReserva, gestion);
        return format(prefix, nextNumber, gestion);
    }

    /**
     * Validates if a reservation number format is correct
     */
    public boolean isReservationNumberValid(String numeroReserva, String tipoReserva) {
        if (numeroReserva == null || numeroReserva.trim().isEmpty()) {
            return false;
        }

        // Format: P-0010/25, R-0010/25, E-0010/25, B-0010/25
        String regex = "^[PREB]-\\d{4}/\\d{2}$";

        if (!Pattern.matches(regex, numeroReserva)) {
            return false;
        }

        // Validate that prefix matches booking type
        if (tipoReserva != null && !tipoReserva.trim().isEmpty()) {
            String expectedPrefix = getPrefixForTipo(tipoReserva);
            String actualPrefix = numeroReserva.substring(0, 1);
            return expectedPrefix.equals(actualPrefix);
        }

        return true;
    }

    /**
     * Validates if a reservation number format is correct (legacy method)
     */
    public boolean isReservationNumberValid(String numeroReserva) {
        // For legacy calls, accept any valid format
        if (numeroReserva == null || numeroReserva.trim().isEmpty()) {
            return false;
        }
        return Pattern.matches("^[PREB]-\\d{4}/\\d{2}$", numeroReserva);
    }

    /**
     * Checks if a reservation number is available (not already used)
     */
    @Transactional(readOnly = true)
    public boolean isReservationNumberAvailable(String numeroReserva) {
        return reservaRepository.findByNumeroReserva(numeroReserva).isEmpty();
    }

    /**
     * The booking form shows a previewed number and sends it back on create.
     * That number never goes through {@link #generateNextReservationNumber}, so
     * the counter must be advanced (never lowered) to the sequence actually
     * used. Otherwise the next preview would offer an already taken number.
     */
    public void registrarNumeroUtilizado(String tipoReserva, String numeroReserva) {
        if (numeroReserva == null || numeroReserva.trim().isEmpty()) {
            return;
        }
        Matcher matcher = Pattern.compile("^[PREB]-(\\d{4})/(\\d{2})$")
                .matcher(numeroReserva.trim());
        if (!matcher.matches()) {
            return;
        }
        long secuencia = Long.parseLong(matcher.group(1));
        int gestion = 2000 + Integer.parseInt(matcher.group(2));

        entityManager.createNativeQuery(
                        "INSERT INTO prefijos_consecutivos (clave, gestion, ultimo_numero) " +
                                "VALUES (:clave, :gestion, :secuencia) " +
                                "ON CONFLICT (clave, gestion) DO UPDATE " +
                                "SET ultimo_numero = GREATEST(prefijos_consecutivos.ultimo_numero, EXCLUDED.ultimo_numero)")
                .setParameter("clave", clave(tipoReserva))
                .setParameter("gestion", gestion)
                .setParameter("secuencia", secuencia)
                .executeUpdate();
    }

    /**
     * Generates the next available number that's not in use.
     * The counter is consumed atomically; if a manual number already uses
     * the candidate, the next counter value is tried.
     */
    public String findNextAvailableReservationNumber(String tipoReserva) {
        int attempts = 0;
        while (attempts < MAX_ATTEMPTS) {
            String candidate = generateNextReservationNumber(tipoReserva);
            if (isReservationNumberAvailable(candidate)) {
                return candidate;
            }
            attempts++;
        }

        // Fallback: use timestamp-based number
        return generateFallbackReservationNumber(tipoReserva);
    }

    private long currentCounter(String tipoReserva, int gestion) {
        List<?> result = entityManager.createNativeQuery(
                        "SELECT ultimo_numero FROM prefijos_consecutivos " +
                                "WHERE clave = :clave AND gestion = :gestion")
                .setParameter("clave", clave(tipoReserva))
                .setParameter("gestion", gestion)
                .getResultList();
        return result.isEmpty() ? 0L : ((Number) result.get(0)).longValue();
    }

    private long nextCounter(String tipoReserva, int gestion) {
        Object result = entityManager.createNativeQuery(
                        "INSERT INTO prefijos_consecutivos (clave, gestion, ultimo_numero) " +
                                "VALUES (:clave, :gestion, 1) " +
                                "ON CONFLICT (clave, gestion) DO UPDATE " +
                                "SET ultimo_numero = prefijos_consecutivos.ultimo_numero + 1 " +
                                "RETURNING ultimo_numero")
                .setParameter("clave", clave(tipoReserva))
                .setParameter("gestion", gestion)
                .getSingleResult();
        return ((Number) result).longValue();
    }

    private String clave(String tipoReserva) {
        return CONSECUTIVO_PREFIX + tipoReserva.toLowerCase().trim();
    }

    private String format(String prefix, long number, int gestion) {
        return prefix + "-" + String.format("%04d", number) + "/" + (gestion % 100);
    }

    private String generateFallbackReservationNumber(String tipoReserva) {
        String prefix = getPrefixForTipo(tipoReserva);
        String currentYear = String.valueOf(Year.now(ZONA_BOLIVIA).getValue() % 100);
        String timestamp = String.valueOf(System.currentTimeMillis() % 10000);
        return prefix + "-" + String.format("%04d", Long.parseLong(timestamp)) + "/" + currentYear;
    }
}
