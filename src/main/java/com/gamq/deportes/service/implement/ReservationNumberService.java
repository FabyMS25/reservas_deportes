package com.gamq.deportes.service.implement;

import com.gamq.deportes.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional
public class ReservationNumberService {

    @Autowired
    private ReservaRepository reservaRepository;

    private static final Map<String, String> TIPO_PREFIX_MAP = Map.of(
            "puntual", "P",
            "recurrente", "R",
            "periodo", "E"
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
            throw new IllegalArgumentException("Tipo de reserva inválido. Debe ser: puntual, recurrente o periodo");
        }

        return TIPO_PREFIX_MAP.get(tipoLower);
    }

    /**
     * Previews the next reservation number without saving it
     */
    @Transactional(readOnly = true)
    public String previewNextReservationNumber(String tipoReserva) {
        String prefix = getPrefixForTipo(tipoReserva);
        String currentYear = String.valueOf(Year.now().getValue() % 100);

        // Pattern for query: R-____/25
        String searchPattern = prefix + "-____/" + currentYear;

        // Get the highest number for current year and prefix
        Optional<Long> maxNumberOpt = reservaRepository.findMaxReservationNumberByPrefixAndYear(searchPattern);

        Long nextNumber = maxNumberOpt.orElse(0L) + 1;
        return prefix + "-" + String.format("%04d", nextNumber) + "/" + currentYear;
    }

    /**
     * Generates the next available reservation number
     */
    @Transactional(readOnly = true)
    public String generateNextReservationNumber(String tipoReserva) {
        return previewNextReservationNumber(tipoReserva);
    }

    /**
     * Validates if a reservation number format is correct
     */
    public boolean isReservationNumberValid(String numeroReserva, String tipoReserva) {
        if (numeroReserva == null || numeroReserva.trim().isEmpty()) {
            return false;
        }

        // Format: P-0010/25, R-0010/25, E-0010/25
        String regex = "^[PRE]-\\d{4}/\\d{2}$";

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
        return Pattern.matches("^[PRE]-\\d{4}/\\d{2}$", numeroReserva);
    }

    /**
     * Checks if a reservation number is available (not already used)
     */
    @Transactional(readOnly = true)
    public boolean isReservationNumberAvailable(String numeroReserva) {
        return reservaRepository.findByNumeroReserva(numeroReserva).isEmpty();
    }

    /**
     * Finds the next available number that's not in use
     * Useful if there are gaps in the sequence
     */
    @Transactional(readOnly = true)
    public String findNextAvailableReservationNumber(String tipoReserva) {
        String previewNumber = previewNextReservationNumber(tipoReserva);

        // Check if the preview number is actually available
        // If not, find the next available one
        String candidate = previewNumber;
        int attempts = 0;
        int maxAttempts = 1000; // Safety limit

        while (attempts < maxAttempts) {
            if (isReservationNumberAvailable(candidate)) {
                return candidate;
            }

            // Increment the number and try again
            candidate = incrementReservationNumber(candidate);
            attempts++;
        }

        // Fallback: use timestamp-based number
        return generateFallbackReservationNumber(tipoReserva);
    }

    /**
     * Increment reservation number
     */
    private String incrementReservationNumber(String currentNumber) {
        try {
            // Extract parts: P-0010/25
            String prefix = currentNumber.substring(0, 1); // P
            String numberPart = currentNumber.substring(2, 6); // 0010
            String yearPart = currentNumber.substring(7); // 25

            Long currentNum = Long.parseLong(numberPart);
            currentNum++;

            return prefix + "-" + String.format("%04d", currentNum) + "/" + yearPart;
        } catch (Exception e) {
            // If parsing fails, return the original
            return currentNumber;
        }
    }

    private String generateFallbackReservationNumber(String tipoReserva) {
        String prefix = getPrefixForTipo(tipoReserva);
        String currentYear = String.valueOf(Year.now().getValue() % 100);
        String timestamp = String.valueOf(System.currentTimeMillis() % 10000);
        return prefix + "-" + String.format("%04d", Long.parseLong(timestamp)) + "/" + currentYear;
    }

}