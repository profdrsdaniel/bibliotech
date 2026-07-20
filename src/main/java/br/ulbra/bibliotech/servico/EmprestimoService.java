package br.ulbra.bibliotech.servico;

import br.ulbra.bibliotech.dominio.Emprestimo;
import br.ulbra.bibliotech.dominio.Livro;
import br.ulbra.bibliotech.dominio.Socio;
import br.ulbra.bibliotech.regras.CalculadoraMulta;
import br.ulbra.bibliotech.regras.PoliticaEmprestimo;
import br.ulbra.bibliotech.repositorio.EmprestimoRepository;

import java.time.LocalDate;

/**
 * Orquestra as operacoes de emprestimo e devolucao de exemplares.
 *
 * <p>Concentra as verificacoes de impedimento (REQ-001, REQ-004, REQ-005 e
 * REQ-006) e delega o calculo da multa para a {@link CalculadoraMulta}.</p>
 */
public class EmprestimoService {

    private final EmprestimoRepository repositorio;
    private final CalculadoraMulta calculadoraMulta;

    /**
     * Cria o servico com suas dependencias.
     *
     * @param repositorio      persistencia de emprestimos
     * @param calculadoraMulta componente responsavel pelo calculo de multas
     */
    public EmprestimoService(EmprestimoRepository repositorio, CalculadoraMulta calculadoraMulta) {
        this.repositorio = repositorio;
        this.calculadoraMulta = calculadoraMulta;
    }

    /**
     * Informa se o socio ja alcancou o numero maximo de emprestimos simultaneos.
     *
     * <p>Implementa a REQ-001: o socio pode manter no maximo
     * {@link PoliticaEmprestimo#LIMITE_EMPRESTIMOS_ATIVOS} emprestimos ativos. Ao
     * alcancar esse numero ele nao pode retirar mais nenhum exemplar.</p>
     *
     * @param socio socio a ser avaliado
     * @return {@code true} se o socio nao puder retirar mais exemplares
     */
    public boolean atingiuLimiteDeEmprestimos(Socio socio) {
        return socio.getEmprestimosAtivos() > PoliticaEmprestimo.LIMITE_EMPRESTIMOS_ATIVOS;
    }

    /**
     * Informa se o socio esta bloqueado por causa das multas em aberto.
     *
     * <p>Implementa a REQ-004: fica bloqueado o socio cuja multa acumulada for
     * maior que {@link PoliticaEmprestimo#LIMITE_MULTA_PARA_BLOQUEIO}. Um socio
     * que deve exatamente esse valor continua liberado.</p>
     *
     * @param socio socio a ser avaliado
     * @return {@code true} se o socio estiver bloqueado
     */
    public boolean estaBloqueadoPorMulta(Socio socio) {
        return socio.getMultaAcumulada() >= PoliticaEmprestimo.LIMITE_MULTA_PARA_BLOQUEIO;
    }

    /**
     * Verifica se existe algum impedimento para o socio retirar o exemplar.
     *
     * <p>As verificacoes acontecem nesta ordem: exemplar indisponivel, exemplar
     * reservado para outro socio, socio bloqueado por multa e socio no limite de
     * emprestimos.</p>
     *
     * @param socio socio que deseja retirar o exemplar
     * @param livro exemplar pretendido
     * @return {@code null} se a retirada pode acontecer, ou a mensagem que
     *         descreve o motivo do impedimento
     */
    public String verificarImpedimento(Socio socio, Livro livro) {
        if (!livro.isDisponivel()) {
            return "Exemplar indisponivel no momento";
        }
        if (livro.estaReservadoParaOutro(socio.getId())) {
            return "Exemplar reservado para outro socio";
        }
        if (estaBloqueadoPorMulta(socio)) {
            return "Socio bloqueado por multa acumulada de R$ "
                    + String.format("%.2f", socio.getMultaAcumulada());
        }
        if (atingiuLimiteDeEmprestimos(socio)) {
            return "Socio atingiu o limite de "
                    + PoliticaEmprestimo.LIMITE_EMPRESTIMOS_ATIVOS + " emprestimos ativos";
        }
        return null;
    }

    /**
     * Registra a retirada de um exemplar por um socio.
     *
     * @param socio          socio que esta retirando o exemplar
     * @param livro          exemplar retirado
     * @param dataEmprestimo data da retirada
     * @return o emprestimo criado e persistido
     * @throws IllegalStateException se houver qualquer impedimento para a retirada
     */
    public Emprestimo emprestar(Socio socio, Livro livro, LocalDate dataEmprestimo) {
        String impedimento = verificarImpedimento(socio, livro);
        if (impedimento != null) {
            throw new IllegalStateException(impedimento);
        }

        Emprestimo emprestimo = new Emprestimo(repositorio.proximoId(), livro, socio, dataEmprestimo);

        livro.marcarComoEmprestado();
        livro.cancelarReserva();
        socio.registrarNovoEmprestimo();

        return repositorio.salvar(emprestimo);
    }

    /**
     * Registra a devolucao de um exemplar, apurando a multa devida.
     *
     * <p>A multa apurada e somada ao total acumulado do socio, o exemplar volta a
     * ficar disponivel e o contador de emprestimos ativos do socio e reduzido.</p>
     *
     * @param emprestimo    emprestimo sendo encerrado
     * @param dataDevolucao data em que o exemplar foi devolvido
     * @return o valor da multa apurada, em reais
     */
    public double devolver(Emprestimo emprestimo, LocalDate dataDevolucao) {
        double multa = calculadoraMulta.calcular(emprestimo.getDataPrevistaDevolucao(), dataDevolucao);

        emprestimo.registrarDevolucao(dataDevolucao, multa);
        emprestimo.getLivro().marcarComoDisponivel();
        emprestimo.getSocio().registrarDevolucao();
        emprestimo.getSocio().adicionarMulta(multa);

        repositorio.salvar(emprestimo);

        return multa;
    }
}
