package com.cipolflo.server.integraciones.telegram.controller;

import com.cipolflo.server.integraciones.telegram.client.TelegramProperties;
import com.cipolflo.server.integraciones.telegram.dto.TelegramChatDto;
import com.cipolflo.server.integraciones.telegram.dto.TelegramMessageDto;
import com.cipolflo.server.integraciones.telegram.dto.TelegramUpdateDto;
import com.cipolflo.server.integraciones.telegram.processor.IProcesadorMensajeTelegram;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@code @WebMvcTest} en este proyecto no carga el {@code SecurityConfig} real (su
 * {@code jwtDecoder()} pegaría contra un issuer real), así que corre con la seguridad
 * default de Spring Boot (todo autenticado + CSRF). Por eso estos tests usan
 * {@code @WithMockUser} + {@code .with(csrf())}, igual que el resto de los controllers del
 * proyecto — no valida el {@code permitAll()} real de {@code /api/public/**} (eso es
 * responsabilidad de {@code SecurityConfig}, no de este endpoint), solo la lógica propia
 * del controller: validación del secret y delegación en el procesador.
 */
@WebMvcTest(TelegramWebhookController.class)
@Import(TelegramWebhookControllerTest.TestConfig.class)
class TelegramWebhookControllerTest {

    private static final String SECRET_CORRECTO = "secret-correcto";
    private static final String HEADER_SECRET = "X-Telegram-Bot-Api-Secret-Token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IProcesadorMensajeTelegram procesadorMensaje;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public TelegramProperties telegramProperties() {
            return new TelegramProperties("TOKEN", SECRET_CORRECTO);
        }
    }

    private TelegramUpdateDto updateConTexto() {
        return new TelegramUpdateDto(1L, new TelegramMessageDto("hola", new TelegramChatDto(123L, "camila")));
    }

    private TelegramUpdateDto updateSinTexto() {
        return new TelegramUpdateDto(1L, null);
    }

    @Test
    @WithMockUser
    void secretAusente_deberiaRetornar401YNoProcesarNada() throws Exception {
        mockMvc.perform(post("/api/public/telegram/webhook")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateConTexto())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("TELEGRAM_SECRET_INVALIDO"));

        verify(procesadorMensaje, never()).procesar(any());
    }

    @Test
    @WithMockUser
    void secretIncorrecto_deberiaRetornar401YNoProcesarNada() throws Exception {
        mockMvc.perform(post("/api/public/telegram/webhook")
                        .with(csrf())
                        .header(HEADER_SECRET, "secret-equivocado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateConTexto())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("TELEGRAM_SECRET_INVALIDO"));

        verify(procesadorMensaje, never()).procesar(any());
    }

    @Test
    @WithMockUser
    void secretCorrecto_deberiaRetornar200YProcesar() throws Exception {
        mockMvc.perform(post("/api/public/telegram/webhook")
                        .with(csrf())
                        .header(HEADER_SECRET, SECRET_CORRECTO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateConTexto())))
                .andExpect(status().isOk());

        verify(procesadorMensaje).procesar(any());
    }

    @Test
    @WithMockUser
    void updateSinTexto_deberiaRetornar200IgualYDelegarEnElProcesador() throws Exception {
        mockMvc.perform(post("/api/public/telegram/webhook")
                        .with(csrf())
                        .header(HEADER_SECRET, SECRET_CORRECTO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateSinTexto())))
                .andExpect(status().isOk());

        verify(procesadorMensaje).procesar(any());
    }
}
