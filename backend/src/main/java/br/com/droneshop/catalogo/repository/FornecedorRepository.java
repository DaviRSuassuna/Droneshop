package br.com.droneshop.catalogo.repository;

import br.com.droneshop.catalogo.model.Fornecedor;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FornecedorRepository implements PanacheRepository<Fornecedor> {

    public Fornecedor findByCnpj(String cnpj) {
        return find("SELECT f FROM Fornecedor f WHERE f.cnpj = ?1 ", cnpj).firstResult();
    }

    public Fornecedor findByCnpjExceptId(String cnpj, Long id) {
        if (id == null)
            return findByCnpj(cnpj);
        return find("SELECT f FROM Fornecedor f WHERE f.cnpj = ?1 AND f.id <> ?2 ", cnpj, id).firstResult();
    }

    // "nome" aqui e a razao social, para manter a mesma assinatura de Marca/Categoria
    public PanacheQuery<Fornecedor> findByNome(String nome) {
        return find("SELECT f FROM Fornecedor f WHERE f.razaoSocial LIKE ?1 ORDER BY f.razaoSocial ", "%" + nome + "%");
    }

    @Override
    public PanacheQuery<Fornecedor> findAll() {
        return find("SELECT f FROM Fornecedor f ORDER BY f.razaoSocial");
    }

}
