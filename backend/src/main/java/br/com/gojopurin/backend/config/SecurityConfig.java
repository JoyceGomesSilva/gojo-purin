package br.com.gojopurin.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Define quem pode acessar o que.
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // libera o uso de @PreAuthorize nos controllers
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF protege logins por cookie. Como usamos token no cabecalho, nao se aplica.
                .csrf(csrf -> csrf.disable())
                // Usa as regras de CORS que ja estao no CorsConfig.
                .cors(Customizer.withDefaults())
                // Sem sessao no servidor: cada chamada se identifica pelo token.
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(regras -> regras
                        // Publico: login, cadastro, cardapio e documentacao.
                        .requestMatchers("/api/auth/**", "/api/cardapio/**", "/api/ping").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        // Painel: so a equipe interna. Usuarios internos: so o ADMIN.
                        .requestMatchers("/api/admin/usuarios/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "GERENTE", "COZINHEIRO")
                        // Todo o resto exige estar logado.
                        .anyRequest().authenticated())
                // Sem token (ou com token invalido) responde 401, que o Angular usa
                // para mandar o usuario para a tela de login.
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // BCrypt transforma a senha em um codigo que nao da para desfazer (RNF01).
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
