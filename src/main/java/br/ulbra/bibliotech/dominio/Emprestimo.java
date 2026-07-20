package br.ulbra.bibliotech.dominio;

import br.ulbra.bibliotech.regras.PoliticaEmprestimo;

import java.time.LocalDate;

/**
 * Representa a retirada de um exemplar por um socio.
 *
 * <p>A data prevista de devolucao e derivada da data do emprestimo conforme a
 * REQ-002. Enquanto a data de devolucao efetiva for nula, o emprestimo e
 * considerado ativo.</p>
 */
public class Emprestimo {

    private Long id;
    private Livro livro;
    private Socio socio;
    private LocalDate dataEmprestimo;
    private LocalDate dataPrevistaDevolucao;
    private LocalDate dataDevolucao;
    private double multaCobrada;

    /**
     * Cria um emprestimo ativo, calculando a data prevista de devolucao a partir
     * da data do emprestimo somada ao prazo de
     * {@link PoliticaEmprestimo#PRAZO_DEVOLUCAO_DIAS} dias.
     *
     * @param id             identificador do emprestimo
     * @param livro          exemplar retirado
     * @param socio          socio que retirou o exemplar
     * @param dataEmprestimo data em que a retirada aconteceu
     */
    public Emprestimo(Long id, Livro livro, Socio socio, LocalDate dataEmprestimo) {
        this.id = id;
        this.livro = livro;
        this.socio = socio;
        this.dataEmprestimo = dataEmprestimo;
        this.dataPrevistaDevolucao = dataEmprestimo.plusDays(PoliticaEmprestimo.PRAZO_DEVOLUCAO_DIAS);
        this.dataDevolucao = null;
        this.multaCobrada = 0.0;
    }

    /**
     * Informa se o emprestimo ainda nao foi devolvido.
     *
     * @return {@code true} enquanto nao houver data de devolucao registrada
     */
    public boolean estaAtivo() {
        return this.dataDevolucao == null;
    }

    /**
     * Registra a devolucao do exemplar e a multa apurada.
     *
     * @param dataDevolucao data em que o exemplar foi devolvido
     * @param multaCobrada  valor da multa apurada para este emprestimo
     */
    public void registrarDevolucao(LocalDate dataDevolucao, double multaCobrada) {
        this.dataDevolucao = dataDevolucao;
        this.multaCobrada = multaCobrada;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Livro getLivro() {
        return livro;
    }

    public void setLivro(Livro livro) {
        this.livro = livro;
    }

    public Socio getSocio() {
        return socio;
    }

    public void setSocio(Socio socio) {
        this.socio = socio;
    }

    public LocalDate getDataEmprestimo() {
        return dataEmprestimo;
    }

    public void setDataEmprestimo(LocalDate dataEmprestimo) {
        this.dataEmprestimo = dataEmprestimo;
    }

    public LocalDate getDataPrevistaDevolucao() {
        return dataPrevistaDevolucao;
    }

    public void setDataPrevistaDevolucao(LocalDate dataPrevistaDevolucao) {
        this.dataPrevistaDevolucao = dataPrevistaDevolucao;
    }

    public LocalDate getDataDevolucao() {
        return dataDevolucao;
    }

    public void setDataDevolucao(LocalDate dataDevolucao) {
        this.dataDevolucao = dataDevolucao;
    }

    public double getMultaCobrada() {
        return multaCobrada;
    }

    public void setMultaCobrada(double multaCobrada) {
        this.multaCobrada = multaCobrada;
    }

    @Override
    public String toString() {
        return "Emprestimo{id=" + id
                + ", livro=" + (livro != null ? livro.getTitulo() : null)
                + ", socio=" + (socio != null ? socio.getNome() : null)
                + ", dataEmprestimo=" + dataEmprestimo
                + ", dataPrevistaDevolucao=" + dataPrevistaDevolucao
                + ", dataDevolucao=" + dataDevolucao
                + ", multaCobrada=" + multaCobrada
                + '}';
    }
}
