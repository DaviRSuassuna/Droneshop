package br.com.droneshop.compra.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import br.com.droneshop.catalogo.model.Categoria;
import br.com.droneshop.catalogo.model.Marca;
import br.com.droneshop.catalogo.repository.CategoriaRepository;
import br.com.droneshop.catalogo.repository.MarcaRepository;
import br.com.droneshop.catalogo.repository.ProdutoRepository;
import br.com.droneshop.compra.dto.CupomRequestDTO;
import br.com.droneshop.compra.model.Cupom;
import br.com.droneshop.compra.model.TipoDescontoCupom;
import br.com.droneshop.compra.model.UsoCupom;
import br.com.droneshop.compra.repository.CupomRepository;
import br.com.droneshop.compra.repository.UsoCupomRepository;
import br.com.droneshop.exception.ConflitoException;
import br.com.droneshop.exception.Problem;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CupomServiceImpl implements CupomService {

    @Inject CupomRepository cupomRepository;
    @Inject UsoCupomRepository usoRepository;
    @Inject CategoriaRepository categoriaRepository;
    @Inject MarcaRepository marcaRepository;
    @Inject ProdutoRepository produtoRepository;

    @Override
    @Transactional
    public Cupom create(CupomRequestDTO dto) {
        Cupom cupom = new Cupom();
        aplicar(cupom, dto);
        cupomRepository.persist(cupom);
        return cupom;
    }

    @Override
    @Transactional
    public void update(long id, CupomRequestDTO dto) {
        aplicar(buscarOuFalhar(id), dto);
    }

    @Override
    @Transactional
    public void delete(long id) {
        Cupom cupom = buscarOuFalhar(id);
        if (usoRepository.countByCupom(id) > 0)
            throw new ConflitoException("Este cupom já foi utilizado em um pedido e não pode ser excluído.");
        cupomRepository.delete(cupom);
    }

    @Override
    public Cupom findById(long id) {
        return buscarOuFalhar(id);
    }

    @Override
    public List<Cupom> findAll(String situacao, String codigo, int page, int pageSize) {
        return cupomRepository.findComFiltro(normalizarSituacao(situacao), codigo, LocalDateTime.now())
                .page(page, pageSize).list();
    }

    @Override
    public long count(String situacao, String codigo) {
        return cupomRepository.findComFiltro(normalizarSituacao(situacao), codigo, LocalDateTime.now()).count();
    }

    @Override
    public List<UsoCupom> usos(long idCupom, int page, int pageSize) {
        buscarOuFalhar(idCupom);
        return usoRepository.find("cupom.id = ?1 order by dataUso desc, id desc", idCupom).page(page, pageSize).list();
    }

    @Override
    public long countUsos(long idCupom) {
        buscarOuFalhar(idCupom);
        return usoRepository.countByCupom(idCupom);
    }

    private void aplicar(Cupom cupom, CupomRequestDTO dto) {
        String codigo = dto.codigo().trim().toUpperCase(Locale.ROOT);
        Cupom existente = cupomRepository.findByCodigo(codigo);
        if (existente != null && !existente.getId().equals(cupom.getId()))
            throw ValidationException.of("codigo", "Já existe um cupom com esse código.");

        TipoDescontoCupom tipo = TipoDescontoCupom.valueOf(dto.idTipoDesconto());
        if (tipo == null)
            throw ValidationException.of("idTipoDesconto", "Tipo de desconto não encontrado.");
        validar(dto, tipo);

        cupom.setCodigo(codigo);
        cupom.setTipoDesconto(tipo);
        cupom.setValor(dto.valor());
        cupom.setDataInicio(dto.dataInicio());
        cupom.setDataFim(dto.dataFim());
        cupom.setValorMinimo(dto.valorMinimo());
        cupom.setLimiteTotalUso(dto.limiteTotalUso());
        cupom.setLimitePorCliente(dto.limitePorCliente());
        cupom.setAtivo(dto.ativo() == null || dto.ativo());
        cupom.setCategorias(buscar(categoriaRepository, dto.idsCategorias(), "idsCategorias"));
        cupom.setMarcas(buscar(marcaRepository, dto.idsMarcas(), "idsMarcas"));
        cupom.setProdutos(buscar(produtoRepository, dto.idsProdutos(), "idsProdutos"));
    }

    private void validar(CupomRequestDTO dto, TipoDescontoCupom tipo) {
        List<Problem.FieldError> erros = new ArrayList<>();
        if (dto.codigo() != null && !dto.codigo().equals(dto.codigo().trim()))
            erros.add(new Problem.FieldError("codigo", "O código não pode conter espaços no início ou no fim."));
        if (dto.valor() != null && (tipo == TipoDescontoCupom.PERCENTUAL
                ? dto.valor().signum() <= 0 || dto.valor().compareTo(java.math.BigDecimal.valueOf(100)) > 0
                : dto.valor().signum() <= 0))
            erros.add(new Problem.FieldError("valor", tipo == TipoDescontoCupom.PERCENTUAL
                    ? "O percentual deve ser maior que zero e no máximo 100."
                    : "O valor fixo deve ser maior que zero."));
        if (dto.dataInicio() != null && dto.dataFim() != null && dto.dataFim().isBefore(dto.dataInicio()))
            erros.add(new Problem.FieldError("dataFim", "A data de fim deve ser igual ou posterior à data de início."));
        if (dto.limitePorCliente() != null && dto.limiteTotalUso() != null
                && dto.limitePorCliente() > dto.limiteTotalUso())
            erros.add(new Problem.FieldError("limitePorCliente", "O limite por cliente não pode superar o limite total."));
        if (!erros.isEmpty())
            throw new ValidationException("Revise os dados do cupom.", erros);
    }

    private String normalizarSituacao(String situacao) {
        if (situacao == null || situacao.isBlank())
            return "";
        String normalizada = situacao.trim().toUpperCase(Locale.ROOT);
        if (!List.of("VIGENTE", "AGENDADO", "ENCERRADO", "ESGOTADO", "INATIVO", "PAUSADO").contains(normalizada))
            throw ValidationException.of("situacao", "Situação de cupom inválida.");
        return normalizada;
    }

    private <T extends br.com.droneshop.model.DefaultEntity> List<T> buscar(
            io.quarkus.hibernate.orm.panache.PanacheRepository<T> repository, List<Long> ids, String campo) {
        if (ids == null || ids.isEmpty())
            return new ArrayList<>();
        if (new HashSet<>(ids).size() != ids.size())
            throw ValidationException.of(campo, "Não informe o mesmo item mais de uma vez.");
        List<T> entidades = repository.list("id in ?1", ids);
        if (entidades.size() != ids.size())
            throw ValidationException.of(campo, "Uma ou mais referências não foram encontradas.");
        return entidades;
    }

    private Cupom buscarOuFalhar(long id) {
        Cupom cupom = cupomRepository.findById(id);
        if (cupom == null)
            throw new ResourceNotFoundException("Cupom não encontrado.");
        return cupom;
    }
}
