package ecommerce;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.repository.CarrinhoDeComprasRepository;
import ecommerce.service.CarrinhoDeComprasService;

class CarrinhoDeComprasServiceTest {

    private final CarrinhoDeComprasRepository repository = mock(CarrinhoDeComprasRepository.class);
    private final CarrinhoDeComprasService service = new CarrinhoDeComprasService(repository);

    @Test
    void buscarPorCarrinhoIdEClienteId_DeveRetornarCarrinho_QuandoCarrinhoExistir() {
        // Arrange
        Long carrinhoId = 1L;
        Cliente cliente = new Cliente();
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        when(repository.findByIdAndCliente(carrinhoId, cliente)).thenReturn(Optional.of(carrinho));

        // Act
        CarrinhoDeCompras resultado = service.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente);

        // Assert
        assertNotNull(resultado);
        verify(repository, times(1)).findByIdAndCliente(carrinhoId, cliente);
    }

    @Test
    void buscarPorCarrinhoIdEClienteId_DeveLancarExcecao_QuandoCarrinhoNaoExistir() {
        // Arrange
        Long carrinhoId = 1L;
        Cliente cliente = new Cliente();
        when(repository.findByIdAndCliente(carrinhoId, cliente)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente);
        });

        assertEquals("Carrinho não encontrado.", exception.getMessage());
        verify(repository, times(1)).findByIdAndCliente(carrinhoId, cliente);
    }
}
