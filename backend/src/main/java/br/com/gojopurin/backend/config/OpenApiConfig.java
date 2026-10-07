package br.com.gojopurin.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

// Configura a pagina do Swagger (/swagger-ui.html, RNF11).
// O @SecurityScheme faz aparecer o botao "Authorize": quem esta testando
// cola ali o token recebido no login e o Swagger passa a envia-lo em todas
// as chamadas, no cabecalho "Authorization: Bearer <token>".
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Gojo Purin API",
                version = "1.0",
                description = "API da dark kitchen Gojo Purin. Para testar os endpoints protegidos: "
                        + "faça POST /api/auth/login, copie o campo token da resposta e cole no botão Authorize."),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class OpenApiConfig {
}
