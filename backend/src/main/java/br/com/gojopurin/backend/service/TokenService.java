package br.com.gojopurin.backend.service;

import br.com.gojopurin.backend.model.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

// Cria e confere os tokens JWT.
// O token e um texto assinado com a chave secreta: qualquer um consegue ler o
// conteudo, mas so quem tem a chave consegue criar ou alterar um token valido.
@Service
public class TokenService {

    private static final String EMISSOR = "gojo-purin";

    private final Algorithm algoritmo;
    private final long horasDeValidade;

    public TokenService(@Value("${app.jwt.secret}") String segredo,
                        @Value("${app.jwt.expiration-hours}") long horasDeValidade) {
        this.algoritmo = Algorithm.HMAC256(segredo);
        this.horasDeValidade = horasDeValidade;
    }

    public String gerar(Usuario usuario) {
        return JWT.create()
                .withIssuer(EMISSOR)
                .withSubject(usuario.getEmail())
                .withClaim("id", usuario.getId())
                .withClaim("nome", usuario.getNome())
                .withClaim("role", usuario.getPerfil().name())
                .withExpiresAt(Instant.now().plus(Duration.ofHours(horasDeValidade)))
                .sign(algoritmo);
    }

    // Devolve o token decodificado, ou vazio se for falso, alterado ou vencido.
    public Optional<DecodedJWT> validar(String token) {
        try {
            return Optional.of(JWT.require(algoritmo).withIssuer(EMISSOR).build().verify(token));
        } catch (JWTVerificationException ex) {
            return Optional.empty();
        }
    }
}
