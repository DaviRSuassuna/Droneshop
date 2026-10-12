package br.com.droneshop.catalogo.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import br.com.droneshop.catalogo.dto.MarcaDTO;
import br.com.droneshop.catalogo.model.Marca;
import br.com.droneshop.catalogo.model.StatusMarca;
import br.com.droneshop.catalogo.repository.MarcaRepository;
import br.com.droneshop.catalogo.repository.ProdutoRepository;
import br.com.droneshop.exception.ConflitoException;
import br.com.droneshop.exception.Problem;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class MarcaServiceImpl implements MarcaService {

    @Inject
    MarcaRepository marcaRepository;

    @Inject
    ProdutoRepository produtoRepository;

    @Override
    @Transactional
    public Marca create(MarcaDTO dto) {
        StatusMarca status = parseStatus(dto.getStatus());
        validarObrigatoriedade(dto, status);

        String slug = resolverSlug(dto.getSlug(), dto.getNome());
        validarSlugUnico(slug, null);

        Marca novaMarca = new Marca();
        novaMarca.setNome(valorOuVazio(dto.getNome()));
        novaMarca.setSlug(slug);
        novaMarca.setPaisOrigem(valorOuVazio(dto.getPaisOrigem()));
        novaMarca.setSiteOficial(dto.getSiteOficial());
        novaMarca.setDescricao(dto.getDescricao());
        novaMarca.setLogoUrl(valorOuVazio(dto.getLogoUrl()));
        novaMarca.setStatus(status);
        novaMarca.setDestacarHome(Boolean.TRUE.equals(dto.getDestacarHome()));

        // realizando inclusao
        marcaRepository.persist(novaMarca);

        return novaMarca;
    }

    @Override
    @Transactional
    public void update(long id, MarcaDTO dto) {
        StatusMarca status = parseStatus(dto.getStatus());
        validarObrigatoriedade(dto, status);

        Marca edicaoMarca = findById(id);

        String slug = resolverSlug(dto.getSlug(), dto.getNome());
        validarSlugUnico(slug, id);

        edicaoMarca.setNome(valorOuVazio(dto.getNome()));
        edicaoMarca.setSlug(slug);
        edicaoMarca.setPaisOrigem(valorOuVazio(dto.getPaisOrigem()));
        edicaoMarca.setSiteOficial(dto.getSiteOficial());
        edicaoMarca.setDescricao(dto.getDescricao());
        edicaoMarca.setLogoUrl(valorOuVazio(dto.getLogoUrl()));
        edicaoMarca.setStatus(status);
        edicaoMarca.setDestacarHome(Boolean.TRUE.equals(dto.getDestacarHome()));
    }

    private StatusMarca parseStatus(String status) {
        try {
            return StatusMarca.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ValidationException.of("status", "Status inválido. Use RASCUNHO, ATIVA ou INATIVA.");
        }
    }

    // RASCUNHO nao exige nada preenchido; ATIVA/INATIVA exige nome, pais e logo
    private void validarObrigatoriedade(MarcaDTO dto, StatusMarca status) {
        if (status == StatusMarca.RASCUNHO)
            return;

        List<Problem.FieldError> erros = new ArrayList<>();
        if (dto.getNome() == null || dto.getNome().isBlank())
            erros.add(new Problem.FieldError("nome", "O campo deve ser informado (back)."));
        if (dto.getPaisOrigem() == null || dto.getPaisOrigem().isBlank())
            erros.add(new Problem.FieldError("paisOrigem", "O campo deve ser informado (back)."));
        if (dto.getLogoUrl() == null || dto.getLogoUrl().isBlank())
            erros.add(new Problem.FieldError("logoUrl", "O campo deve ser informado (back)."));

        if (!erros.isEmpty())
            throw new ValidationException("Preencha os campos obrigatórios para publicar a marca.", erros);
    }

    private String valorOuVazio(String valor) {
        return (valor == null) ? "" : valor;
    }

    private void validarSlugUnico(String slug, Long id) {
        Marca marca = marcaRepository.findBySlugExceptId(slug, id);
        if (marca != null)
            throw ValidationException.of("slug", "Já existe uma marca cadastrada com esse slug.");
    }

    // se o slug nao vier preenchido no DTO, gera a partir do nome;
    // se nome tambem estiver vazio (rascunho totalmente em branco), gera um slug temporario
    private String resolverSlug(String slugInformado, String nome) {
        if (slugInformado != null && !slugInformado.isBlank())
            return slugify(slugInformado);
        if (nome != null && !nome.isBlank())
            return slugify(nome);
        return "rascunho-" + System.currentTimeMillis();
    }

    private String slugify(String texto) {
        String semAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcentos.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s-]+", "-")
                .replaceAll("^-|-$", "");
    }

    @Override
    @Transactional
    public void delete(long id) {
        long emUso = produtoRepository.count("marca.id", id);
        if (emUso > 0)
            throw new ConflitoException("Esta marca tem " + emUso + (emUso == 1 ? " produto" : " produtos")
                    + ". Inative a marca em vez de excluir.");
        marcaRepository.deleteById(id);
    }

    @Override
    public Marca findById(long id) {
        Marca marca = marcaRepository.findById(id);
        if (marca == null)
            throw new ResourceNotFoundException("Marca não encontrada.");
        return marca;
    }

    @Override
    public Marca findBySlug(String slug) {
        Marca marca = marcaRepository.findBySlug(slug);
        if (marca == null)
            throw new ResourceNotFoundException("Marca não encontrada.");
        return marca;
    }

    @Override
    public List<Marca> findAll(Integer page, Integer pageSize) {
        PanacheQuery<Marca> query = marcaRepository.findAll().page(page, pageSize);
        return query.list();
    }

    @Override
    public List<Marca> findByNome(String nome, Integer page, Integer pageSize) {
        return marcaRepository.findByNome(nome).page(page, pageSize).list();
    }

    @Override
    public long count() {
        return marcaRepository.findAll().count();
    }

    @Override
    public long count(String nome) {
        return marcaRepository.findByNome(nome).count();
    }

}
