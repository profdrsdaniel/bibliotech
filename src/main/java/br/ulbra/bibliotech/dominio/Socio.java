package br.ulbra.bibliotech.dominio;

/**
 * Representa um socio da biblioteca.
 *
 * <p>Alem dos dados cadastrais, o socio guarda o total de multas que ainda deve
 * e a quantidade de emprestimos que mantem ativos no momento. Esses dois valores
 * sao a base das regras REQ-001 e REQ-004.</p>
 */
public class Socio {

    private Long id;
    private String nome;
    private String email;
    private double multaAcumulada;
    private int emprestimosAtivos;

    /**
     * Cria um socio recem cadastrado, sem multas e sem emprestimos ativos.
     *
     * @param id    identificador do socio
     * @param nome  nome completo do socio
     * @param email email de contato do socio
     */
    public Socio(Long id, String nome, String email) {
        this(id, nome, email, 0.0, 0);
    }

    /**
     * Cria um socio com estado ja definido. Util para reconstruir um socio a
     * partir de dados existentes.
     *
     * @param id                identificador do socio
     * @param nome              nome completo do socio
     * @param email             email de contato do socio
     * @param multaAcumulada    total de multas em aberto, em reais
     * @param emprestimosAtivos quantidade de emprestimos ainda nao devolvidos
     */
    public Socio(Long id, String nome, String email, double multaAcumulada, int emprestimosAtivos) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.multaAcumulada = multaAcumulada;
        this.emprestimosAtivos = emprestimosAtivos;
    }

    /**
     * Soma um valor a multa acumulada do socio.
     *
     * @param valor valor em reais a acrescentar
     */
    public void adicionarMulta(double valor) {
        this.multaAcumulada += valor;
    }

    /**
     * Quita todas as multas em aberto, zerando o valor acumulado.
     */
    public void quitarMultas() {
        this.multaAcumulada = 0.0;
    }

    /**
     * Registra que o socio retirou mais um exemplar, incrementando o contador de
     * emprestimos ativos.
     */
    public void registrarNovoEmprestimo() {
        this.emprestimosAtivos++;
    }

    /**
     * Registra que o socio devolveu um exemplar, decrementando o contador de
     * emprestimos ativos. O contador nunca fica negativo.
     */
    public void registrarDevolucao() {
        if (this.emprestimosAtivos > 0) {
            this.emprestimosAtivos--;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getMultaAcumulada() {
        return multaAcumulada;
    }

    public void setMultaAcumulada(double multaAcumulada) {
        this.multaAcumulada = multaAcumulada;
    }

    public int getEmprestimosAtivos() {
        return emprestimosAtivos;
    }

    public void setEmprestimosAtivos(int emprestimosAtivos) {
        this.emprestimosAtivos = emprestimosAtivos;
    }

    @Override
    public String toString() {
        return "Socio{id=" + id
                + ", nome='" + nome + '\''
                + ", multaAcumulada=" + multaAcumulada
                + ", emprestimosAtivos=" + emprestimosAtivos
                + '}';
    }
}
