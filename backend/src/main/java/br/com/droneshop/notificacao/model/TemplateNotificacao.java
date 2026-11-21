package br.com.droneshop.notificacao.model;

import java.util.Arrays;
import java.util.List;

import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** Template versionado (RF39): editar cria nova versão e desativa a anterior. */
@Entity
@Table(name = "template_notificacao")
public class TemplateNotificacao extends DefaultEntity {

    @Column(nullable = false, length = 60)
    private String codigo;

    @Column(nullable = false, length = 200)
    private String assunto;

    @Column(nullable = false, columnDefinition = "text")
    private String corpoTemplate;

    @Column(nullable = false)
    private int versao;

    @Column(nullable = false)
    private boolean ativo;

    @Column(nullable = false)
    private CategoriaNotificacao categoria;

    /** Variáveis que o template oferece, separadas por vírgula. */
    @Column(length = 400)
    private String variaveis;

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getAssunto() { return assunto; }
    public void setAssunto(String assunto) { this.assunto = assunto; }
    public String getCorpoTemplate() { return corpoTemplate; }
    public void setCorpoTemplate(String corpoTemplate) { this.corpoTemplate = corpoTemplate; }
    public int getVersao() { return versao; }
    public void setVersao(int versao) { this.versao = versao; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public CategoriaNotificacao getCategoria() { return categoria; }
    public void setCategoria(CategoriaNotificacao categoria) { this.categoria = categoria; }
    public String getVariaveis() { return variaveis; }
    public void setVariaveis(String variaveis) { this.variaveis = variaveis; }

    public List<String> listaVariaveis() {
        if (variaveis == null || variaveis.isBlank()) return List.of();
        return Arrays.stream(variaveis.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }
}
