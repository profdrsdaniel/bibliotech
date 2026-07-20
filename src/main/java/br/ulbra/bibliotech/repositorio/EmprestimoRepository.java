package br.ulbra.bibliotech.repositorio;

import br.ulbra.bibliotech.dominio.Emprestimo;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia de emprestimos.
 *
 * <p>A camada de servico depende desta interface, e nao de uma implementacao
 * concreta. Essa inversao de dependencia existe para permitir que os testes
 * substituam a persistencia real por dubles de teste criados com Mockito
 * (mocks e stubs), isolando as regras de negocio do armazenamento e permitindo
 * simular cenarios que seriam trabalhosos de montar com dados reais.</p>
 */
public interface EmprestimoRepository {

    /**
     * Gera o proximo identificador disponivel para um novo emprestimo.
     *
     * @return identificador ainda nao utilizado
     */
    Long proximoId();

    /**
     * Grava um emprestimo, inserindo ou atualizando conforme o identificador.
     *
     * @param emprestimo emprestimo a ser gravado
     * @return o emprestimo gravado
     */
    Emprestimo salvar(Emprestimo emprestimo);

    /**
     * Busca um emprestimo pelo identificador.
     *
     * @param id identificador procurado
     * @return o emprestimo encontrado, ou vazio se nao existir
     */
    Optional<Emprestimo> buscarPorId(Long id);

    /**
     * Lista os emprestimos de um socio que ainda nao foram devolvidos.
     *
     * @param socioId identificador do socio
     * @return lista de emprestimos ativos do socio, possivelmente vazia
     */
    List<Emprestimo> listarAtivosDoSocio(Long socioId);

    /**
     * Lista todos os emprestimos gravados, ativos ou ja devolvidos.
     *
     * @return lista de todos os emprestimos, possivelmente vazia
     */
    List<Emprestimo> listarTodos();
}
