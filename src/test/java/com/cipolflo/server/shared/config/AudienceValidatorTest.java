package com.cipolflo.server.shared.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AudienceValidatorTest {

    private static final String AUDIENCE = "https://cipolflo-api";

    private final AudienceValidator validator = new AudienceValidator(AUDIENCE);

    @Test
    void deberiaValidarTokenConAudienceCorrecta() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getAudience()).thenReturn(List.of(AUDIENCE));

        OAuth2TokenValidatorResult result = validator.validate(jwt);

        assertFalse(result.hasErrors());
    }

    @Test
    void deberiaRechazarTokenConAudienceIncorrecta() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getAudience()).thenReturn(List.of("https://otra-api"));

        OAuth2TokenValidatorResult result = validator.validate(jwt);

        assertTrue(result.hasErrors());
    }

    @Test
    void deberiaRechazarTokenSinAudience() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getAudience()).thenReturn(null);

        OAuth2TokenValidatorResult result = validator.validate(jwt);

        assertTrue(result.hasErrors());
    }

    @Test
    void deberiaValidarTokenConMultiplesAudiencesQueIncluyeLaCorrecta() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getAudience()).thenReturn(List.of("https://otra-api", AUDIENCE));

        OAuth2TokenValidatorResult result = validator.validate(jwt);

        assertFalse(result.hasErrors());
    }
}
