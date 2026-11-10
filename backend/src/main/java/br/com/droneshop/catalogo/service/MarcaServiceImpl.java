package br.com.droneshop.catalogo.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jboss.resteasy.reactive.multipart.FileUpload;

import br.com.droneshop.catalogo.dto.MarcaRequestDTO;
import br.com.droneshop.catalogo.model.Marca;
import br.com.droneshop.catalogo.model.StatusMarca;
import br.com.droneshop.catalogo.repository.MarcaRepository;
import br.com.droneshop.catalogo.repository.ProdutoRepository;
import br.com.droneshop.exception.ConflitoException;
import br.com.droneshop.exception.Problem;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.util.Textos;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class MarcaServiceImpl implements MarcaService {
    @Override
    public List<Marca> findComFiltro(String busca, Long idStatus, String paisOrigem, int page, int pageSize) {
        return marcaRepository.findComFiltro(busca, idStatus, paisOrigem).page(page, pageSize).list();
    }
    @Override
    public long countComFiltro(String busca, Long idStatus, String paisOrigem) {
        return marcaRepository.findComFiltro(busca, idStatus, paisOrigem).count();
    }


    @Inject
    MarcaRepository marcaRepository;

    @Inject
    ProdutoRepository produtoRepository;

    @Inject
    ArmazenamentoMidia armazenamento;

    private static final long TAMANHO_MAXIMO_LOGO = 2L * 1024 * 1024;
    private static final byte[] ASSINATURA_PNG = { (byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10 };

    @Override
    @Transactional
    public Marca create(MarcaRequestDTO dto) {
        StatusMarca status = buscarStatusOuFalhar(dto.idStatus());
        validarObrigatoriedade(dto, status);
        String slug = resolverSlug(dto.slug(), dto.nome());
        validarSlugUnico(slug, null);

        Marca novaMarca = new Marca();
        aplicarDados(novaMarca, dto, status, slug);
        marcaRepository.persist(novaMarca);
        return novaMarca;
    }

    @Override
    @Transactional
    public void update(long id, MarcaRequestDTO dto) {
        Marca edicaoMarca = buscarMarcaOuFalhar(id);
        StatusMarca status = buscarStatusOuFalhar(dto.idStatus());
        validarObrigatoriedade(dto, status);
        String slug = resolverSlug(dto.slug(), dto.nome());
        validarSlugUnico(slug, id);

        aplicarDados(edicaoMarca, dto, status, slug);
    }

    @Override
    @Transactional
    public void delete(long id) {
        long emUso = produtoRepository.count("marca.id", id);
        if (emUso > 0)
            throw new ConflitoException("Esta marca tem " + emUso + (emUso == 1 ? " produto" : " produtos")
                    + ". Inative a marca em vez de excluir.");
        if (!marcaRepository.deleteById(id))
            throw new ResourceNotFoundException("Marca não encontrada.");
    }

    // Logo por arquivo (#84): grava no disco das midias (#12) e troca o logoUrl.
    // O logo antigo so e apagado se tambem tiver sido enviado por upload.
    @Override
    @Transactional
    public Marca enviarLogo(long id, FileUpload arquivo) {
        Marca marca = buscarMarcaOuFalhar(id);
        String extensao = validarLogo(arquivo);
        String anterior = marca.getLogoUrl();
        marca.setLogoUrl(armazenamento.gravar(arquivo, extensao));
        if (armazenamento.ehArquivoEnviado(anterior))
            armazenamento.apagar(anterior);
        return marca;
    }

    @Override
    public Marca findById(long id) {
        return buscarMarcaOuFalhar(id);
    }

    @Override
    public Marca findBySlug(String slug) {
        Marca marca = marcaRepository.findBySlug(slug);
        if (marca == null)
            throw new ResourceNotFoundException("Marca não encontrada.");
        return marca;
    }

    @Override
    public List<Marca> findAll(int page, int pageSize) {
        return marcaRepository.findAll().page(page, pageSize).list();
    }

    @Override
    public List<Marca> findByNome(String nome, int page, int pageSize) {
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

    // RASCUNHO nao exige nada preenchido; ATIVA/INATIVA exige nome, pais e logo
    private void validarObrigatoriedade(MarcaRequestDTO dto, StatusMarca status) {
        if (status == StatusMarca.RASCUNHO)
            return;

        List<Problem.FieldError> erros = new ArrayList<>();
        if (dto.nome() == null)
            erros.add(new Problem.FieldError("nome", "O campo deve ser informado (back)."));
        if (dto.paisOrigem() == null)
            erros.add(new Problem.FieldError("paisOrigem", "O campo deve ser informado (back)."));
        if (dto.logoUrl() == null)
            erros.add(new Problem.FieldError("logoUrl", "O campo deve ser informado (back)."));

        if (!erros.isEmpty())
            throw new ValidationException("Preencha os campos obrigatórios para publicar a marca.", erros);
    }

    // So PNG ou SVG ate 2 MB, conferindo o conteudo e nao so a extensao
    private String validarLogo(FileUpload arquivo) {
        if (arquivo == null)
            throw ValidationException.of("arquivo", "Envie o arquivo do logo (back).");
        if (arquivo.size() > TAMANHO_MAXIMO_LOGO)
            throw ValidationException.of("arquivo", "O logo deve ter no máximo 2 MB (back).");
        String nome = arquivo.fileName() == null ? "" : arquivo.fileName().toLowerCase(Locale.ROOT);
        String extensao = nome.substring(nome.lastIndexOf('.') + 1);
        byte[] inicio = armazenamento.inicio(arquivo.filePath(), 1024);
        boolean valido = switch (extensao) {
            case "png" -> comecaCom(inicio, ASSINATURA_PNG);
            case "svg" -> ehSvg(inicio);
            default -> false;
        };
        if (!valido)
            throw ValidationException.of("arquivo", "O logo deve ser um arquivo PNG ou SVG (back).");
        return extensao;
    }

    private boolean comecaCom(byte[] valor, byte[] assinatura) {
        if (valor.length < assinatura.length)
            return false;
        for (int i = 0; i < assinatura.length; i++)
            if (valor[i] != assinatura[i])
                return false;
        return true;
    }

    // SVG e texto: comeca pela declaracao XML ou pela tag e tem <svg logo no inicio
    private boolean ehSvg(byte[] inicio) {
        String texto = new String(inicio, StandardCharsets.UTF_8).replace("\uFEFF", "").stripLeading();
        return (texto.startsWith("<?xml") || texto.startsWith("<svg")) && texto.contains("<svg");
    }

    private void validarSlugUnico(String slug, Long id) {
        if (marcaRepository.findBySlugExceptId(slug, id) != null)
            throw ValidationException.of("slug", "Já existe uma marca cadastrada com esse slug.");
    }

    // sem slug informado, gera a partir do nome; rascunho sem nome ganha um slug temporario
    private String resolverSlug(String slug, String nome) {
        if (slug != null)
            return Textos.slug(slug);
        if (nome != null)
            return Textos.slug(nome);
        return "rascunho-" + System.currentTimeMillis();
    }

    private void aplicarDados(Marca destino, MarcaRequestDTO origem, StatusMarca status, String slug) {
        destino.setNome(valorOuVazio(origem.nome()));
        destino.setSlug(slug);
        destino.setPaisOrigem(valorOuVazio(origem.paisOrigem()));
        destino.setSiteOficial(origem.siteOficial());
        destino.setDescricao(origem.descricao());
        destino.setLogoUrl(valorOuVazio(origem.logoUrl()));
        destino.setStatus(status);
        destino.setDestacarHome(Boolean.TRUE.equals(origem.destacarHome()));
    }

    // colunas NOT NULL guardam "" enquanto a marca for rascunho
    private String valorOuVazio(String valor) {
        return (valor == null) ? "" : valor;
    }

    private Marca buscarMarcaOuFalhar(long id) {
        Marca marca = marcaRepository.findById(id);
        if (marca == null)
            throw new ResourceNotFoundException("Marca não encontrada.");
        return marca;
    }

    private StatusMarca buscarStatusOuFalhar(Long idStatus) {
        StatusMarca status = StatusMarca.valueOf(idStatus);
        if (status == null)
            throw ValidationException.of("idStatus", "Status não encontrado.");
        return status;
    }

}
