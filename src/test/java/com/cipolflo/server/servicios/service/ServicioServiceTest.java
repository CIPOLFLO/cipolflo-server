package com.cipolflo.server.servicios.service;

import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.servicios.dto.ServicioResponseDto;
import com.cipolflo.server.servicios.repository.ServicioRepository;
import com.cipolflo.server.shared.enums.Procedencia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.junit.jupiter.MockitoExtension;
import com.cipolflo.server.servicios.exception.ServicioNotFoundException;
import java.math.BigDecimal;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)

class ServicioServiceTest {


   @Mock
    private ServicioRepository servicioRepository;

   @InjectMocks
    private ServicioService servicioService;

   @Test
    void deberiaRetornarDetalleServicioCuandoExiste(){
       Long servicioId = 1L;

       Servicio servicio = new Servicio();
       servicio.setId(servicioId);
       servicio.setNombre("Cabaña");
       servicio.setProcedencia(Procedencia.CAMPING);
       servicio.setCapacidad(4);
       servicio.setCantidad(2);
       servicio.setPrecioSocio(BigDecimal.valueOf(1500));
       servicio.setPrecioParticular(BigDecimal.valueOf(2500));
       servicio.setHabilitado(true);
       servicio.setModalidadPrecio(ModalidadPrecio.POR_DIA);

       when(servicioRepository.findById(servicioId))
               .thenReturn(Optional.of(servicio));

       ServicioResponseDto resultado = servicioService.getDetalleServicio(servicioId);

       assertNotNull(resultado);
       assertEquals(servicioId, resultado.getId());
       assertEquals("Cabaña", resultado.getNombre());
       assertEquals(Procedencia.CAMPING,resultado.getProcedencia());
       assertEquals(4,resultado.getCapacidad());
       assertEquals(2,resultado.getCantidad());
       assertEquals(BigDecimal.valueOf(1500),resultado.getPrecioSocio());
       assertEquals(BigDecimal.valueOf(2500),resultado.getPrecioParticular());
       assertEquals(true,resultado.getHabilitado());
       assertEquals(ModalidadPrecio.POR_DIA,resultado.getModalidadPrecio());
       verify(servicioRepository).findById(servicioId);
   }
    @Test
    void deberiaLanzarErrorCuandoElServicioNoExiste() {
        Long servicioId = 99L;
        when(servicioRepository.findById(servicioId))
                .thenReturn(Optional.empty());

        ServicioNotFoundException exception =
                assertThrows(ServicioNotFoundException.class, () -> {
                    servicioService.getDetalleServicio(servicioId);
                });

        assertEquals(
                "Servicio no encontrado con id: " + servicioId,
                exception.getMessage()
        );

        verify(servicioRepository).findById(servicioId);
         }
}




