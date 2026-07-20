package br.ulbra.bibliotech.regras;

/**
 * Centraliza os parametros da politica de emprestimo da biblioteca.
 *
 * <p>Todas as regras de negocio numericas do sistema estao declaradas aqui como
 * constantes, para que exista uma unica fonte de verdade e para que uma eventual
 * mudanca de politica seja feita em um unico lugar.</p>
 *
 * <p>Classe utilitaria: nao deve ser instanciada.</p>
 */
public final class PoliticaEmprestimo {

    /**
     * REQ-001: quantidade maxima de emprestimos ativos que um socio pode manter
     * simultaneamente. Ao atingir esse numero, o socio nao pode retirar mais nenhum
     * exemplar ate devolver algum.
     */
    public static final int LIMITE_EMPRESTIMOS_ATIVOS = 3;

    /**
     * REQ-002: prazo de devolucao em dias corridos, contados a partir da data do
     * emprestimo. A data prevista de devolucao e a data do emprestimo somada a
     * esta quantidade de dias.
     */
    public static final int PRAZO_DEVOLUCAO_DIAS = 14;

    /**
     * REQ-003: valor em reais cobrado por cada dia de atraso na devolucao.
     */
    public static final double MULTA_POR_DIA = 1.00;

    /**
     * REQ-003: periodo de carencia, em dias. Um atraso de ate esta quantidade de
     * dias nao gera multa. A partir do primeiro dia seguinte a carencia, a multa
     * passa a ser cobrada sobre todos os dias de atraso, inclusive os dias que
     * estavam dentro da carencia.
     */
    public static final int CARENCIA_DIAS = 2;

    /**
     * REQ-003: valor maximo em reais que uma unica multa pode alcancar. Por maior
     * que seja o atraso, a multa de um emprestimo nunca ultrapassa este valor.
     */
    public static final double TETO_MULTA = 50.00;

    /**
     * REQ-004: valor de multa acumulada a partir do qual o socio fica bloqueado.
     * O bloqueio ocorre quando a multa acumulada e estritamente maior que este
     * valor; um socio que deve exatamente este valor continua liberado.
     */
    public static final double LIMITE_MULTA_PARA_BLOQUEIO = 20.00;

    private PoliticaEmprestimo() {
        throw new UnsupportedOperationException("Classe utilitaria nao deve ser instanciada");
    }
}
