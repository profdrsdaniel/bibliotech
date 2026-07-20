package br.ulbra.bibliotech.dominio;

import java.util.Objects;

/**
 * Representa um exemplar do acervo da biblioteca.
 *
 * <p>Um exemplar controla dois estados independentes: se esta disponivel para
 * retirada (REQ-006) e se esta reservado para algum socio (REQ-005).</p>
 */
public class Livro {

    private Long id;
    private String titulo;
    private String autor;
    private String isbn;
    private boolean disponivel;
    private Long reservadoPorSocioId;

    /**
     * Cria um exemplar disponivel e sem reserva.
     *
     * @param id     identificador do exemplar
     * @param titulo titulo da obra
     * @param autor  autor da obra
     * @param isbn   codigo ISBN da obra
     */
    public Livro(Long id, String titulo, String autor, String isbn) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.disponivel = true;
        this.reservadoPorSocioId = null;
    }

    /**
     * Marca o exemplar como emprestado, tornando-o indisponivel para novas retiradas.
     */
    public void marcarComoEmprestado() {
        this.disponivel = false;
    }

    /**
     * Marca o exemplar como disponivel, liberando-o para novas retiradas.
     */
    public void marcarComoDisponivel() {
        this.disponivel = true;
    }

    /**
     * Reserva o exemplar para um socio especifico.
     *
     * @param socioId identificador do socio que reservou o exemplar
     */
    public void reservarPara(Long socioId) {
        this.reservadoPorSocioId = socioId;
    }

    /**
     * Cancela a reserva existente, deixando o exemplar livre para qualquer socio.
     */
    public void cancelarReserva() {
        this.reservadoPorSocioId = null;
    }

    /**
     * Informa se o exemplar esta reservado para um socio diferente do informado.
     *
     * <p>Implementa a REQ-005. Quando nao existe reserva alguma, o exemplar nao
     * esta reservado para ninguem e o metodo retorna {@code false}.</p>
     *
     * @param socioId identificador do socio que deseja retirar o exemplar
     * @return {@code true} se houver reserva em nome de outro socio
     */
    public boolean estaReservadoParaOutro(Long socioId) {
        if (this.reservadoPorSocioId == null) {
            return false;
        }
        return !Objects.equals(this.reservadoPorSocioId, socioId);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    public void setDisponivel(boolean disponivel) {
        this.disponivel = disponivel;
    }

    public Long getReservadoPorSocioId() {
        return reservadoPorSocioId;
    }

    public void setReservadoPorSocioId(Long reservadoPorSocioId) {
        this.reservadoPorSocioId = reservadoPorSocioId;
    }

    @Override
    public String toString() {
        return "Livro{id=" + id
                + ", titulo='" + titulo + '\''
                + ", autor='" + autor + '\''
                + ", disponivel=" + disponivel
                + '}';
    }
}
