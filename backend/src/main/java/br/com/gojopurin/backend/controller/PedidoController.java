package br.com.gojopurin.backend.controller;

import br.com.gojopurin.backend.dto.EnderecoResponse;
import br.com.gojopurin.backend.dto.PaginaResponse;
import br.com.gojopurin.backend.dto.PedidoRequest;
import br.com.gojopurin.backend.dto.PedidoResponse;
import br.com.gojopurin.backend.dto.PedidoStatusResponse;
import br.com.gojopurin.backend.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Pedidos do cliente. @PreAuthorize na classe vale para todos os metodos:
// so quem tem o perfil CLIENTE entra aqui (RF-040).
// O "Authentication" que chega em cada metodo e o usuario identificado pelo
// token; getName() devolve o e-mail dele.
@RestController
@RequestMapping("/api/pedidos")
@PreAuthorize("hasRole('CLIENTE')")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // Checkout: transforma o carrinho em pedido.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse criar(@RequestBody @Valid PedidoRequest request, Authentication autenticacao) {
        return pedidoService.criar(autenticacao.getName(), request);
    }

    // Historico do cliente logado, mais recente primeiro.
    @GetMapping("/meus")
    public PaginaResponse<PedidoResponse> meus(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication autenticacao) {
        int tamanho = Math.max(1, Math.min(size, 50));
        return pedidoService.listarDoCliente(autenticacao.getName(), PageRequest.of(Math.max(page, 0), tamanho));
    }

    // Endereco do cadastro, para preencher o checkout.
    @GetMapping("/endereco-cadastrado")
    public EnderecoResponse enderecoCadastrado(Authentication autenticacao) {
        return pedidoService.enderecoCadastrado(autenticacao.getName());
    }

    @GetMapping("/{id}")
    public PedidoResponse detalhar(@PathVariable Long id, Authentication autenticacao) {
        return pedidoService.detalhar(autenticacao.getName(), id);
    }

    @GetMapping("/{id}/status")
    public PedidoStatusResponse status(@PathVariable Long id, Authentication autenticacao) {
        return pedidoService.status(autenticacao.getName(), id);
    }
}
