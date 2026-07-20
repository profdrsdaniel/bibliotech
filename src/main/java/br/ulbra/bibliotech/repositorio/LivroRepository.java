package br.ulbra.bibliotech.repositorio;

import br.ulbra.bibliotech.dominio.Livro;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia de livros.
 *
 * <p>Assim como {@link EmprestimoRepository}, a camada de API depende desta
 * interface e nao de uma implementacao concreta, o que permite trocar o
 * armazenamento e usar dubles de teste.</p>
 */
public interface LivroRepository {

    /**
     * Grava um livro, inserindo ou atualizando conforme o identificador.
     *
     * @param livro livro a ser gravado
     * @return o livro gravado
     */
    Livro salvar(Livro livro);

    /**
     * Busca um livro pelo identificador.
     *
     * @param id identificador procurado
     * @return o livro encontrado, ou vazio se nao existir
     */
    Optional<Livro> buscarPorId(Long id);

    /**
     * Lista todos os livros do acervo.
     *
     * @return lista de livros, possivelmente vazia
     */
    List<Livro> listarTodos();
}
