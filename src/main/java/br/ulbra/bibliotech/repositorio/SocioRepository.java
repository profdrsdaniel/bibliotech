package br.ulbra.bibliotech.repositorio;

import br.ulbra.bibliotech.dominio.Socio;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia de socios.
 *
 * <p>Assim como {@link EmprestimoRepository}, a camada de API depende desta
 * interface e nao de uma implementacao concreta, o que permite trocar o
 * armazenamento e usar dubles de teste.</p>
 */
public interface SocioRepository {

    /**
     * Grava um socio, inserindo ou atualizando conforme o identificador.
     *
     * @param socio socio a ser gravado
     * @return o socio gravado
     */
    Socio salvar(Socio socio);

    /**
     * Busca um socio pelo identificador.
     *
     * @param id identificador procurado
     * @return o socio encontrado, ou vazio se nao existir
     */
    Optional<Socio> buscarPorId(Long id);

    /**
     * Lista todos os socios cadastrados.
     *
     * @return lista de socios, possivelmente vazia
     */
    List<Socio> listarTodos();
}
