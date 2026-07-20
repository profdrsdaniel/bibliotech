package br.ulbra.bibliotech.api;

/**
 * Sinaliza que um recurso solicitado nao existe.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
