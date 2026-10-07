package br.com.gojopurin.backend.service;

import br.com.gojopurin.backend.dto.AdminCategoriaRequest;
import br.com.gojopurin.backend.dto.AdminCategoriaResponse;
import br.com.gojopurin.backend.exception.ApiException;
import br.com.gojopurin.backend.model.Categoria;
import br.com.gojopurin.backend.repository.CategoriaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Regras do CRUD de categorias no painel (RF-009).
@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    // Todas as categorias, ativas e inativas, na ordem do cardapio.
    // Sao poucas, entao a lista vem inteira, sem paginacao.
    @Transactional(readOnly = true)
    public List<AdminCategoriaResponse> listar() {
        return categoriaRepository.findAllByOrderByOrdemAscNomeAsc().stream()
                .map(AdminCategoriaResponse::de)
                .toList();
    }

    @Transactional
    public AdminCategoriaResponse criar(AdminCategoriaRequest request) {
        String nome = request.nome().trim();
        if (categoriaRepository.existsByNomeIgnoreCase(nome)) {
            throw new ApiException(HttpStatus.CONFLICT, "Já existe uma categoria com este nome");
        }

        Categoria categoria = new Categoria();
        preencher(categoria, request);
        return AdminCategoriaResponse.de(categoriaRepository.save(categoria));
    }

    @Transactional
    public AdminCategoriaResponse editar(Long id, AdminCategoriaRequest request) {
        Categoria categoria = buscar(id);
        if (categoriaRepository.existsByNomeIgnoreCaseAndIdNot(request.nome().trim(), id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Já existe uma categoria com este nome");
        }

        preencher(categoria, request);
        return AdminCategoriaResponse.de(categoriaRepository.save(categoria));
    }

    // RN06: nunca apagamos a linha. A categoria fica INATIVA e some do
    // cardapio publico junto com os pratos dela (as consultas do cardapio
    // ja exigem categoria ATIVA).
    @Transactional
    public void desativar(Long id) {
        Categoria categoria = buscar(id);
        categoria.setStatus("INATIVO");
        categoriaRepository.save(categoria);
    }

    // Copia os dados do request para a Entity. Serve para criar e para editar.
    private void preencher(Categoria categoria, AdminCategoriaRequest request) {
        categoria.setNome(request.nome().trim());
        categoria.setDescricao(limpar(request.descricao()));
        categoria.setOrdem(request.ordem());
        categoria.setStatus(request.status());
    }

    private Categoria buscar(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Categoria não encontrada"));
    }

    // Campo opcional: texto vazio vira null no banco.
    private String limpar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
