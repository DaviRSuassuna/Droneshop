package br.com.droneshop.catalogo.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import br.com.droneshop.catalogo.dto.AcessorioRequestDTO;
import br.com.droneshop.catalogo.model.Acessorio;
import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.model.StatusProduto;
import br.com.droneshop.catalogo.repository.ProdutoRepository;
import br.com.droneshop.exception.Problem;
import br.com.droneshop.exception.ValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AcessorioServiceImpl implements AcessorioService {

    @Inject
    ProdutoCadastro cadastro;

    @Inject
    ProdutoRepository produtoRepository;

    @Override
    @Transactional
    public Acessorio create(AcessorioRequestDTO dto) {
        Acessorio acessorio = new Acessorio();
        aplicarDados(acessorio, dto, null);
        cadastro.persistir(acessorio);
        return acessorio;
    }

    @Override
    @Transactional
    public void update(long id, AcessorioRequestDTO dto) {
        aplicarDados(cadastro.buscarOuFalhar(id, Acessorio.class), dto, id);
    }

    private void aplicarDados(Acessorio destino, AcessorioRequestDTO dto, Long idAtual) {
        StatusProduto status = cadastro.status(dto.idStatus());
        List<Problem.FieldError> errosDoTipo = new ArrayList<>();
        if (dto.tipoAcessorio() == null)
            errosDoTipo.add(new Problem.FieldError("tipoAcessorio", ProdutoCadastro.OBRIGATORIO));
        cadastro.validarObrigatorios(dto, status, null, errosDoTipo);
        Set<Produto> compativeis = buscarCompativeis(dto.idsCompativeis(), idAtual);
        cadastro.aplicar(destino, dto, status, idAtual);
        destino.setTipoAcessorio(dto.tipoAcessorio());
        destino.setPesoItemG(dto.pesoItemG());
        destino.setSugerirNosCompativeis(Boolean.TRUE.equals(dto.sugerirNosCompativeis()));
        destino.getCompativeis().clear();
        destino.getCompativeis().addAll(compativeis);
    }

    // Lista vazia ou nula limpa o vinculo; id repetido conta uma vez so
    private Set<Produto> buscarCompativeis(List<Long> ids, Long idAtual) {
        Set<Produto> produtos = new LinkedHashSet<>();
        if (ids == null)
            return produtos;
        for (Long id : new LinkedHashSet<>(ids)) {
            if (id == null)
                continue;
            if (id.equals(idAtual))
                throw ValidationException.of("idsCompativeis", "O acessório não pode ser compatível com ele mesmo.");
            Produto produto = produtoRepository.findById(id);
            if (produto == null)
                throw ValidationException.of("idsCompativeis", "Produto compatível não encontrado (id " + id + ").");
            produtos.add(produto);
        }
        return produtos;
    }

}
