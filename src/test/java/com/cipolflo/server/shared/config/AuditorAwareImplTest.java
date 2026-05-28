package com.cipolflo.server.shared.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuditorAwareImplTest {

    private static final String EMAIL_CLAIM = "https://cipolflo.com/email";

    private final AuditorAwareImpl auditorAware = new AuditorAwareImpl();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthentication(Authentication auth) {
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void deberiaRetornarSystemCuandoNoHayAutenticacion() {
        setAuthentication(null);

        Optional<String> result = auditorAware.getCurrentAuditor();

        assertEquals("system", result.get());
    }

    @Test
    void deberiaRetornarSystemCuandoEsAutenticacionAnonima() {
        AnonymousAuthenticationToken anonAuth = mock(AnonymousAuthenticationToken.class);
        setAuthentication(anonAuth);

        Optional<String> result = auditorAware.getCurrentAuditor();

        assertEquals("system", result.get());
    }

    @Test
    void deberiaRetornarEmailDelTokenJwt() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString(EMAIL_CLAIM)).thenReturn("usuario@cipolflo.com");
        JwtAuthenticationToken jwtAuth = mock(JwtAuthenticationToken.class);
        when(jwtAuth.isAuthenticated()).thenReturn(true);
        when(jwtAuth.getToken()).thenReturn(jwt);
        setAuthentication(jwtAuth);

        Optional<String> result = auditorAware.getCurrentAuditor();

        assertEquals("usuario@cipolflo.com", result.get());
    }

    @Test
    void deberiaRetornarNombreCuandoJwtNoTieneEmail() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString(EMAIL_CLAIM)).thenReturn(null);
        JwtAuthenticationToken jwtAuth = mock(JwtAuthenticationToken.class);
        when(jwtAuth.isAuthenticated()).thenReturn(true);
        when(jwtAuth.getToken()).thenReturn(jwt);
        when(jwtAuth.getName()).thenReturn("auth0|abc123");
        setAuthentication(jwtAuth);

        Optional<String> result = auditorAware.getCurrentAuditor();

        assertEquals("auth0|abc123", result.get());
    }

    @Test
    void deberiaRetornarNombreCuandoNoEsJwt() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("usuario");
        setAuthentication(auth);

        Optional<String> result = auditorAware.getCurrentAuditor();

        assertEquals("usuario", result.get());
    }
}
