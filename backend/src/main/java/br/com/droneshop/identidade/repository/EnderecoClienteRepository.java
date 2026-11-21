package br.com.droneshop.identidade.repository;

import java.util.List;

import br.com.droneshop.identidade.model.EnderecoCliente;
import br.com.droneshop.identidade.model.PerfilCliente;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EnderecoClienteRepository implements PanacheRepository<EnderecoCliente> {

    // principal primeiro, depois do mais antigo para o mais novo
    public List<EnderecoCliente> doPerfil(PerfilCliente perfil) {
        return list("perfilCliente = ?1 order by principal desc, id", perfil);
    }

    public EnderecoCliente doPerfil(Long id, PerfilCliente perfil) {
        return find("id = ?1 and perfilCliente = ?2", id, perfil).firstResult();
    }

    public long contarDoPerfil(PerfilCliente perfil) {
        return count("perfilCliente", perfil);
    }

    public void desmarcarPrincipal(PerfilCliente perfil) {
        update("principal = false where perfilCliente = ?1 and principal = true", perfil);
    }

    public EnderecoCliente maisAntigo(PerfilCliente perfil) {
        return find("perfilCliente = ?1 order by id", perfil).firstResult();
    }
}
