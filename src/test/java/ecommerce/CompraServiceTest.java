package ecommerce;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.entity.ItemCompra;
import ecommerce.entity.Produto;
import ecommerce.entity.TipoCliente;
import ecommerce.service.CarrinhoDeComprasService;
import ecommerce.service.ClienteService;
import ecommerce.service.CompraService;

class CompraServiceTest {

    @InjectMocks
    private CompraService compraService;

    @Mock
    private CarrinhoDeComprasService carrinhoService;

    @Mock
    private ClienteService clienteService;

    private TipoCliente tipoCliente;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCalcularCustoTotal_SemDesconto_FreteGratis() {
        CarrinhoDeCompras carrinho = criarCarrinho(BigDecimal.valueOf(100), 2, tipoCliente.BRONZE);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);
        assertEquals(BigDecimal.valueOf(200.00).setScale(2), custoTotal);
    }

    @Test
    void testCalcularCustoTotal_ComDesconto10_FretePago() {
        CarrinhoDeCompras carrinho = criarCarrinho(BigDecimal.valueOf(600), 6, tipoCliente.BRONZE);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);
        assertEquals(BigDecimal.valueOf(1152.00).setScale(2), custoTotal);
    }

    @Test
    void testCalcularCustoTotal_ComDesconto20_FreteMetade() {
        CarrinhoDeCompras carrinho = criarCarrinho(BigDecimal.valueOf(1200), 10, tipoCliente.PRATA);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);
        assertEquals(BigDecimal.valueOf(1760.00).setScale(2), custoTotal);
    }

    @Test
    void testCalcularCustoTotal_IsencaoFrete() {
        CarrinhoDeCompras carrinho = criarCarrinho(BigDecimal.valueOf(1000), 20, tipoCliente.OURO);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);
        assertEquals(BigDecimal.valueOf(800.00).setScale(2), custoTotal);
    }

    private CarrinhoDeCompras criarCarrinho(BigDecimal precoProduto, int pesoProduto, TipoCliente tipoCliente) {
        Cliente cliente = new Cliente();
        cliente.setTipo(tipoCliente);

        Produto produto = new Produto();
        produto.setPreco(precoProduto);
        produto.setPeso(pesoProduto);

        ItemCompra item = new ItemCompra();
        item.setProduto(produto);
        item.setQuantidade((long) 1);

        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(cliente);
        carrinho.setItens(Arrays.asList(item));

        return carrinho;
    }
}