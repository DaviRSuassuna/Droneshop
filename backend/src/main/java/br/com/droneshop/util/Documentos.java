package br.com.droneshop.util;

// CPF, CNPJ, CEP e telefone ficam no banco so com digitos; a mascara e so do front.
public final class Documentos {

    private Documentos() {
    }

    public static String somenteDigitos(String valor) {
        return (valor == null) ? null : valor.replaceAll("\\D", "");
    }

    // devolve null quando nao sobra nenhum digito (campo opcional vazio)
    public static String somenteDigitosOuNulo(String valor) {
        String digitos = somenteDigitos(valor);
        return (digitos == null || digitos.isEmpty()) ? null : digitos;
    }

}
