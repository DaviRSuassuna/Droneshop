package br.com.droneshop.catalogo.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import br.com.droneshop.catalogo.dto.ProdutoLoteRequestDTO;
import br.com.droneshop.catalogo.mapper.ProdutoAlteracaoMapper;
import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.model.Drone;
import br.com.droneshop.catalogo.model.Acessorio;
import br.com.droneshop.catalogo.model.Kit;
import br.com.droneshop.catalogo.model.StatusProduto;
import br.com.droneshop.catalogo.repository.ProdutoRepository;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.Problem;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ProdutoLoteItemService {
    @Inject ProdutoRepository repository;
    @Inject DroneService droneService;
    @Inject AcessorioService acessorioService;
    @Inject KitService kitService;
    @Inject jakarta.validation.Validator validator;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void executar(Long id, ProdutoLoteRequestDTO dto) {
        Produto p = repository.findById(id, LockModeType.PESSIMISTIC_WRITE);
        if (p == null) throw new ResourceNotFoundException("Produto não encontrado.");
        Long idStatus = p.getStatus().getId();
        Long idSubcategoria = p.getSubcategoria() == null ? null : p.getSubcategoria().getId();
        BigDecimal preco = p.getPreco();
        switch (dto.acao()) {
            case "ATIVAR" -> idStatus = StatusProduto.ATIVO.getId();
            case "INATIVAR" -> idStatus = StatusProduto.INATIVO.getId();
            case "MOVER_SUBCATEGORIA" -> idSubcategoria = dto.idSubcategoria();
            case "REAJUSTAR_PRECO" -> {
                if (preco == null) throw ValidationException.of("preco", "Produto sem preço de venda (back).");
                preco = preco.multiply(BigDecimal.ONE.add(dto.percentual().movePointLeft(2)))
                        .setScale(2, RoundingMode.HALF_UP);
            }
            default -> throw ValidationException.of("acao", "Ação inválida (back).");
        }
        // Os mesmos serviços e Bean Validation do cadastro, inclusive rascunho e D39.
        if (p instanceof Drone drone) droneService.update(id, validar(ProdutoAlteracaoMapper.toRequest(drone, idStatus, idSubcategoria, preco)));
        else if (p instanceof Acessorio acessorio) acessorioService.update(id, validar(ProdutoAlteracaoMapper.toRequest(acessorio, idStatus, idSubcategoria, preco)));
        else if (p instanceof Kit kit) kitService.update(id, validar(ProdutoAlteracaoMapper.toRequest(kit, idStatus, idSubcategoria, preco)));
    }

    private <T> T validar(T dto) {
        var erros = validator.validate(dto).stream()
                .map(v -> new Problem.FieldError(v.getPropertyPath().toString(), v.getMessage())).toList();
        if (!erros.isEmpty()) throw new ValidationException("Revise o produto.", erros);
        return dto;
    }
}
