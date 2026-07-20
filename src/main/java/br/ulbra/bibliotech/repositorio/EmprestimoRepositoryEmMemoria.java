package br.ulbra.bibliotech.repositorio;

import br.ulbra.bibliotech.dominio.Emprestimo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementacao de {@link EmprestimoRepository} que guarda os emprestimos em
 * memoria.
 *
 * <p>Usa um {@link LinkedHashMap} para preservar a ordem de insercao, de modo que
 * as listagens saiam sempre na mesma sequencia e a saida do sistema seja
 * reproduzivel. Os dados existem apenas enquanto a aplicacao estiver no ar.</p>
 */
public class EmprestimoRepositoryEmMemoria implements EmprestimoRepository {

    private final Map<Long, Emprestimo> emprestimos = new LinkedHashMap<>();
    private long contadorId = 0L;

    @Override
    public Long proximoId() {
        return ++contadorId;
    }

    @Override
    public Emprestimo salvar(Emprestimo emprestimo) {
        if (emprestimo == null) {
            throw new IllegalArgumentException("Emprestimo nao pode ser nulo");
        }
        if (emprestimo.getId() == null) {
            emprestimo.setId(proximoId());
        }
        emprestimos.put(emprestimo.getId(), emprestimo);
        return emprestimo;
    }

    @Override
    public Optional<Emprestimo> buscarPorId(Long id) {
        return Optional.ofNullable(emprestimos.get(id));
    }

    @Override
    public List<Emprestimo> listarAtivosDoSocio(Long socioId) {
        List<Emprestimo> ativos = new ArrayList<>();
        for (Emprestimo emprestimo : emprestimos.values()) {
            if (!emprestimo.estaAtivo()) {
                continue;
            }
            if (emprestimo.getSocio() != null
                    && Objects.equals(emprestimo.getSocio().getId(), socioId)) {
                ativos.add(emprestimo);
            }
        }
        return ativos;
    }

    @Override
    public List<Emprestimo> listarTodos() {
        return new ArrayList<>(emprestimos.values());
    }
}
