# Padroes de Persistencia PostgreSQL, JPA, Panache e Flyway

Referencia complementar da skill `persistencia-postgresql` com exemplos praticos e checklist de revisao para entidades JPA, repositories Panache, migrations SQL, isolamento de tenants e controle de concorrencia.

## 1. Padrao de Entidade JPA (`model/`)

Toda entidade herda de `DefaultEntity`, mantem todos os atributos `private` e declara acessores explicitos (`get`/`set`/`is`):

```java
package br.com.droneshop.catalogo.model;

import java.math.BigDecimal;
import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "exemplo_item",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_exemplo_item_tenant_slug", columnNames = { "id_usuario", "slug" })
    },
    indexes = {
        @Index(name = "idx_exemplo_item_usuario", columnList = "id_usuario"),
        @Index(name = "idx_exemplo_item_marca", columnList = "id_marca")
    }
)
public class ExemploItem extends DefaultEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_marca")
    private Marca marca;

    @Column(length = 120, nullable = false)
    private String nome;

    @Column(length = 140, nullable = false)
    private String slug;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal valor;

    @Column(nullable = false)
    private StatusExemplo status;

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Marca getMarca() {
        return marca;
    }

    public void setMarca(Marca marca) {
        this.marca = marca;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public StatusExemplo getStatus() {
        return status;
    }

    public void setStatus(StatusExemplo status) {
        this.status = status;
    }
}
```

## 2. Conversor JPA para Enums de Dominio (D31)

```java
package br.com.droneshop.catalogo.model.converterjpa;

import br.com.droneshop.catalogo.model.StatusExemplo;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusExemploConverter implements AttributeConverter<StatusExemplo, Long> {

    @Override
    public Long convertToDatabaseColumn(StatusExemplo status) {
        return (status == null) ? null : status.getId();
    }

    @Override
    public StatusExemplo convertToEntityAttribute(Long id) {
        return StatusExemplo.valueOf(id);
    }
}
```

## 3. Repository com Isolamento de Tenant/Titular e Controle de Concorrencia

```java
package br.com.droneshop.estoque.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import br.com.droneshop.catalogo.model.Produto;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;

@ApplicationScoped
public class ExemploProdutoRepository implements PanacheRepository<Produto> {

    // Bloqueio pessimista para atualizacao concorrente de saldo de estoque
    public Produto findByIdForUpdate(Long idProduto) {
        return find("id = ?1", idProduto)
                .withLock(LockModeType.PESSIMISTIC_WRITE)
                .firstResult();
    }

    // Bloqueio ordenado de multiplos produtos (ex.: componentes de Kit ou itens de Pedido)
    // para evitar deadlock entre transacoes concorrentes
    public List<Produto> findByIdsForUpdateOrdenado(List<Long> ids) {
        return find("id IN ?1 ORDER BY id ASC", ids)
                .withLock(LockModeType.PESSIMISTIC_WRITE)
                .list();
    }

    // Consulta escopada pelo titular/tenant autenticado (previne IDOR e relacao cruzada)
    public Optional<Produto> findByIdAndFornecedorId(Long idProduto, Long idFornecedor) {
        return find("id = ?1 AND fornecedor.id = ?2", idProduto, idFornecedor)
                .firstResultOptional();
    }

    // Filtro dinamico seguro com parametros nomeados e ordenacao deterministica
    public PanacheQuery<Produto> findComFiltro(String busca, Long idMarca) {
        var params = new HashMap<String, Object>();
        var hql = new StringBuilder("1 = 1");

        if (busca != null && !busca.isBlank()) {
            params.put("busca", "%" + busca.trim().toLowerCase(Locale.ROOT) + "%");
            hql.append(" AND (LOWER(nome) LIKE :busca OR LOWER(sku) LIKE :busca)");
        }
        if (idMarca != null) {
            params.put("idMarca", idMarca);
            hql.append(" AND marca.id = :idMarca");
        }
        return find(hql + " ORDER BY nome ASC, id ASC", params);
    }
}
```

## 4. Boas Praticas de Migrations SQL (Flyway) e Constraints PostgreSQL

Quando uma migration versionada (`V<N>__<descricao>.sql`) for aprovada:

- **Nomenclatura de constraints e indices**:
  - Primary Key: `pk_<tabela>`
  - Foreign Key: `fk_<tabela>_<coluna_ou_destino>`
  - Unique Constraint: `uk_<tabela>_<colunas>`
  - Check Constraint: `ck_<tabela>_<regra>`
  - Index: `idx_<tabela>_<colunas>`
