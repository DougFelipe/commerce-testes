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

		BigDecimal custoTotal = calcularCustoTotal(carrinho);

		PagamentoDTO pagamento = pagamentoExternal.autorizarPagamento(cliente.getId(), custoTotal.doubleValue());
		if (!pagamento.autorizado()) {
			throw new IllegalStateException("Pagamento não autorizado.");
		}

		return new CompraDTO(true, pagamento.transacaoId(), "Compra finalizada com sucesso.");
	}

	public BigDecimal calcularCustoTotal(CarrinhoDeCompras carrinho) {
		BigDecimal custoProdutos = calcularCustoProdutos(carrinho);
		BigDecimal desconto = calcularDesconto(custoProdutos);
		BigDecimal custoFrete = calcularCustoFrete(carrinho);


		if (carrinho.getItens() == null || carrinho.getItens().isEmpty()) {
			return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		}

		return custoProdutos.subtract(desconto).add(custoFrete).setScale(2, RoundingMode.HALF_UP);
	}

	private BigDecimal calcularCustoProdutos(CarrinhoDeCompras carrinho) {
		return carrinho.getItens().stream()
				.map(item -> item.getProduto().getPreco().multiply(BigDecimal.valueOf(item.getQuantidade())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private BigDecimal calcularCustoFrete(CarrinhoDeCompras carrinho) {
		int pesoTotal = carrinho.getItens().stream()
				.mapToInt(item -> item.getProduto().getPeso() * item.getQuantidade().intValue())
				.sum();

		BigDecimal custoFrete = BigDecimal.ZERO;
		if (pesoTotal > 5 && pesoTotal <= 10) {
			custoFrete = BigDecimal.valueOf(pesoTotal).multiply(BigDecimal.valueOf(2));
		} else if (pesoTotal > 10 && pesoTotal <= 50) {
			custoFrete = BigDecimal.valueOf(pesoTotal).multiply(BigDecimal.valueOf(4));
		} else if (pesoTotal > 50) {
			custoFrete = BigDecimal.valueOf(pesoTotal).multiply(BigDecimal.valueOf(7));
		}

		Cliente cliente = carrinho.getCliente();
		switch (cliente.getTipo()) {
			case OURO:
				return BigDecimal.ZERO; // Frete grátis
			case PRATA:
				return custoFrete.multiply(BigDecimal.valueOf(0.5)); // 50% de desconto
			default:
				return custoFrete; // Sem desconto
		}
	}

	private BigDecimal calcularDesconto(BigDecimal custoProdutos) {
		if (custoProdutos.compareTo(BigDecimal.valueOf(1000)) > -1) {
			return custoProdutos.multiply(BigDecimal.valueOf(0.2)); // 20% desconto
		} else if (custoProdutos.compareTo(BigDecimal.valueOf(500)) > -1) {
			return custoProdutos.multiply(BigDecimal.valueOf(0.1)); // 10% desconto
		} else {
			return BigDecimal.ZERO; // Sem desconto
		}
	}
}

