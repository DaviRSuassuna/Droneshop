package br.com.droneshop.catalogo.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import br.com.droneshop.catalogo.dto.ProdutoLoteRequestDTO;
import br.com.droneshop.catalogo.dto.ProdutoLoteResponseDTO;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ConflitoException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ProdutoLoteServiceImpl implements ProdutoLoteService {
    @Inject ProdutoLoteItemService itemService;

    // Cada item confirma a própria transação; uma validação não desfaz os anteriores.
    @Override
    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public ProdutoLoteResponseDTO executar(ProdutoLoteRequestDTO dto) {
        if ("MOVER_SUBCATEGORIA".equals(dto.acao()) && dto.idSubcategoria() == null)
            throw ValidationException.of("idSubcategoria", "Selecione a subcategoria (back).");
        if ("REAJUSTAR_PRECO".equals(dto.acao()) && dto.percentual() == null)
            throw ValidationException.of("percentual", "Informe o percentual de reajuste (back).");
        int processados = 0;
        var falhas = new ArrayList<ProdutoLoteResponseDTO.Falha>();
        for (Long id : new LinkedHashSet<>(dto.ids())) {
            try {
                itemService.executar(id, dto);
                processados++;
            } catch (ValidationException e) {
                String motivo = e.getFieldErrors().stream().map(erro -> erro.field + ": " + erro.message)
                        .collect(Collectors.joining("; "));
                falhas.add(new ProdutoLoteResponseDTO.Falha(id, motivo.isEmpty() ? e.getMessage() : motivo));
            } catch (ResourceNotFoundException | ConflitoException e) {
                falhas.add(new ProdutoLoteResponseDTO.Falha(id, e.getMessage()));
            }
        }
        return new ProdutoLoteResponseDTO(processados, falhas);
    }
}
