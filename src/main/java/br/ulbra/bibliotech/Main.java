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
 * <p>Percorre seis cenarios de uso e imprime, lado a lado, o valor que o sistema
 * produziu e o valor que a especificacao determina. As datas sao fixas para que
 * a saida seja sempre a mesma, independentemente de quando o programa rodar.</p>
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
        cenario6(servico);

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
        comparar("Data prevista devolucao",
                emprestimo.getDataPrevistaDevolucao().format(DATA),
                DATA_BASE.plusDays(14).format(DATA));
        comparar("Emprestimos ativos",
                String.valueOf(socio.getEmprestimosAtivos()),
                "1");
    }

    private static void cenario2(EmprestimoService servico) {
        titulo(2, "Devolucao dentro do prazo");

        Socio socio = new Socio(2L, "Bruno Lima", "bruno.lima@ulbra.br");
        Livro livro = new Livro(2L, "Grande Sertao: Veredas", "Guimaraes Rosa", "978-85-01-05555-4");

        Emprestimo emprestimo = servico.emprestar(socio, livro, DATA_BASE);
        LocalDate devolucao = emprestimo.getDataPrevistaDevolucao().minusDays(2);
        double multa = servico.devolver(emprestimo, devolucao);

        System.out.println("  Data prevista.........: " + emprestimo.getDataPrevistaDevolucao().format(DATA));
        System.out.println("  Data da devolucao.....: " + devolucao.format(DATA) + "  (2 dias adiantado)");
        comparar("Multa", moeda(multa), moeda(0.00));
        comparar("Livro disponivel novamente", String.valueOf(livro.isDisponivel()), "true");
    }

    private static void cenario3(EmprestimoService servico) {
        titulo(3, "Devolucao com 3 dias de atraso");

        Socio socio = new Socio(3L, "Carla Nunes", "carla.nunes@ulbra.br");
        Livro livro = new Livro(3L, "Vidas Secas", "Graciliano Ramos", "978-85-03-01204-8");

        Emprestimo emprestimo = servico.emprestar(socio, livro, DATA_BASE);
        LocalDate devolucao = emprestimo.getDataPrevistaDevolucao().plusDays(3);
        double multa = servico.devolver(emprestimo, devolucao);

        System.out.println("  Data prevista.........: " + emprestimo.getDataPrevistaDevolucao().format(DATA));
        System.out.println("  Data da devolucao.....: " + devolucao.format(DATA) + "  (3 dias de atraso)");
        comparar("Multa", moeda(multa), moeda(3.00));
        comparar("Multa acumulada do socio", moeda(socio.getMultaAcumulada()), moeda(3.00));
    }

    private static void cenario4(EmprestimoService servico) {
        titulo(4, "Devolucao com 51 dias de atraso");

        Socio socio = new Socio(4L, "Diego Alves", "diego.alves@ulbra.br");
        Livro livro = new Livro(4L, "Macunaima", "Mario de Andrade", "978-85-260-1234-7");

        Emprestimo emprestimo = servico.emprestar(socio, livro, DATA_BASE);
        LocalDate devolucao = emprestimo.getDataPrevistaDevolucao().plusDays(51);
        double multa = servico.devolver(emprestimo, devolucao);

        System.out.println("  Data prevista.........: " + emprestimo.getDataPrevistaDevolucao().format(DATA));
        System.out.println("  Data da devolucao.....: " + devolucao.format(DATA) + "  (51 dias de atraso)");
        comparar("Multa", moeda(multa), moeda(50.00) + " (teto)");
    }

    private static void cenario5(EmprestimoService servico) {
        titulo(5, "Socio com multa de exatamente R$ 20,00");

        Socio socio = new Socio(5L, "Elisa Rocha", "elisa.rocha@ulbra.br", 20.00, 0);
        Livro livro = new Livro(5L, "A Hora da Estrela", "Clarice Lispector", "978-85-209-1234-0");

        System.out.println("  Multa acumulada.......: " + moeda(socio.getMultaAcumulada()));
        System.out.println("  Limite de bloqueio....: acima de " + moeda(20.00));

        String impedimento = servico.verificarImpedimento(socio, livro);
        String situacao = (impedimento == null) ? "LIBERADO" : "BLOQUEADO";

        comparar("Situacao", situacao, "LIBERADO");
        if (impedimento != null) {
            System.out.println("  Motivo informado......: " + impedimento);
        }
    }

    private static void cenario6(EmprestimoService servico) {
        titulo(6, "Socio com 3 emprestimos ativos tentando o quarto");

        Socio socio = new Socio(6L, "Felipe Souza", "felipe.souza@ulbra.br", 0.00, 3);
        Livro livro = new Livro(6L, "O Cortico", "Aluisio Azevedo", "978-85-08-11111-2");

        System.out.println("  Emprestimos ativos....: " + socio.getEmprestimosAtivos());
        System.out.println("  Limite da politica....: 3");

        String impedimento = servico.verificarImpedimento(socio, livro);
        String situacao = (impedimento == null) ? "PERMITIDO" : "RECUSADO";

        comparar("Quarto emprestimo", situacao, "RECUSADO");
        if (impedimento != null) {
            System.out.println("  Motivo informado......: " + impedimento);
        }
    }

    private static void comparar(String rotulo, String produzido, String esperado) {
        String base = "  " + rotulo;
        while (base.length() < 26) {
            base += ".";
        }
        String marca = produzido.equals(esperado) ? " " : "!";
        System.out.println(base + ": " + pad(produzido, 14)
                + "| regra manda: " + esperado + "  " + marca);
    }

    private static String pad(String valor, int largura) {
        StringBuilder sb = new StringBuilder(valor);
        while (sb.length() < largura) {
            sb.append(' ');
        }
        return sb.toString();
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
        System.out.println("Cada linha mostra o valor produzido pelo sistema e, ao lado, o valor");
        System.out.println("que a especificacao determina. Compare os dois com atencao.");
    }

    private static void rodape() {
        System.out.println();
        System.out.println("=".repeat(78));
        System.out.println("Fim da demonstracao.");
        System.out.println("=".repeat(78));
    }
}
