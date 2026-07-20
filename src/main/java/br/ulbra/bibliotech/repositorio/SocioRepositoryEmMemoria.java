package br.ulbra.bibliotech.repositorio;

import br.ulbra.bibliotech.dominio.Socio;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementacao de {@link SocioRepository} que guarda os socios em memoria.
 *
 * <p>Usa um {@link LinkedHashMap} para preservar a ordem de insercao, deixando as
 * listagens sempre na mesma sequencia. Os dados existem apenas enquanto a
 * aplicacao estiver no ar.</p>
 */
public class SocioRepositoryEmMemoria implements SocioRepository {

    private final Map<Long, Socio> socios = new LinkedHashMap<>();

    @Override
    public Socio salvar(Socio socio) {
        if (socio == null) {
            throw new IllegalArgumentException("Socio nao pode ser nulo");
        }
        socios.put(socio.getId(), socio);
        return socio;
    }

    @Override
    public Optional<Socio> buscarPorId(Long id) {
        return Optional.ofNullable(socios.get(id));
    }

    @Override
    public List<Socio> listarTodos() {
        return new ArrayList<>(socios.values());
    }
}
