package br.com.droneshop.catalogo.service;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.jboss.resteasy.reactive.multipart.FileUpload;

import br.com.droneshop.catalogo.dto.MidiaProdutoAlteracaoRequestDTO;
import br.com.droneshop.catalogo.dto.MidiaProdutoOrdemRequestDTO;
import br.com.droneshop.catalogo.dto.MidiaProdutoRequestDTO;
import br.com.droneshop.catalogo.model.MidiaProduto;
import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.model.TipoMidia;
import br.com.droneshop.catalogo.repository.MidiaProdutoRepository;
import br.com.droneshop.catalogo.repository.ProdutoRepository;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

@ApplicationScoped
public class MidiaProdutoServiceImpl implements MidiaProdutoService {

    private static final long TAMANHO_MAXIMO = 20L * 1024 * 1024;

    @Inject
    MidiaProdutoRepository midiaRepository;

    @Inject
    ProdutoRepository produtoRepository;

    @Inject
    Validator validator;

    @Inject
    ArmazenamentoMidia armazenamento;

    @Override
    @Transactional
    public MidiaProduto criar(long idProduto, MidiaProdutoRequestDTO dto) {
        validar(dto);
        Produto produto = buscarProdutoComLock(idProduto);
        TipoMidia tipo = TipoMidia.valueOf(dto.idTipo());
        if (tipo == null)
            throw ValidationException.of("idTipo", "Tipo de mídia inválido.");

        String altText = dto.altText() == null ? null : dto.altText().trim();
        if (tipo == TipoMidia.IMAGEM && (altText == null || altText.isBlank()))
            throw ValidationException.of("altText", "Informe o texto alternativo da imagem.");

        String url;
        if (tipo == TipoMidia.VIDEO) {
            if (dto.url() == null || dto.url().isBlank())
                throw ValidationException.of("url", "Informe a URL do vídeo.");
            url = validarUrl(dto.url().trim());
            if (dto.arquivo() != null)
                throw ValidationException.of("arquivo", "Vídeos devem ser informados por URL.");
        } else {
            if (dto.arquivo() == null)
                throw ValidationException.of("arquivo", "Envie um arquivo para esta mídia.");
            validarArquivo(tipo, dto.arquivo());
            if (dto.url() != null && !dto.url().isBlank())
                throw ValidationException.of("url", "Arquivos de imagem e PDF não aceitam URL.");
            url = gravarArquivo(dto.arquivo());
        }

        MidiaProduto midia = new MidiaProduto();
        midia.setProduto(produto);
        midia.setTipo(tipo);
        midia.setUrl(url);
        midia.setAltText(altText);
        midia.setOrdem(dto.ordem() == null ? proximaOrdem(idProduto) : dto.ordem());
        midia.setPrincipal(Boolean.TRUE.equals(dto.principal()));
        if (midia.isPrincipal())
            desmarcarPrincipais(idProduto, null);
        midiaRepository.persist(midia);
        return midia;
    }

    @Override
    public List<MidiaProduto> listar(long idProduto) {
        buscarProduto(idProduto);
        return midiaRepository.findByProdutoId(idProduto);
    }

    @Override
    @Transactional
    public void alterar(long id, MidiaProdutoAlteracaoRequestDTO dto) {
        validar(dto);
        if (dto.principal() == null && dto.ordem() == null && dto.altText() == null)
            throw ValidationException.of("midia", "Informe ao menos um campo para alterar.");

        MidiaProduto midia = buscarMidia(id);
        if (dto.ordem() != null)
            midia.setOrdem(dto.ordem());
        if (dto.altText() != null) {
            String altText = dto.altText().trim();
            if (midia.getTipo() == TipoMidia.IMAGEM && altText.isBlank())
                throw ValidationException.of("altText", "Informe o texto alternativo da imagem.");
            midia.setAltText(altText.isBlank() ? null : altText);
        }
        if (dto.principal() != null) {
            if (dto.principal()) {
                buscarProdutoComLock(midia.getProduto().getId());
                desmarcarPrincipais(midia.getProduto().getId(), id);
            }
            midia.setPrincipal(dto.principal());
        }
    }

    @Override
    @Transactional
    public void reordenar(long idProduto, MidiaProdutoOrdemRequestDTO dto) {
        buscarProdutoComLock(idProduto);
        List<Long> ids = dto == null ? null : dto.ids();
        if (ids == null || ids.stream().anyMatch(id -> id == null) || ids.size() != Set.copyOf(ids).size())
            throw ValidationException.of("ids", "Informe cada mídia do produto uma única vez.");

        List<MidiaProduto> midias = midiaRepository.findByProdutoId(idProduto);
        Set<Long> idsAtuais = midias.stream().map(MidiaProduto::getId).collect(java.util.stream.Collectors.toSet());
        if (ids.size() != idsAtuais.size() || !idsAtuais.equals(Set.copyOf(ids)))
            throw ValidationException.of("ids", "A lista deve conter todas as mídias deste produto.");

        var porId = new java.util.HashMap<Long, MidiaProduto>();
        for (MidiaProduto midia : midias)
            porId.put(midia.getId(), midia);
        for (int i = 0; i < ids.size(); i++)
            porId.get(ids.get(i)).setOrdem(i + 1);
    }

    @Override
    @Transactional
    public void excluir(long id) {
        MidiaProduto midia = buscarMidia(id);
        long idProduto = midia.getProduto().getId();
        buscarProdutoComLock(idProduto);
        removerArquivo(midia);
        midiaRepository.delete(midia);
        // as que sobram voltam a ser 1..n, sem buraco na ordem
        int ordem = 1;
        for (MidiaProduto restante : midiaRepository.findByProdutoId(idProduto))
            if (restante.getId() != id)
                restante.setOrdem(ordem++);
    }

