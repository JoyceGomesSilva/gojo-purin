package br.com.gojopurin.backend.service;

import br.com.gojopurin.backend.dto.PaginaResponse;
import br.com.gojopurin.backend.dto.UsuarioCriarRequest;
import br.com.gojopurin.backend.dto.UsuarioEditarRequest;
import br.com.gojopurin.backend.dto.UsuarioResponse;
import br.com.gojopurin.backend.exception.ApiException;
import br.com.gojopurin.backend.model.Perfil;
import br.com.gojopurin.backend.model.Usuario;
import br.com.gojopurin.backend.repository.UsuarioRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

// Regras do CRUD de usuarios internos (RF-041): o admin cria, edita,
// desativa e reativa gerentes e cozinheiros.
@Service
public class UsuarioService {

    // Este CRUD so enxerga estes dois perfis. Clientes e o proprio admin
    // ficam de fora: para o painel e como se nao existissem.
    private static final List<Perfil> PERFIS_INTERNOS = List.of(Perfil.GERENTE, Perfil.COZINHEIRO);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Lista paginada (RNF08), em ordem alfabetica, com filtros opcionais.
    @Transactional(readOnly = true)
    public PaginaResponse<UsuarioResponse> listar(String perfil, String status, int pagina, int tamanho) {
        // Converte o texto do filtro antes de montar a consulta. null = sem filtro.
        Perfil perfilFiltro = temTexto(perfil) ? paraPerfil(perfil) : null;

        Specification<Usuario> filtro = (root, query, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();
            // Sempre: so gerentes e cozinheiros.
            condicoes.add(root.get("perfil").in(PERFIS_INTERNOS));
            if (perfilFiltro != null) {
                condicoes.add(cb.equal(root.get("perfil"), perfilFiltro));
            }
            if (temTexto(status)) {
                condicoes.add(cb.equal(root.get("status"), status.trim().toUpperCase()));
            }
            return cb.and(condicoes.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(pagina, tamanho, Sort.by("nome"));
        return PaginaResponse.de(usuarioRepository.findAll(filtro, pageable), UsuarioResponse::de);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse detalhar(Long id) {
        return UsuarioResponse.de(buscar(id));
    }

    @Transactional
    public UsuarioResponse criar(UsuarioCriarRequest request) {
        String email = request.email().trim().toLowerCase();

        // RN10: e-mail unico, responde 409 se ja existir.
        if (usuarioRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Já existe uma conta com este e-mail");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome().trim());
        usuario.setEmail(email);
        // Nunca gravamos a senha, so o hash BCrypt dela (RNF01).
        usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
        usuario.setPerfil(paraPerfil(request.perfil()));
        usuario.setTelefone(limpar(request.telefone()));

        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse editar(Long id, UsuarioEditarRequest request) {
        Usuario usuario = buscar(id);
        String email = request.email().trim().toLowerCase();

        // RN10 na edicao: o e-mail novo nao pode ser de OUTRA pessoa.
        if (usuarioRepository.existsByEmailAndIdNot(email, id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Já existe uma conta com este e-mail");
        }

        usuario.setNome(request.nome().trim());
        usuario.setEmail(email);
        usuario.setPerfil(paraPerfil(request.perfil()));
        usuario.setTelefone(limpar(request.telefone()));

        // Senha so muda se o admin digitou uma nova.
        if (temTexto(request.senha())) {
            usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
        }

        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }

    // "Excluir" aqui e desativar (soft delete): a linha continua no banco,
    // porque pedidos e movimentacoes de estoque apontam para o usuario.
    // Quem esta INATIVO nao consegue mais entrar.
    @Transactional
    public void desativar(Long id) {
        mudarStatus(id, "INATIVO");
    }

    @Transactional
    public UsuarioResponse reativar(Long id) {
        return UsuarioResponse.de(mudarStatus(id, "ATIVO"));
    }

    private Usuario mudarStatus(Long id, String status) {
        Usuario usuario = buscar(id);
        usuario.setStatus(status);
        return usuarioRepository.save(usuario);
    }

    // Busca um usuario interno. Se o id for de um cliente ou do admin,
    // responde 404 do mesmo jeito: este CRUD nao mexe neles.
    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .filter(usuario -> PERFIS_INTERNOS.contains(usuario.getPerfil()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }

    // Transforma o texto "GERENTE" no valor Perfil.GERENTE, recusando
    // qualquer coisa fora dos perfis internos.
    private Perfil paraPerfil(String texto) {
        return PERFIS_INTERNOS.stream()
                .filter(perfil -> perfil.name().equals(texto.trim().toUpperCase()))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "O perfil deve ser GERENTE ou COZINHEIRO"));
    }

    // Campo opcional: texto vazio vira null no banco.
    private String limpar(String texto) {
        return temTexto(texto) ? texto.trim() : null;
    }

    private boolean temTexto(String texto) {
        return texto != null && !texto.isBlank();
    }
}
