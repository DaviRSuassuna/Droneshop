package br.com.droneshop.notificacao.dto;

import java.util.List;

import br.com.droneshop.notificacao.model.CategoriaNotificacao;
import br.com.droneshop.notificacao.model.TemplateNotificacao;

public record TemplateEmailResponseDTO(Long id, String codigo, String assunto, int versao, boolean ativo,
        CategoriaNotificacao categoria, String corpoTemplate, List<String> variaveis) {

    public static TemplateEmailResponseDTO from(TemplateNotificacao t) {
        return new TemplateEmailResponseDTO(t.getId(), t.getCodigo(), t.getAssunto(), t.getVersao(), t.isAtivo(),
                t.getCategoria(), t.getCorpoTemplate(), t.listaVariaveis());
    }
}
