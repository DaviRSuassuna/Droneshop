package br.com.droneshop.estoque.service;

import java.util.List;
import java.util.Locale;

import org.eclipse.microprofile.jwt.JsonWebToken;

import br.com.droneshop.catalogo.model.Kit;
import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.repository.ProdutoRepository;
import br.com.droneshop.estoque.dto.MovimentoEstoqueRequestDTO;
import br.com.droneshop.estoque.dto.SituacaoEstoque;
import br.com.droneshop.estoque.model.MovimentoEstoque;
import br.com.droneshop.estoque.model.TipoMovimento;
import br.com.droneshop.estoque.repository.EstoqueRepository;
import br.com.droneshop.estoque.repository.MovimentoEstoqueRepository;
import br.com.droneshop.exception.ConflitoException;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.identidade.model.Usuario;
import br.com.droneshop.identidade.repository.UsuarioRepository;
import br.com.droneshop.model.OrigemRegistro;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotAuthorizedException;

@ApplicationScoped
public class MovimentoEstoqueServiceImpl implements MovimentoEstoqueService {

    @Inject
    MovimentoEstoqueRepository movimentoRepository;

    @Inject
    EstoqueRepository estoqueRepository;

    @Inject
    ProdutoRepository produtoRepository;

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    JsonWebToken jwt;

    @Override
    @Transactional
    public MovimentoEstoque registrar(MovimentoEstoqueRequestDTO dto) {
        TipoMovimento tipo = TipoMovimento.valueOf(dto.idTipo());
        if (tipo == null)
            throw ValidationException.of("idTipo", "Tipo de movimento não encontrado (back).");
        int minimo = (tipo == TipoMovimento.AJUSTE) ? 0 : 1;
        if (dto.quantidade() < minimo)
            throw ValidationException.of("quantidade", tipo == TipoMovimento.AJUSTE
                    ? "Informe o saldo novo, zero ou mais (back)."
                    : "A quantidade deve ser de pelo menos 1 (back).");

        // trava a linha do produto: dois movimentos ao mesmo tempo nao leem o mesmo saldo
        Produto produto = produtoRepository.findById(dto.idProduto(), LockModeType.PESSIMISTIC_WRITE);
        if (produto == null)
            throw new ResourceNotFoundException("Produto não encontrado.");
        if (produto instanceof Kit)
            throw ValidationException.of("idProduto",
                    "Kit não tem estoque próprio: lance o movimento em cada componente (back).");

        int atual = (produto.getEstoqueFisico() == null) ? 0 : produto.getEstoqueFisico();
        int saldo;
        int quantidade = dto.quantidade();
        switch (tipo) {
            case ENTRADA, DEVOLUCAO -> saldo = atual + quantidade;
            case SAIDA, PERDA -> saldo = atual - quantidade;
            default -> {
                saldo = quantidade;
                quantidade = saldo - atual;
            }
        }
        if (saldo < 0)
            throw new ConflitoException("Saldo insuficiente: o produto tem " + atual + " em estoque e o movimento "
                    + "retira " + dto.quantidade() + ".");

        MovimentoEstoque movimento = novo(produto, tipo, quantidade, saldo, dto.observacao());
        movimentoRepository.persist(movimento);
        return movimento;
    }

    @Override
    @Transactional
    public void entradaInicial(Produto produto, int quantidade) {
        produto.setEstoqueFisico(quantidade);
        if (quantidade > 0)
            produto.getMovimentos().add(novo(produto, TipoMovimento.ENTRADA, quantidade, quantidade, "Saldo inicial"));
    }

    @Override
    public TipoMovimento tipo(String idTipo) {
        if (idTipo == null || idTipo.isBlank())
            return null;
        TipoMovimento tipo;
        try {
            tipo = TipoMovimento.valueOf(Long.valueOf(idTipo.trim()));
        } catch (NumberFormatException e) {
            tipo = null;
        }
        if (tipo == null)
            throw ValidationException.of("idTipo", "Tipo de movimento não encontrado.");
        return tipo;
    }

    @Override
    public List<MovimentoEstoque> findComFiltro(Long idProduto, TipoMovimento tipo, int page, int pageSize) {
        return movimentoRepository.findComFiltro(idProduto, tipo).page(page, pageSize).list();
    }

    @Override
    public long countComFiltro(Long idProduto, TipoMovimento tipo) {
        return movimentoRepository.findComFiltro(idProduto, tipo).count();
    }

    @Override
    public SituacaoEstoque situacao(String situacao) {
        return situacao(situacao, "situacao");
    }

    @Override
    public SituacaoEstoque situacao(String situacao, String campo) {
        if (situacao == null || situacao.isBlank())
            return SituacaoEstoque.TODOS;
        try {
            return SituacaoEstoque.valueOf(situacao.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ValidationException.of(campo, "Situação inválida: use ABAIXO_MINIMO, ZERADO ou TODOS.");
        }
    }

    @Override
    public List<Produto> findEstoque(SituacaoEstoque situacao, String busca, Long idMarca, int page, int pageSize) {
        return estoqueRepository.findComFiltro(situacao, busca, idMarca).page(page, pageSize).list();
    }

    @Override
    public long countEstoque(SituacaoEstoque situacao, String busca, Long idMarca) {
        return estoqueRepository.findComFiltro(situacao, busca, idMarca).count();
    }

    // Pela API e no cadastro, sempre MANUAL com o usuario do token (D11)
    private MovimentoEstoque novo(Produto produto, TipoMovimento tipo, int quantidade, int saldo,
            String observacao) {
        MovimentoEstoque m = new MovimentoEstoque();
        m.setProduto(produto);
        m.setTipo(tipo);
        m.setOrigem(OrigemRegistro.MANUAL);
        m.setAutor(usuarioLogado());
        m.setQuantidade(quantidade);
        m.setSaldoResultante(saldo);
        m.setObservacao((observacao == null || observacao.isBlank()) ? null : observacao.trim());
        produto.setEstoqueFisico(saldo);
        return m;
    }

    private Usuario usuarioLogado() {
        Usuario usuario = null;
        try {
            if (jwt != null && jwt.getSubject() != null)
                usuario = usuarioRepository.findById(Long.valueOf(jwt.getSubject()));
        } catch (NumberFormatException e) {
            usuario = null;
        }
        if (usuario == null)
            throw new NotAuthorizedException("Credenciais ausentes ou inválidas.");
        return usuario;
    }

}
