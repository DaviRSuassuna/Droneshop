package br.com.droneshop.endereco.repository;

import java.util.List;

import br.com.droneshop.endereco.model.Municipio;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MunicipioRepository implements PanacheRepository<Municipio> {

    public List<Municipio> findByEstado(Long idEstado) {
        return find("SELECT m FROM Municipio m WHERE m.estado.id = ?1 ORDER BY m.nome ", idEstado).list();
    }

    public Municipio findByCodigoIbge(Integer codigoIbge) {
        return find("SELECT m FROM Municipio m WHERE m.codigoIbge = ?1 ", codigoIbge).firstResult();
    }

    // plano B do CEP, quando o codigo IBGE nao vem ou nao bate: nome + sigla da UF
    public Municipio findByNomeAndSiglaEstado(String nome, String siglaEstado) {
        return find("SELECT m FROM Municipio m WHERE LOWER(m.nome) = ?1 AND m.estado.sigla = ?2 ",
                nome.trim().toLowerCase(), siglaEstado.trim().toUpperCase()).firstResult();
    }

}
