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

    // CPF com 11 dígitos e os dois verificadores corretos (aceita com máscara)
    public static boolean cpfValido(String valor) {
        String d = somenteDigitos(valor);
        if (d == null || d.length() != 11 || d.chars().distinct().count() == 1)
            return false;
        return digitoVerificador(d, 9, 10) == d.charAt(9) - '0'
                && digitoVerificador(d, 10, 11) == d.charAt(10) - '0';
    }

    // CNPJ com 14 dígitos e os dois verificadores corretos (aceita com máscara)
    public static boolean cnpjValido(String valor) {
        String d = somenteDigitos(valor);
        if (d == null || d.length() != 14 || d.chars().distinct().count() == 1)
            return false;
        return digitoCnpj(d, 12) == d.charAt(12) - '0' && digitoCnpj(d, 13) == d.charAt(13) - '0';
    }

    private static int digitoVerificador(String d, int tamanho, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < tamanho; i++)
            soma += (d.charAt(i) - '0') * (pesoInicial - i);
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }

    private static int digitoCnpj(String d, int tamanho) {
        int soma = 0;
        int peso = tamanho - 7;
        for (int i = 0; i < tamanho; i++) {
            soma += (d.charAt(i) - '0') * peso;
            peso = (peso == 2) ? 9 : peso - 1;
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

}
