package br.com.droneshop.compra.exception;

/** Cupom que não se aplica (RN03): 400 com errors[].motivo (#25). */
public class CupomRecusadoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final String motivo;

    public CupomRecusadoException(String motivo, String mensagem) {
        super(mensagem);
        this.motivo = motivo;
    }

    public String getMotivo() {
        return motivo;
    }
}
