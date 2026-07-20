package br.ulbra.bibliotech;

import br.ulbra.bibliotech.dominio.Emprestimo;
import br.ulbra.bibliotech.dominio.Livro;
import br.ulbra.bibliotech.dominio.Socio;
import br.ulbra.bibliotech.regras.CalculadoraMulta;
import br.ulbra.bibliotech.repositorio.EmprestimoRepository;
import br.ulbra.bibliotech.repositorio.EmprestimoRepositoryEmMemoria;
import br.ulbra.bibliotech.servico.EmprestimoService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Demonstracao executavel do BiblioTech.
 *
 * <p>Percorre alguns cenarios de uso do sistema e imprime no console o que cada
 * operacao produz. As datas sao fixas para que a saida seja sempre a mesma,
 * independentemente de quando o programa rodar.</p>
 */
public class Main {

    private static final Locale BR = Locale.of("pt", "BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final LocalDate DATA_BASE = LocalDate.of(2026, 3, 1);

    public static void main(String[] args) {
        EmprestimoRepository repositorio = new EmprestimoRepositoryEmMemoria();
        CalculadoraMulta calculadora = new CalculadoraMulta();
        EmprestimoService servico = new EmprestimoService(repositorio, calculadora);

        cabecalho();

        cenario1(servico);
        cenario2(servico);
        cenario3(servico);
        cenario4(servico);
        cenario5(servico);

        rodape();
    }

    private static void cenario1(EmprestimoService servico) {
        titulo(1, "Emprestimo simples");

        Socio socio = new Socio(1L, "Ana Prado", "ana.prado@ulbra.br");
        Livro livro = new Livro(1L, "Dom Casmurro", "Machado de Assis", "978-85-359-0277-5");

        Emprestimo emprestimo = servico.emprestar(socio, livro, DATA_BASE);

        System.out.println("  Socio.................: " + socio.getNome());
        System.out.println("  Livro.................: " + livro.getTitulo() + " (" + livro.getAutor() + ")");
        System.out.println("  Data do emprestimo....: " + emprestimo.getDataEmprestimo().format(DATA));
        System.out.println("  Data prevista devolucao: " + emprestimo.getDataPrevistaDevolucao().format(DATA));
    }

    private static void cenario2(EmprestimoService servico) {
        titulo(2, "Devolucao dentro do prazo");

        Socio socio = new Socio(2L, "Bruno Lima", "bruno.lima@ulbra.br");
        Livro livro = new Livro(2L, "Grande Sertao: Veredas", "Guimaraes Rosa", "978-85-01-05555-4");

        Emprestimo emprestimo = servico.emprestar(socio, livro, DATA_BASE);
        LocalDate devolucao = emprestimo.getDataPrevistaDevolucao().minusDays(1);
        double multa = servico.devolver(emprestimo, devolucao);

        System.out.println("  Data prevista.........: " + emprestimo.getDataPrevistaDevolucao().format(DATA));
        System.out.println("  Data da devolucao.....: " + devolucao.format(DATA));
        System.out.println("  Multa calculada.......: " + moeda(multa));
    }

    private static void cenario3(EmprestimoService servico) {
        titulo(3, "Devolucao com 10 dias de atraso");

        Socio socio = new Socio(3L, "Carla Nunes", "carla.nunes@ulbra.br");
        Livro livro = new Livro(3L, "Vidas Secas", "Graciliano Ramos", "978-85-03-01204-8");

        Emprestimo emprestimo = servico.emprestar(socio, livro, DATA_BASE);
        LocalDate devolucao = emprestimo.getDataPrevistaDevolucao().plusDays(10);
        double multa = servico.devolver(emprestimo, devolucao);

        System.out.println("  Data prevista.........: " + emprestimo.getDataPrevistaDevolucao().format(DATA));
        System.out.println("  Data da devolucao.....: " + devolucao.format(DATA));
        System.out.println("  Multa calculada.......: " + moeda(multa));
    }

    private static void cenario4(EmprestimoService servico) {
        titulo(4, "Socio com multa acumulada tentando novo emprestimo");

        Socio socio = new Socio(4L, "Diego Alves", "diego.alves@ulbra.br", 35.00, 0);
        Livro livro = new Livro(4L, "Macunaima", "Mario de Andrade", "978-85-260-1234-7");

        System.out.println("  Socio.................: " + socio.getNome());
        System.out.println("  Multa acumulada.......: " + moeda(socio.getMultaAcumulada()));

        String impedimento = servico.verificarImpedimento(socio, livro);
        if (impedimento == null) {
            servico.emprestar(socio, livro, DATA_BASE);
            System.out.println("  Resultado.............: emprestimo realizado");
        } else {
            System.out.println("  Resultado.............: emprestimo recusado");
            System.out.println("  Motivo................: " + impedimento);
        }
    }

    private static void cenario5(EmprestimoService servico) {
        titulo(5, "Tentativa de emprestar exemplar ja emprestado");

        Socio primeiro = new Socio(5L, "Elisa Rocha", "elisa.rocha@ulbra.br");
        Socio segundo = new Socio(6L, "Felipe Souza", "felipe.souza@ulbra.br");
        Livro livro = new Livro(5L, "A Hora da Estrela", "Clarice Lispector", "978-85-209-1234-0");

        servico.emprestar(primeiro, livro, DATA_BASE);
        System.out.println("  Exemplar..............: " + livro.getTitulo());
        System.out.println("  Primeiro emprestimo...: " + primeiro.getNome());

        String impedimento = servico.verificarImpedimento(segundo, livro);
        if (impedimento == null) {
            servico.emprestar(segundo, livro, DATA_BASE);
            System.out.println("  Segundo emprestimo....: realizado para " + segundo.getNome());
        } else {
            System.out.println("  Segundo emprestimo....: recusado para " + segundo.getNome());
            System.out.println("  Motivo................: " + impedimento);
        }
    }

    private static String moeda(double valor) {
        return String.format(BR, "R$ %.2f", valor);
    }

    private static void titulo(int numero, String descricao) {
        System.out.println();
        System.out.println("-".repeat(78));
        System.out.println("CENARIO " + numero + " - " + descricao);
        System.out.println("-".repeat(78));
    }

    private static void cabecalho() {
        System.out.println("=".repeat(78));
        System.out.println("BIBLIOTECH - Demonstracao do sistema");
        System.out.println("Controle de Qualidade e Teste de Software");
        System.out.println("=".repeat(78));
        System.out.println("Cada cenario abaixo executa uma operacao do sistema e mostra o que ela");
        System.out.println("produz no console.");
    }

    private static void rodape() {
        System.out.println();
        System.out.println("=".repeat(78));
        System.out.println("Fim da demonstracao.");
        System.out.println("=".repeat(78));
    }
}
