package ecommerce.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.math.RoundingMode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ecommerce.dto.CompraDTO;
import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.EstoqueBaixaDTO;
import ecommerce.dto.PagamentoDTO;
import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.entity.ItemCompra;
import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;
import jakarta.transaction.Transactional;

@Service
public class CompraService {

	private final CarrinhoDeComprasService carrinhoService;
	private final ClienteService clienteService;

	private final IEstoqueExternal estoqueExternal;
	private final IPagamentoExternal pagamentoExternal;

	@Autowired
	public CompraService(CarrinhoDeComprasService carrinhoService, ClienteService clienteService,
						 IEstoqueExternal estoqueExternal, IPagamentoExternal pagamentoExternal) {
		this.carrinhoService = carrinhoService;
		this.clienteService = clienteService;

		this.estoqueExternal = estoqueExternal;
		this.pagamentoExternal = pagamentoExternal;
	}

	@Transactional
	public CompraDTO finalizarCompra(Long carrinhoId, Long clienteId) {
		Cliente cliente = clienteService.buscarPorId(clienteId);
		CarrinhoDeCompras carrinho = carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente);

		List<Long> produtosIds = carrinho.getItens().stream().map(i -> i.getProduto().getId())
				.collect(Collectors.toList());
		List<Long> produtosQtds = carrinho.getItens().stream().map(i -> i.getQuantidade()).collect(Collectors.toList());

		DisponibilidadeDTO disponibilidade = estoqueExternal.verificarDisponibilidade(produtosIds, produtosQtds);

		if (!disponibilidade.disponivel()) {
			throw new IllegalStateException("Itens fora de estoque.");
		}

		BigDecimal custoTotal = calcularCustoTotal(carrinho);

		PagamentoDTO pagamento = pagamentoExternal.autorizarPagamento(cliente.getId(), custoTotal.doubleValue());

		if (!pagamento.autorizado()) {
			throw new IllegalStateException("Pagamento não autorizado.");
		}

		EstoqueBaixaDTO baixaDTO = estoqueExternal.darBaixa(produtosIds, produtosQtds);

		if (!baixaDTO.sucesso()) {
			pagamentoExternal.cancelarPagamento(cliente.getId(), pagamento.transacaoId());
			throw new IllegalStateException("Erro ao dar baixa no estoque.");
		}

		CompraDTO compraDTO = new CompraDTO(true, pagamento.transacaoId(), "Compra finalizada com sucesso.");

		return compraDTO;
	}

	public BigDecimal calcularCustoTotal(CarrinhoDeCompras carrinho) {

		//Calcula o valor inicial do Carrinho de compras
		BigDecimal custoProdutos = calcularCustoProdutos(carrinho);

		//Calcula o valor de desconto
		BigDecimal desconto = calcularDesconto(custoProdutos);

		//Subtrai o desconto do valor do carrinho
		custoProdutos = custoProdutos.subtract(desconto);

		//Calcula o valor do Frete
		BigDecimal custoFrete = calcularCustoFrete(carrinho);

		//Valor final = Valor do carrinho (já com desconto) + valor do frete (se houver)
		BigDecimal custoFinalProdutos = custoProdutos.add(custoFrete).setScale(2, RoundingMode.HALF_UP);
		//O comando final garante que haverá um arredondamento para cima com duas casas decimais

		//Retorna o valor final
		return custoFinalProdutos;
	}

	private BigDecimal calcularCustoProdutos(CarrinhoDeCompras carrinho) { // calcular preço e quantidade dos produtos do carrinho
		return carrinho.getItens().stream()
				.map(item -> item.getProduto().getPreco().multiply(BigDecimal.valueOf(item.getQuantidade()))) //usando o multiply pra operar com preço
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private BigDecimal calcularCustoFrete(CarrinhoDeCompras carrinho) { // calcular o frete desses produtos de acordo com o peso
		int pesoTotal = carrinho.getItens().stream()
				.mapToInt(item -> item.getProduto().getPeso() * item.getQuantidade().intValue())
				.sum();

		BigDecimal custoFrete;
		if (pesoTotal <= 5) { // frete grátis
			custoFrete = BigDecimal.ZERO;

		} else if (pesoTotal <= 10) { // R$2,00 por kg
			custoFrete = BigDecimal.valueOf(pesoTotal).multiply(BigDecimal.valueOf(2));

		} else if (pesoTotal <= 50) { // R$4,00 por kg
			custoFrete = BigDecimal.valueOf(pesoTotal).multiply(BigDecimal.valueOf(4));

		} else { // R$7,00 por kg
			custoFrete = BigDecimal.valueOf(pesoTotal).multiply(BigDecimal.valueOf(7));

		}

		// Categorizando cliente
		Cliente cliente = carrinho.getCliente();
		switch (cliente.getTipo()) {
			case OURO: // Desconto de 100% no frete (gratis)
				return BigDecimal.ZERO;

			case PRATA: // Desconto de 50% no frete
				return custoFrete.multiply(BigDecimal.valueOf(0.5));

			case BRONZE: // Sem desconto
			default:
				return custoFrete;
		}
	}

	private BigDecimal calcularDesconto(BigDecimal custoProdutos) { // Descontos pelo valor do carrinho

		if (custoProdutos.compareTo(BigDecimal.valueOf(1000)) > 0) { // Recebe 20% de desconto
			return custoProdutos.multiply(BigDecimal.valueOf(0.2));

		} else if (custoProdutos.compareTo(BigDecimal.valueOf(500)) > 0) { // Recebe 10% de desconto
			return custoProdutos.multiply(BigDecimal.valueOf(0.1));

		} else { // Não recebe desconto
			return BigDecimal.ZERO;

		}
	}
}
