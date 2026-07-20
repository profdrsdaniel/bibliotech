package br.ulbra.bibliotech.configuracao;

import br.ulbra.bibliotech.dominio.Livro;
import br.ulbra.bibliotech.dominio.Socio;
import br.ulbra.bibliotech.repositorio.LivroRepository;
import br.ulbra.bibliotech.repositorio.SocioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Popula a base em memoria com socios e livros assim que a aplicacao sobe.
 *
 * <p>Os identificadores sao fixos e redondos para facilitar o uso da API em sala:
 * socios de 1 a 5 e livros de 10 a 17.</p>
 */
@Component
public class DadosIniciais implements CommandLineRunner {

    private final SocioRepository socioRepository;
    private final LivroRepository livroRepository;

    public DadosIniciais(SocioRepository socioRepository, LivroRepository livroRepository) {
        this.socioRepository = socioRepository;
        this.livroRepository = livroRepository;
    }

    @Override
    public void run(String... args) {
        popularSocios();
        popularLivros();
    }

    private void popularSocios() {
        socioRepository.salvar(new Socio(1L, "Ana Prado", "ana.prado@ulbra.br"));
        socioRepository.salvar(new Socio(2L, "Bruno Lima", "bruno.lima@ulbra.br", 35.00, 0));
        socioRepository.salvar(new Socio(3L, "Carla Nunes", "carla.nunes@ulbra.br", 0.00, 3));
        socioRepository.salvar(new Socio(4L, "Diego Alves", "diego.alves@ulbra.br", 5.00, 1));
        socioRepository.salvar(new Socio(5L, "Elisa Rocha", "elisa.rocha@ulbra.br"));
    }

    private void popularLivros() {
        livroRepository.salvar(new Livro(10L, "Dom Casmurro", "Machado de Assis", "978-85-359-0277-5"));
        livroRepository.salvar(new Livro(11L, "Grande Sertao: Veredas", "Guimaraes Rosa", "978-85-01-05555-4"));
        livroRepository.salvar(new Livro(12L, "Vidas Secas", "Graciliano Ramos", "978-85-03-01204-8"));

        Livro reservado = new Livro(13L, "Macunaima", "Mario de Andrade", "978-85-260-1234-7");
        reservado.reservarPara(4L);
        livroRepository.salvar(reservado);

        Livro indisponivel = new Livro(14L, "A Hora da Estrela", "Clarice Lispector", "978-85-209-1234-0");
        indisponivel.marcarComoEmprestado();
        livroRepository.salvar(indisponivel);

        livroRepository.salvar(new Livro(15L, "O Cortico", "Aluisio Azevedo", "978-85-08-11111-2"));
        livroRepository.salvar(new Livro(16L, "Iracema", "Jose de Alencar", "978-85-260-5678-1"));
        livroRepository.salvar(new Livro(17L, "Memorias Postumas de Bras Cubas", "Machado de Assis", "978-85-359-0155-6"));
    }
}
