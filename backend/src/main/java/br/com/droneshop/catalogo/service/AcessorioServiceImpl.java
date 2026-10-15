package br.com.droneshop.catalogo.service;

import java.util.ArrayList;
import java.util.List;

import br.com.droneshop.catalogo.dto.AcessorioRequestDTO;
import br.com.droneshop.catalogo.model.Acessorio;
import br.com.droneshop.catalogo.model.StatusProduto;
import br.com.droneshop.exception.Problem;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AcessorioServiceImpl implements AcessorioService {

    @Inject
    ProdutoCadastro cadastro;

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
        cadastro.validarObrigatorios(dto, status, errosDoTipo);
        cadastro.aplicar(destino, dto, status, idAtual);
        destino.setTipoAcessorio(dto.tipoAcessorio());
    }

}
