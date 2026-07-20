package br.ulbra.bibliotech.configuracao;

import br.ulbra.bibliotech.regras.CalculadoraMulta;
import br.ulbra.bibliotech.repositorio.EmprestimoRepository;
import br.ulbra.bibliotech.repositorio.EmprestimoRepositoryEmMemoria;
import br.ulbra.bibliotech.repositorio.LivroRepository;
import br.ulbra.bibliotech.repositorio.LivroRepositoryEmMemoria;
import br.ulbra.bibliotech.repositorio.SocioRepository;
import br.ulbra.bibliotech.repositorio.SocioRepositoryEmMemoria;
import br.ulbra.bibliotech.servico.EmprestimoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Fornece ao Spring as instancias das camadas de dominio, regras, servico e
 * repositorio.
 *
 * <p>Essas camadas sao Java puro e nao possuem anotacoes de framework. A ligacao
 * com o Spring e feita aqui, por fora: cada classe e criada com {@code new} e
 * exposta como bean, de modo que continua podendo ser instanciada normalmente
 * fora do contexto do Spring.</p>
 */
@Configuration
public class BeansConfiguracao {

    @Bean
    public SocioRepository socioRepository() {
        return new SocioRepositoryEmMemoria();
    }

    @Bean
    public LivroRepository livroRepository() {
        return new LivroRepositoryEmMemoria();
    }

    @Bean
    public EmprestimoRepository emprestimoRepository() {
        return new EmprestimoRepositoryEmMemoria();
    }

    @Bean
    public CalculadoraMulta calculadoraMulta() {
        return new CalculadoraMulta();
    }

    @Bean
    public EmprestimoService emprestimoService(EmprestimoRepository emprestimoRepository,
                                               CalculadoraMulta calculadoraMulta) {
        return new EmprestimoService(emprestimoRepository, calculadoraMulta);
    }
}
