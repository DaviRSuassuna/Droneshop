package br.com.droneshop.exception;

// Recurso procurado nao existe (ex.: CEP que o ViaCEP nao conhece). Vira 404.
public class ResourceNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String msg) {
        super(msg);
    }
}
