package br.com.gojopurin.backend.service;

import br.com.gojopurin.backend.dto.AuthResponse;
import br.com.gojopurin.backend.dto.LoginRequest;
import br.com.gojopurin.backend.dto.RegisterRequest;
import br.com.gojopurin.backend.exception.ApiException;
import br.com.gojopurin.backend.model.Perfil;
import br.com.gojopurin.backend.model.Usuario;
import br.com.gojopurin.backend.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Regras de login e cadastro. O controller so repassa o pedido para ca.
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        // A mesma mensagem para e-mail inexistente e senha errada: assim ninguem
        // descobre quais e-mails estao cadastrados.
        Usuario usuario = usuarioRepository.findByEmail(email)
                .filter(u -> "ATIVO".equals(u.getStatus()))
                .filter(u -> passwordEncoder.matches(request.senha(), u.getSenhaHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos"));

        return montarResposta(usuario);
    }

    // Cadastro publico: sempre cria um CLIENTE (RF-039).
    @Transactional
    public AuthResponse registrar(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();

        // RN10: e-mail unico, responde 409 se ja existir.
        if (usuarioRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Já existe uma conta com este e-mail");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome().trim());
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
        usuario.setPerfil(Perfil.CLIENTE);
        usuario.setTelefone(request.telefone().trim());
        usuario.setEndereco(request.endereco().trim());

        return montarResposta(usuarioRepository.save(usuario));
    }

    private AuthResponse montarResposta(Usuario usuario) {
        return new AuthResponse(
                tokenService.gerar(usuario),
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil().name());
    }
}