- **Regras de DDL**:
  - Nunca use `ON DELETE CASCADE` em tabelas cujo historico ou vinculo de negocio exija bloqueio (`409 Conflict`) e inativacao (D24).
  - Sempre crie indice para colunas `FOREIGN KEY` no PostgreSQL (o PostgreSQL nao cria indice automatico no lado filho da FK).
  - Garanta `NOT NULL` em colunas de tenant/titular, timestamps de criacao e chaves obrigatorias.

Exemplo de migration SQL:

```sql
CREATE TABLE reserva_estoque (
    id BIGSERIAL NOT NULL,
    data_cadastro TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    data_alteracao TIMESTAMP WITHOUT TIME ZONE,
    id_pedido BIGINT NOT NULL,
    id_produto BIGINT NOT NULL,
    quantidade INTEGER NOT NULL,
    status BIGINT NOT NULL,
    CONSTRAINT pk_reserva_estoque PRIMARY KEY (id),
    CONSTRAINT fk_reserva_estoque_pedido FOREIGN KEY (id_pedido) REFERENCES pedido (id),
    CONSTRAINT fk_reserva_estoque_produto FOREIGN KEY (id_produto) REFERENCES produto (id),
    CONSTRAINT ck_reserva_estoque_quantidade_positiva CHECK (quantidade > 0)
);

CREATE INDEX idx_reserva_estoque_pedido ON reserva_estoque (id_pedido);
CREATE INDEX idx_reserva_estoque_produto ON reserva_estoque (id_produto);
CREATE INDEX idx_reserva_estoque_status ON reserva_estoque (status);
```

## 5. Checklist de Revisao de Persistencia

Antes de finalizar qualquer tarefa que envolva banco de dados, verifique todos os itens:

1. **Alinhamento de Dominio e Aprovacao**:
   - [ ] A mudanca de entidade, schema ou migration esta aprovada no contrato da issue (`docs/issues/NNN.md`), no PRD e no diagrama UML?
   - [ ] Nenhuma nova decisao de dominio foi introduzida no schema sem aprovacao previa?
   - [ ] `docs/modelo.plantuml`, `docs/diagramas/aeria-*.puml`, `docs/REGRAS_NEGOCIO.md` e `import.sql` foram atualizados se houve mudanca aprovada de modelo?
2. **Mapeamento JPA e Encapsulamento**:
   - [ ] A entidade herda de `DefaultEntity`?
   - [ ] Todos os atributos sao `private` e possuem getters/setters explicitos?
   - [ ] Valores monetarios usam `BigDecimal` com `precision = 12, scale = 2`?
   - [ ] Enums usam `AttributeConverter` persistindo o `id` (`Long`)?
   - [ ] Campos opcionais em `RASCUNHO` permitem `null` no banco e sao validados na publicacao pelo `ServiceImpl`?
3. **Integridade Referencial, Tenants e Relacoes Cruzadas**:
   - [ ] Entidades escopadas por tenant/titular possuem vinculo obrigatorio e `NOT NULL`?
   - [ ] Consultas e associacoes validam que todos os registros relacionados pertencem ao mesmo tenant/titular autenticado (sem relacoes cruzadas)?
   - [ ] Nao ha `CascadeType.REMOVE` indevido em entidades que devem responder `409 Conflict` quando estiverem em uso (D24)?
4. **Consultas, Indices e Migrations**:
   - [ ] Todas as queries no `Repository` usam parametros vinculados (`?1` ou `:param`), sem concatenacao de strings?
   - [ ] Listagens paginadas possuem `ORDER BY` deterministico (incluindo `id`)?
   - [ ] Chaves estrangeiras e colunas de busca/filtro possuem indices adequados?
   - [ ] Migrations existentes nao foram adulteradas de forma incompativel?
5. **Transacoes, Atomicidade e Concorrencia**:
   - [ ] Metodos de escrita no `ServiceImpl` possuem `@Transactional` garantindo atomicidade de todas as alteracoes no banco?
   - [ ] Operacoes concorrentes criticas (estoque, limite de cupom, mudanca de status concorrente) utilizam bloqueio pessimista (`PESSIMISTIC_WRITE` com ordenacao de IDs) ou `@Version`?
