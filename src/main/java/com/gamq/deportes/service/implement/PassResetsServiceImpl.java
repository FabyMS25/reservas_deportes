package com.gamq.deportes.service.implement;

import com.gamq.deportes.model.Contribuyente;
import com.gamq.deportes.model.Pass;
import com.gamq.deportes.dto.PassDto;
import com.gamq.deportes.repository.ContribuyenteRepository;
import com.gamq.deportes.repository.PassRepository;
import com.gamq.deportes.service.ContribuyenteService;
import com.gamq.deportes.service.PassResetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Component
public class PassResetsServiceImpl implements PassResetsService {
    private static final String GENERIC_REQUEST_MESSAGE =
            "Si el correo está registrado, recibirá instrucciones para recuperar su contraseña.";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;
    private static final int TOKEN_TTL_MINUTES = 30;
    private static final int MAX_ACTIVE_RESET_TOKENS = 3;
    private static final Pattern TOKEN_HASH = Pattern.compile("^[a-f0-9]{64}$");

    @Autowired
    private PassRepository passRepository;

    @Autowired
    private ContribuyenteRepository contribuyentesRepository;

    @Autowired
    private ContribuyenteService contribuyenteService;

    @Override
    @Transactional
    public PassDto requestPasswordReset(String email) {
        if (email == null || email.trim().isEmpty()) {
            return new PassDto().setMessage(GENERIC_REQUEST_MESSAGE);
        }

        Optional<Contribuyente> taxpayerOptional = contribuyentesRepository.findByEmailIgnoreCaseForUpdate(email.trim());
        if (!taxpayerOptional.isPresent()) return new PassDto().setMessage(GENERIC_REQUEST_MESSAGE);

        Contribuyente taxpayer = taxpayerOptional.get();
        List<Pass> previousTokens = passRepository.findAllByEmailIgnoreCase(taxpayer.getEmail());
        LocalDateTime now = LocalDateTime.now();
        long unexpiredTokens = previousTokens.stream()
                .filter(pass -> isTokenHash(pass.getToken()))
                .filter(pass -> pass.getExpiresAt() != null && pass.getExpiresAt().isAfter(now))
                .count();
        if (unexpiredTokens >= MAX_ACTIVE_RESET_TOKENS) return new PassDto().setMessage(GENERIC_REQUEST_MESSAGE);

        for (Pass previous : previousTokens) {
            if (!isTokenHash(previous.getToken()) || previous.getExpiresAt() == null
                    || !previous.getExpiresAt().isAfter(now)) {
                previous.setEstado(true);
                passRepository.save(previous);
            }
        }

        String rawToken = generateToken();
        Pass reset = new Pass()
                .setEmail(taxpayer.getEmail())
                .setToken(hashToken(rawToken))
                .setExpiresAt(LocalDateTime.now().plusMinutes(TOKEN_TTL_MINUTES))
                .setEstado(false);
        passRepository.save(reset);

        return new PassDto().setToken(rawToken).setMessage(GENERIC_REQUEST_MESSAGE);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isResetTokenValid(String token) {
        return findValidToken(token, false).isPresent();
    }

    @Override
    @Transactional
    public void completePasswordReset(String token, String newPassword) {
        Pass reset = findValidToken(token, true)
                .orElseThrow(() -> new IllegalArgumentException("El enlace de recuperación no es válido o ha expirado"));
        contribuyenteService.resetPasswordForRecovery(reset.getEmail(), newPassword);
        reset.setEstado(true);
        passRepository.save(reset);
    }

    private Optional<Pass> findValidToken(String rawToken, boolean lockForUpdate) {
        if (rawToken == null || rawToken.trim().isEmpty()) return Optional.empty();
        String tokenHash = hashToken(rawToken);
        Optional<Pass> reset = lockForUpdate
                ? passRepository.findByTokenForUpdate(tokenHash)
                : passRepository.findByToken(tokenHash);
        if (!reset.isPresent()) return Optional.empty();

        Pass pass = reset.get();
        if (pass.isEstado() || pass.getExpiresAt() == null || !pass.getExpiresAt().isAfter(LocalDateTime.now())) {
            return Optional.empty();
        }
        return Optional.of(pass);
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte value : digest) hex.append(String.format("%02x", value & 0xff));
            return hex.toString();
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private boolean isTokenHash(String token) {
        return token != null && TOKEN_HASH.matcher(token).matches();
    }

}
