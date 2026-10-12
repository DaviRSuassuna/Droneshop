package br.com.droneshop.exception;

// Recurso procurado nao existe (ex.: CEP que o ViaCEP nao conhece). Vira 404.
// GET e PUT de id inexistente respondem 404. DELETE de id inexistente continua 204
// de proposito: excluir o que ja nao existe (clique duplo, duas abas) nao e erro.
public class ResourceNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String msg) {
        super(msg);
    }
}