    @Override
    @Transactional
    public void excluirDoProduto(long idProduto) {
        List<MidiaProduto> midias = midiaRepository.findByProdutoId(idProduto);
        for (MidiaProduto midia : midias)
            removerArquivo(midia);
        for (MidiaProduto midia : midias)
            midiaRepository.delete(midia);
    }

    @Override
    public ArquivoMidia buscarArquivo(String nome) throws IOException {
        ArmazenamentoMidia.Arquivo arquivo = armazenamento.buscar(nome);
        return new ArquivoMidia(arquivo.conteudo(), arquivo.contentType());
    }

    private void validar(MidiaProdutoRequestDTO dto) {
        if (dto == null)
            throw ValidationException.of("midia", "Informe os dados da mídia.");
        List<br.com.droneshop.exception.Problem.FieldError> erros = new ArrayList<>();
        for (ConstraintViolation<MidiaProdutoRequestDTO> violacao : validator.validate(dto))
            erros.add(new br.com.droneshop.exception.Problem.FieldError(
                    violacao.getPropertyPath().toString(), violacao.getMessage()));
        if (!erros.isEmpty())
            throw new ValidationException("Dados inválidos.", erros);
    }

    private void validar(MidiaProdutoAlteracaoRequestDTO dto) {
        if (dto == null)
            throw ValidationException.of("midia", "Informe os dados para alteração.");
        List<br.com.droneshop.exception.Problem.FieldError> erros = new ArrayList<>();
        for (ConstraintViolation<MidiaProdutoAlteracaoRequestDTO> violacao : validator.validate(dto))
            erros.add(new br.com.droneshop.exception.Problem.FieldError(
                    violacao.getPropertyPath().toString(), violacao.getMessage()));
        if (!erros.isEmpty())
            throw new ValidationException("Dados inválidos.", erros);
    }

    private Produto buscarProduto(long id) {
        Produto produto = produtoRepository.findById(id);
        if (produto == null)
            throw new ResourceNotFoundException("Produto não encontrado.");
        return produto;
    }

    private Produto buscarProdutoComLock(long id) {
        Produto produto = produtoRepository.findById(id, LockModeType.PESSIMISTIC_WRITE);
        if (produto == null)
            throw new ResourceNotFoundException("Produto não encontrado.");
        return produto;
    }

    private MidiaProduto buscarMidia(long id) {
        MidiaProduto midia = midiaRepository.findById(id);
        if (midia == null)
            throw new ResourceNotFoundException("Mídia não encontrada.");
        return midia;
    }

    private void desmarcarPrincipais(long idProduto, Long idIgnorado) {
        for (MidiaProduto midia : midiaRepository.findPrincipais(idProduto, idIgnorado))
            midia.setPrincipal(false);
    }

    private int proximaOrdem(long idProduto) {
        return midiaRepository.findByProdutoId(idProduto).stream()
                .mapToInt(MidiaProduto::getOrdem)
                .max()
                .orElse(0) + 1;
    }

    private String validarUrl(String valor) {
        try {
            URI uri = URI.create(valor);
            String esquema = uri.getScheme();
            if (uri.getHost() == null || esquema == null
                    || (!esquema.equalsIgnoreCase("http") && !esquema.equalsIgnoreCase("https")))
                throw ValidationException.of("url", "Informe uma URL HTTP ou HTTPS válida.");
            return uri.toString();
        } catch (IllegalArgumentException e) {
            throw ValidationException.of("url", "Informe uma URL HTTP ou HTTPS válida.");
        }
    }

    private void validarArquivo(TipoMidia tipo, FileUpload upload) {
        if (upload.size() > TAMANHO_MAXIMO)
            throw ValidationException.of("arquivo", "O arquivo deve ter no máximo 20 MB.");
        String nome = upload.fileName() == null ? "" : upload.fileName().toLowerCase(Locale.ROOT);
        String extensao = nome.substring(nome.lastIndexOf('.') + 1);
        boolean formatoValido = switch (tipo) {
            case IMAGEM -> Set.of("png", "jpg", "jpeg").contains(extensao)
                    && conteudoValido(extensao, upload.filePath());
            case MANUAL_PDF -> extensao.equals("pdf") && conteudoValido(extensao, upload.filePath());
            case VIDEO -> false;
        };
        if (!formatoValido)
            throw ValidationException.of("arquivo", "O formato do arquivo não corresponde ao tipo de mídia.");
    }

    private boolean conteudoValido(String extensao, Path caminho) {
        byte[] inicio = armazenamento.inicio(caminho, 8);
        return switch (extensao) {
            case "png" -> assinatura(inicio,
                    new byte[] { (byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10 });
            case "jpg", "jpeg" -> assinatura(inicio, new byte[] { (byte) 0xff, (byte) 0xd8, (byte) 0xff });
            case "pdf" -> inicio.length >= 5 && inicio[0] == '%'
                    && inicio[1] == 'P' && inicio[2] == 'D' && inicio[3] == 'F' && inicio[4] == '-';
            default -> false;
        };
    }

    private boolean assinatura(byte[] valor, byte[] assinatura) {
        if (valor.length < assinatura.length)
            return false;
        for (int i = 0; i < assinatura.length; i++) {
            if (valor[i] != assinatura[i])
                return false;
        }
        return true;
    }

    private String gravarArquivo(FileUpload upload) {
        String nome = upload.fileName().toLowerCase(Locale.ROOT);
        return armazenamento.gravar(upload, nome.substring(nome.lastIndexOf('.') + 1));
    }

    private void removerArquivo(MidiaProduto midia) {
        if (midia.getTipo() == TipoMidia.VIDEO)
            return;
        armazenamento.apagar(midia.getUrl());
    }
}
