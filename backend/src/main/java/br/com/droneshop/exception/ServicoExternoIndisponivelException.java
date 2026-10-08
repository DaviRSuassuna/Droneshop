package br.com.droneshop.exception;

// Integracao externa fora do ar ou lenta demais (ex.: ViaCEP). Vira 503:
// o front entende que pode seguir com preenchimento manual.
public class ServicoExternoIndisponivelException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ServicoExternoIndisponivelException(String msg, Throwable causa) {
        super(msg, causa);
    }
}
