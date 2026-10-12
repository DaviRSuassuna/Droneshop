package br.com.droneshop.util;

public final class Textos {

    private Textos() {
    }

    // A tela envia "" para campo nao preenchido. Para o Bean Validation, "" falha no
    // @Length(min = 2), mas null passa; por isso os DTOs que aceitam rascunho trocam
    // texto em branco por null antes de validar.
    public static String nuloSeVazio(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor;
    }

}
