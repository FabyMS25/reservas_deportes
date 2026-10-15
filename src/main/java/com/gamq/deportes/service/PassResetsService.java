package com.gamq.deportes.service;

import com.gamq.deportes.dto.PassDto;
import org.springframework.stereotype.Service;

@Service
public interface PassResetsService {
    PassDto requestPasswordReset(String email);
    boolean isResetTokenValid(String token);
    void completePasswordReset(String token, String newPassword);
}
