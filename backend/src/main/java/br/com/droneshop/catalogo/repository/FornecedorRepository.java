package br.com.droneshop.catalogo.repository;

import java.util.Locale;
import java.util.HashMap;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.catalogo.model.StatusFornecedor;
import br.com.droneshop.catalogo.model.Fornecedor;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FornecedorRepository implements PanacheRepository<Fornecedor> {
    public PanacheQuery<Fornecedor> findComFiltro(String busca, Long idStatus, String uf) {
        var params = new HashMap<String, Object>();
        StringBuilder hql = new StringBuilder("1 = 1");
        if (busca != null && !busca.isBlank()) {
            params.put("busca", "%" + busca.trim().toLowerCase(Locale.ROOT) + "%");
            String digitos = busca.replaceAll("\\D", "");
            hql.append(" AND (lower(razaoSocial) LIKE :busca OR lower(emailContato) LIKE :busca");
            if (!digitos.isEmpty()) { hql.append(" OR cnpj LIKE :cnpj"); params.put("cnpj", "%" + digitos + "%"); }
            hql.append(")");
        }
        if (idStatus != null) {
            var status = StatusFornecedor.valueOf(idStatus);
            if (status == null) throw ValidationException.of("idStatus", "Status inválido (back).");
            hql.append(" AND status = :status"); params.put("status", status);
        }
        if (uf != null && !uf.isBlank()) {
            hql.append(" AND endereco.municipio.estado.sigla = :extra"); params.put("extra", uf.trim().toUpperCase(Locale.ROOT));
        }
        return find(hql + " ORDER BY razaoSocial, id", params);
    }


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
        return find("SELECT f FROM Fornecedor f WHERE LOWER(f.razaoSocial) LIKE ?1 ORDER BY f.razaoSocial ", "%" + nome.toLowerCase() + "%");
    }

    @Override
    public PanacheQuery<Fornecedor> findAll() {
        return find("SELECT f FROM Fornecedor f ORDER BY f.razaoSocial");
    }

}
