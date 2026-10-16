package br.com.droneshop.administracao.repository;

import br.com.droneshop.administracao.model.ConfiguracaoLoja;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ConfiguracaoLojaRepository implements PanacheRepository<ConfiguracaoLoja> {

    public ConfiguracaoLoja buscarUnica() {
        return findById(1L);
    }
}
