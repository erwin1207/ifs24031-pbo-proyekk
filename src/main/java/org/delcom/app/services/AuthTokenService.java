package org.delcom.app.services;

import java.util.UUID;

import org.delcom.app.entities.AuthToken;
import org.delcom.app.repositories.AuthTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthTokenService {
    private final AuthTokenRepository authTokenRepository;

    public AuthTokenService(AuthTokenRepository authTokenRepository) {
        this.authTokenRepository = authTokenRepository;
    }

    @Transactional(readOnly = true)
    public AuthToken findUserToken(UUID userId, String token) {
        return authTokenRepository.findUserToken(userId, token);
    }

    @Transactional
    public AuthToken createAuthToken(AuthToken authToken) {
        return authTokenRepository.save(authToken);
    }

    @Transactional
    public void deleteAuthToken(UUID userId) {
        authTokenRepository.deleteByUserId(userId);
    }
}
