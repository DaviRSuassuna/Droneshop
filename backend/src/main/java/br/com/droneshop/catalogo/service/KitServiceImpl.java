package br.com.droneshop.catalogo.service;

import java.util.ArrayList;
import java.util.List;

import br.com.droneshop.catalogo.dto.KitRequestDTO;
import br.com.droneshop.catalogo.model.Kit;
import br.com.droneshop.catalogo.model.StatusProduto;
import br.com.droneshop.exception.Problem;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class KitServiceImpl implements KitService {

    @Inject
    ProdutoCadastro cadastro;

    @Override
    @Transactional
    public Kit create(KitRequestDTO dto) {
        Kit kit = new Kit();
        aplicarDados(kit, dto, null);
        cadastro.persistir(kit);
        return kit;
    }

    @Override
    @Transactional
    public void update(long id, KitRequestDTO dto) {
        aplicarDados(cadastro.buscarOuFalhar(id, Kit.class), dto, id);
    }

    private void aplicarDados(Kit destino, KitRequestDTO dto, Long idAtual) {
        StatusProduto status = cadastro.status(dto.idStatus());
        List<KitCalculo.Parte> partes = cadastro.validarComponentes(dto.estoqueFisico(), dto.componentes(), idAtual);
        cadastro.validarObrigatorios(dto, status, partes, obrigatoriosDoKit(partes));
        cadastro.aplicar(destino, dto, status, idAtual);
        cadastro.sincronizarComponentes(destino, partes);
    }

    // So vale fora do rascunho (validarObrigatorios ignora a lista no RASCUNHO)
    private List<Problem.FieldError> obrigatoriosDoKit(List<KitCalculo.Parte> partes) {
        List<Problem.FieldError> erros = new ArrayList<>();
        if (partes.isEmpty())
            erros.add(new Problem.FieldError("componentes",
                    "Escolha ao menos um produto componente para publicar o kit (back)."));
        return erros;
    }

}
