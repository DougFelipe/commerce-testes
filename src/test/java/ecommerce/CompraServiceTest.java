package ecommerce;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
import ecommerce.external.fake.EstoqueSimulado;
import ecommerce.external.fake.PagamentoSimulado;
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

    @Mock 
    private EstoqueSimulado estoqueExternal;

    @Mock
    private PagamentoSimulado pagamentoExternal;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDependenciasInjetadas() {
    assertNotNull(compraService);
    assertNotNull(carrinhoService);
    assertNotNull(clienteService);
    assertNotNull(estoqueExternal);
    assertNotNull(pagamentoExternal);
    }

    @Test
    void testCalcularCustoTotal_SemDesconto_FreteGratis() {
        Produto produto = criarProduto("Produto A", BigDecimal.valueOf(100), 2);
        CarrinhoDeCompras carrinho = criarCarrinho(
                TipoCliente.BRONZE,
                Collections.singletonList(produto),
                Collections.singletonList(2L)
        );

        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);
        assertEquals(BigDecimal.valueOf(200.00).setScale(2, RoundingMode.HALF_UP), custoTotal);
    }

    @Test
    void testCalcularCustoTotal_ComDesconto10_FretePago() {
        Produto produto = criarProduto("Produto B", BigDecimal.valueOf(600), 6);
        CarrinhoDeCompras carrinho = criarCarrinho(
                TipoCliente.BRONZE,
                Collections.singletonList(produto),
                Collections.singletonList(1L)
        );

        BigDecimal custoProdutos = BigDecimal.valueOf(600);
        BigDecimal desconto = custoProdutos.multiply(BigDecimal.valueOf(0.1));
        BigDecimal frete = BigDecimal.valueOf(6).multiply(BigDecimal.valueOf(2));
        BigDecimal esperado = custoProdutos.subtract(desconto).add(frete).setScale(2, RoundingMode.HALF_UP);

        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);
        assertEquals(esperado, custoTotal);
    }

    @Test
    void testCalcularCustoTotal_ComDesconto20_FreteMetade() {
        Produto produto1 = criarProduto("Produto C", BigDecimal.valueOf(800), 5);
        Produto produto2 = criarProduto("Produto D", BigDecimal.valueOf(400), 5);
        CarrinhoDeCompras carrinho = criarCarrinho(
                TipoCliente.PRATA,
                Arrays.asList(produto1, produto2),
                Arrays.asList(1L, 1L)
        );

        BigDecimal custoProdutos = BigDecimal.valueOf(800).add(BigDecimal.valueOf(400));
        BigDecimal desconto = custoProdutos.multiply(BigDecimal.valueOf(0.2));
        BigDecimal frete = BigDecimal.valueOf(10).multiply(BigDecimal.valueOf(2)).multiply(BigDecimal.valueOf(0.5));
        BigDecimal esperado = custoProdutos.subtract(desconto).add(frete).setScale(2, RoundingMode.HALF_UP);

        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);
        assertEquals(esperado, custoTotal);
    }

    @Test
    void testCalcularCustoTotal_IsencaoFrete() {
        Produto produto = criarProduto("Produto E", BigDecimal.valueOf(1000), 20);
        CarrinhoDeCompras carrinho = criarCarrinho(
                TipoCliente.OURO,
                Collections.singletonList(produto),
                Collections.singletonList(1L)
        );

        BigDecimal custoProdutos = BigDecimal.valueOf(1000);
        BigDecimal desconto = custoProdutos.multiply(BigDecimal.valueOf(0.2));
        BigDecimal esperado = custoProdutos.subtract(desconto).setScale(2, RoundingMode.HALF_UP);

        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);
        assertEquals(esperado, custoTotal);
    }

    // Novos casos de teste

    @Test
    void testCarrinhoVazio() {
        CarrinhoDeCompras carrinho = criarCarrinho(
                TipoCliente.BRONZE,
                Collections.emptyList(),
                Collections.emptyList()
        );

        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);
        assertEquals(BigDecimal.ZERO.setScale(2), custoTotal);
    }

    @Test
    void testProdutoComPrecoZero() {
        Produto produto = criarProduto("Produto F", BigDecimal.ZERO, 2);
        CarrinhoDeCompras carrinho = criarCarrinho(
                TipoCliente.BRONZE,
                Collections.singletonList(produto),
                Collections.singletonList(1L)
        );

        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);
        assertEquals(BigDecimal.ZERO.setScale(2), custoTotal);
    }

    @Test
    void testProdutoComQuantidadeZero() {
        Produto produto = criarProduto("Produto G", BigDecimal.valueOf(100), 2);
        CarrinhoDeCompras carrinho = criarCarrinho(
                TipoCliente.BRONZE,
                Collections.singletonList(produto),
                Collections.singletonList(0L)
        );

        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);
        assertEquals(BigDecimal.ZERO.setScale(2), custoTotal);
    }

    @Test
    void testFreteMuitoAlto() {
        Produto produto = criarProduto("Produto H", BigDecimal.valueOf(100), 100);
        CarrinhoDeCompras carrinho = criarCarrinho(
                TipoCliente.BRONZE,
                Collections.singletonList(produto),
                Collections.singletonList(1L)
        );

        BigDecimal custoProdutos = BigDecimal.valueOf(100);
        BigDecimal frete = BigDecimal.valueOf(100).multiply(BigDecimal.valueOf(7)); // 100kg * R$7/kg
        BigDecimal esperado = custoProdutos.add(frete).setScale(2, RoundingMode.HALF_UP);

        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);
        assertEquals(esperado, custoTotal);
    }

    // Métodos auxiliares

    private Produto criarProduto(String nome, BigDecimal preco, int peso) {
        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setPreco(preco);
        produto.setPeso(peso);
        return produto;
    }

    private CarrinhoDeCompras criarCarrinho(TipoCliente tipoCliente, List<Produto> produtos, List<Long> quantidades) {

        Cliente cliente = new Cliente();
        cliente.setTipo(tipoCliente);

        List<ItemCompra> itens = criarItensCompra(produtos, quantidades);

        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(cliente);
        carrinho.setItens(itens);

        return carrinho;
    }

    private List<ItemCompra> criarItensCompra(List<Produto> produtos, List<Long> quantidades) {
        return produtos.stream()
                .map(produto -> {
                    int index = produtos.indexOf(produto);
                    ItemCompra item = new ItemCompra();
                    item.setProduto(produto);
                    item.setQuantidade(quantidades.get(index));
                    return item;
                })
                .toList();
    }
}
