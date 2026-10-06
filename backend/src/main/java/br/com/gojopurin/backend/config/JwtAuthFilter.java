package br.com.gojopurin.backend.config;

import br.com.gojopurin.backend.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// Roda antes de cada chamada a API. Se vier o cabecalho
// "Authorization: Bearer <token>" com um token valido, avisa o Spring Security
// quem e o usuario e qual o perfil dele.
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    public JwtAuthFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String cabecalho = request.getHeader("Authorization");

        if (cabecalho != null && cabecalho.startsWith("Bearer ")) {
            String token = cabecalho.substring(7);

            tokenService.validar(token).ifPresent(jwt -> {
                // O Spring Security espera o perfil com o prefixo ROLE_.
                String perfil = "ROLE_" + jwt.getClaim("role").asString();
                UsernamePasswordAuthenticationToken autenticacao = new UsernamePasswordAuthenticationToken(
                        jwt.getSubject(), null, List.of(new SimpleGrantedAuthority(perfil)));
                SecurityContextHolder.getContext().setAuthentication(autenticacao);
            });
        }

        // Segue para o proximo passo, com ou sem usuario identificado.
        filterChain.doFilter(request, response);
    }
}
