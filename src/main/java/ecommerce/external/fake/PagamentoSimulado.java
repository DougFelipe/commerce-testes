package ecommerce.external.fake;

import org.springframework.stereotype.Service;
import ecommerce.dto.PagamentoDTO;
import ecommerce.external.IPagamentoExternal;

@Service
public class PagamentoSimulado implements IPagamentoExternal {

    @Override
    public PagamentoDTO autorizarPagamento(Long clienteId, Double custoTotal) {
        return null;
    }

    @Override
    public void cancelarPagamento(Long pagamentoId, Long usuarioId) {
        // Implementação simulada do cancelamento de pagamento
        System.out.println("Cancelando pagamento com ID: " + pagamentoId + " para o usuário com ID: " + usuarioId);
        // Lógica fictícia para simular o cancelamento
    }


}
