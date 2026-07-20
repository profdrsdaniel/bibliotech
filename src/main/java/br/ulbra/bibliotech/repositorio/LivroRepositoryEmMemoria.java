package br.ulbra.bibliotech.repositorio;

import br.ulbra.bibliotech.dominio.Livro;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementacao de {@link LivroRepository} que guarda os livros em memoria.
 *
 * <p>Usa um {@link LinkedHashMap} para preservar a ordem de insercao, deixando as
 * listagens sempre na mesma sequencia. Os dados existem apenas enquanto a
 * aplicacao estiver no ar.</p>
 */
public class LivroRepositoryEmMemoria implements LivroRepository {

    private final Map<Long, Livro> livros = new LinkedHashMap<>();

    @Override
    public Livro salvar(Livro livro) {
        if (livro == null) {
            throw new IllegalArgumentException("Livro nao pode ser nulo");
        }
        livros.put(livro.getId(), livro);
        return livro;
    }

    @Override
    public Optional<Livro> buscarPorId(Long id) {
        return Optional.ofNullable(livros.get(id));
    }

    @Override
    public List<Livro> listarTodos() {
        return new ArrayList<>(livros.values());
    }
}
