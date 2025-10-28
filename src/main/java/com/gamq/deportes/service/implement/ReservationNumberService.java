package com.gamq.deportes.service.implement;

import com.gamq.deportes.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.Optional;

@Service
@Transactional
public class ReservationNumberService {

    @Autowired
    private ReservaRepository reservaRepository;

    /**
     * Previews the next reservation number without saving it
     */
    @Transactional(readOnly = true)
    public String previewNextReservationNumber() {
        String currentYear = String.valueOf(Year.now().getValue() % 100);
        String yearPattern = "____/" + currentYear; // Pattern: 4 digits + / + year

        // Get the highest number for current year
        Optional<Long> maxNumberOpt = reservaRepository.findMaxReservationNumberByYear(yearPattern);

        Long nextNumber = maxNumberOpt.orElse(0L) + 1;
        return String.format("%04d", nextNumber) + "/" + currentYear;
    }

    /**
     * Generates the next available reservation number
     * This finds the next available number at the moment of calling
     */
    @Transactional(readOnly = true)
    public String generateNextReservationNumber() {
        return previewNextReservationNumber();
    }

    /**
     * Validates if a reservation number format is correct
     */
    public boolean isReservationNumberValid(String numeroReserva) {
        if (numeroReserva == null || numeroReserva.trim().isEmpty()) {
            return false;
        }
        // Format: 4 digits + / + 2 digits (year)
        return numeroReserva.matches("^\\d{4}/\\d{2}$");
    }

    /**
     * Checks if a reservation number is available (not already used)
     */
    @Transactional(readOnly = true)
    public boolean isReservationNumberAvailable(String numeroReserva) {
        if (!isReservationNumberValid(numeroReserva)) {
            return false;
        }
        return reservaRepository.findByNumeroReserva(numeroReserva).isEmpty();
    }

    /**
     * Finds the next available number that's not in use
     * Useful if there are gaps in the sequence
     */
    @Transactional(readOnly = true)
    public String findNextAvailableReservationNumber() {
        String previewNumber = previewNextReservationNumber();

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
            String[] parts = candidate.split("/");
            Long currentNum = Long.parseLong(parts[0]);
            String year = parts[1];

            currentNum++;
            candidate = String.format("%04d", currentNum) + "/" + year;
            attempts++;
        }

        // Fallback: use timestamp-based number
        return generateFallbackReservationNumber();
    }

    private String generateFallbackReservationNumber() {
        String currentYear = String.valueOf(Year.now().getValue() % 100);
        String timestamp = String.valueOf(System.currentTimeMillis() % 10000);
        return String.format("%04d", Long.parseLong(timestamp)) + "/" + currentYear;
    }
}