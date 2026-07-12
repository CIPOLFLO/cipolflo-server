package com.cipolflo.server.integraciones.telegram.client;

import com.cipolflo.server.integraciones.mensajeria.log.CanalMensaje;
import com.cipolflo.server.integraciones.mensajeria.log.EnvioMensajeLogRegistrar;
import com.cipolflo.server.integraciones.mensajeria.log.EstadoEnvioMensaje;
import com.cipolflo.server.integraciones.mensajeria.log.TipoEventoMensaje;
import com.cipolflo.server.integraciones.telegram.exception.TelegramEnvioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class TelegramApiClientTest {

    private static final String URL_SEND_MESSAGE = "https://api.telegram.org/botTOKEN/sendMessage";

    @Mock
    private EnvioMensajeLogRegistrar logRegistrar;

    private MockRestServiceServer server;
    private TelegramApiClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        TelegramProperties properties = new TelegramProperties("TOKEN", "SECRET");
        client = new TelegramApiClient(builder, properties, logRegistrar);
    }

    @Test
    void enviar_exitoso_registraEnviado() {
        server.expect(times(1), requestTo(URL_SEND_MESSAGE))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"ok\":true}", MediaType.APPLICATION_JSON));

        client.enviar("123", "hola", TipoEventoMensaje.RESPUESTA_CONSULTA);

        server.verify();
        verify(logRegistrar).registrar(CanalMensaje.TELEGRAM, "123", TipoEventoMensaje.RESPUESTA_CONSULTA,
                EstadoEnvioMensaje.ENVIADO, null);
    }

    @Test
    void enviar_error5xx_reintentaYFallaTrasAgotarReintentos() {
        server.expect(times(3), requestTo(URL_SEND_MESSAGE))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        assertThrows(TelegramEnvioException.class,
                () -> client.enviar("123", "hola", TipoEventoMensaje.RESPUESTA_CONSULTA));

        server.verify();
        verify(logRegistrar).registrar(any(), any(), any(), eq(EstadoEnvioMensaje.FALLIDO), anyString());
    }

    @Test
    void enviar_error4xx_noReintenta() {
        server.expect(times(1), requestTo(URL_SEND_MESSAGE))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withBadRequest());

        assertThrows(TelegramEnvioException.class,
                () -> client.enviar("123", "hola", TipoEventoMensaje.RESPUESTA_CONSULTA));

        server.verify();
        verify(logRegistrar).registrar(any(), any(), any(), eq(EstadoEnvioMensaje.FALLIDO), anyString());
    }
}
