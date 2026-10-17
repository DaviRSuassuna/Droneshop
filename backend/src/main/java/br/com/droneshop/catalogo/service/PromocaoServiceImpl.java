package br.com.droneshop.catalogo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import br.com.droneshop.catalogo.dto.PromocaoRequestDTO;
import br.com.droneshop.catalogo.model.EscopoPromocao;
import br.com.droneshop.catalogo.model.Promocao;
import br.com.droneshop.catalogo.model.VarianteProduto;
import br.com.droneshop.catalogo.repository.PromocaoRepository;
import br.com.droneshop.catalogo.repository.VarianteProdutoRepository;
import br.com.droneshop.exception.ConflitoException;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PromocaoServiceImpl implements PromocaoService {

    @Inject
    PromocaoRepository promocaoRepository;

    @Inject
    VarianteProdutoRepository varianteRepository;

    @Override
    @Transactional
    public Promocao create(PromocaoRequestDTO dto) {
        Promocao promocao = new Promocao();
        aplicar(promocao, dto);
        promocaoRepository.persist(promocao);
        return promocao;
    }

    @Override
    @Transactional
    public void update(long id, PromocaoRequestDTO dto) {
        aplicar(buscarOuFalhar(id), dto);
    }

    @Override
    @Transactional
    public void delete(long id) {
        Promocao promocao = buscarOuFalhar(id);
        if (!promocao.getDescontos().isEmpty())
            throw new ConflitoException("Esta promoção já foi aplicada em um pedido e não pode ser excluída.");
        promocaoRepository.delete(promocao);
    }

    @Override
    public Promocao findById(long id) {
        return buscarOuFalhar(id);
    }

    @Override
    public List<Promocao> findAll(String situacao, int page, int pageSize) {
        return promocaoRepository.findBySituacao(validarSituacao(situacao), LocalDateTime.now())
                .page(page, pageSize).list();
    }

    @Override
    public long count(String situacao) {
        return promocaoRepository.findBySituacao(validarSituacao(situacao), LocalDateTime.now()).count();
    }

    private void aplicar(Promocao promocao, PromocaoRequestDTO dto) {
        EscopoPromocao escopo = EscopoPromocao.valueOf(dto.idEscopo());
        if (escopo == null)
            throw ValidationException.of("idEscopo", "Escopo de promoção não encontrado.");
        validar(dto, escopo);

        List<VarianteProduto> variantes = buscarVariantes(dto.idsVariantes());
        promocao.setNome(dto.nome().trim());
        promocao.setEscopo(escopo);
        promocao.setDataInicio(dto.dataInicio());
        promocao.setDataFim(dto.dataFim());
        promocao.setPercentualDesconto(dto.percentualDesconto());
        promocao.setValorDesconto(dto.valorDesconto());
        promocao.setValorMinimoCarrinho(dto.valorMinimoCarrinho());
        promocao.setAtiva(Boolean.TRUE.equals(dto.ativa()));
        promocao.setVariantes(variantes);
    }

    private void validar(PromocaoRequestDTO dto, EscopoPromocao escopo) {
        List<br.com.droneshop.exception.Problem.FieldError> erros = new ArrayList<>();
        boolean semPercentual = dto.percentualDesconto() == null;
        boolean semValor = dto.valorDesconto() == null;
        if (semPercentual == semValor)
            erros.add(new br.com.droneshop.exception.Problem.FieldError(
                    "percentualDesconto", "Informe percentual ou valor fixo, mas não ambos."));
        if (dto.dataInicio() != null && dto.dataFim() != null && dto.dataFim().isBefore(dto.dataInicio()))
            erros.add(new br.com.droneshop.exception.Problem.FieldError(
                    "dataFim", "A data de fim deve ser igual ou posterior à data de início."));
        if (escopo == EscopoPromocao.PRODUTO && (dto.idsVariantes() == null || dto.idsVariantes().isEmpty()))
            erros.add(new br.com.droneshop.exception.Problem.FieldError(
                    "idsVariantes", "Selecione ao menos uma variante para uma promoção de produto."));
        if (escopo != EscopoPromocao.PRODUTO && dto.idsVariantes() != null && !dto.idsVariantes().isEmpty())
            erros.add(new br.com.droneshop.exception.Problem.FieldError(
                    "idsVariantes", "Somente promoções de produto podem vincular variantes."));
        if (escopo == EscopoPromocao.CARRINHO && dto.valorMinimoCarrinho() == null)
            erros.add(new br.com.droneshop.exception.Problem.FieldError(
                    "valorMinimoCarrinho", "Informe o valor mínimo do carrinho."));
        if (dto.idsVariantes() != null && new HashSet<>(dto.idsVariantes()).size() != dto.idsVariantes().size())
            erros.add(new br.com.droneshop.exception.Problem.FieldError(
                    "idsVariantes", "Não informe a mesma variante mais de uma vez."));
        if (!erros.isEmpty())
            throw new ValidationException("Revise os dados da promoção.", erros);
    }

    private String validarSituacao(String situacao) {
        if (situacao == null || situacao.isBlank())
            return "";
        String normalizada = situacao.trim().toUpperCase();
        if (!List.of("VIGENTE", "AGENDADA", "ENCERRADA", "INATIVA").contains(normalizada))
            throw ValidationException.of("situacao", "Situação de promoção inválida.");
        return normalizada;
    }

    private List<VarianteProduto> buscarVariantes(List<Long> ids) {
        if (ids == null || ids.isEmpty())
            return new ArrayList<>();
        List<VarianteProduto> variantes = varianteRepository.list("id in ?1", ids);
        if (variantes.size() != ids.size()) {
            HashSet<Long> encontrados = new HashSet<>();
            variantes.forEach(v -> encontrados.add(v.getId()));
            Long ausente = ids.stream().filter(id -> !encontrados.contains(id)).findFirst().orElse(null);
            throw ValidationException.of("idsVariantes", "Variante não encontrada (id " + ausente + ").");
        }
        return variantes;
    }

    private Promocao buscarOuFalhar(long id) {
        Promocao promocao = promocaoRepository.findById(id);
        if (promocao == null)
            throw new ResourceNotFoundException("Promoção não encontrada.");
        return promocao;
    }
}
