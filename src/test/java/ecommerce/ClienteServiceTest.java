package ecommerce;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import ecommerce.entity.Cliente;
import ecommerce.repository.ClienteRepository;
import ecommerce.service.ClienteService;

class ClienteServiceTest {

    private final ClienteRepository repository = mock(ClienteRepository.class);
    private final ClienteService service = new ClienteService(repository);

    @Test
    void buscarPorId_DeveRetornarCliente_QuandoIdExistir() {
        // Arrange
        Long clienteId = 1L;
        Cliente cliente = new Cliente();
        cliente.setId(clienteId);
        when(repository.findById(clienteId)).thenReturn(Optional.of(cliente));

        // Act
        Cliente resultado = service.buscarPorId(clienteId);

        // Assert
        assertNotNull(resultado);
        assertEquals(clienteId, resultado.getId());
        verify(repository, times(1)).findById(clienteId);
    }

    @Test
    void buscarPorId_DeveLancarExcecao_QuandoIdNaoExistir() {
        // Arrange
        Long clienteId = 1L;
        when(repository.findById(clienteId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.buscarPorId(clienteId);
        });

        assertEquals("Cliente não encontrado", exception.getMessage());
        verify(repository, times(1)).findById(clienteId);
    }
}
