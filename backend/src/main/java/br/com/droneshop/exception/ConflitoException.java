package br.com.droneshop.exception;

// Registro em uso por outro (ex.: marca com produtos). Vira 409.
// Regra do catalogo: o que esta em uso nao se exclui, se inativa (status INATIVA/INATIVO).
public class ConflitoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ConflitoException(String msg) {
        super(msg);
    }
}
