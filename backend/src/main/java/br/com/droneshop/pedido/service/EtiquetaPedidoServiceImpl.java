package br.com.droneshop.pedido.service;

import java.util.ArrayList;
import java.util.List;
import br.com.droneshop.administracao.model.ConfiguracaoLoja;
import br.com.droneshop.pedido.model.Pedido;
import br.com.droneshop.administracao.service.ConfiguracaoLojaService;
import br.com.droneshop.exception.ConflitoException;
import br.com.droneshop.pedido.mapper.PedidoResponseMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class EtiquetaPedidoServiceImpl implements EtiquetaPedidoService {
    @Inject PedidoService pedidoService;
    @Inject ConfiguracaoLojaService configuracaoService;

    @Override
    @Transactional
    public byte[] gerar(long idPedido) {
        var pedido = pedidoService.findById(idPedido);
        if (pedido.getEnderecoEntrega() == null) throw new ConflitoException("O pedido não tem endereço de entrega para gerar a etiqueta.");
        return PdfEtiqueta.gerar(linhas(pedido, configuracaoService.buscar()));
    }

    @Override
    @Transactional
    public byte[] gerarLote(List<Long> ids) {
        List<Pedido> pedidos = ids.stream().map(pedidoService::findById).toList();
        List<String> semEndereco = pedidos.stream().filter(p -> p.getEnderecoEntrega() == null)
                .map(p -> PedidoResponseMapper.numero(p.getId())).toList();
        if (!semEndereco.isEmpty())
            throw new ConflitoException("Pedidos sem endereço de entrega para a etiqueta: " + String.join(", ", semEndereco) + ".");
        var configuracao = configuracaoService.buscar();
        return PdfEtiqueta.gerarPaginas(pedidos.stream().map(p -> linhas(p, configuracao)).toList());
    }

    private List<String> linhas(Pedido pedido, ConfiguracaoLoja configuracao) {
        var endereco = pedido.getEnderecoEntrega();
        var linhas = new ArrayList<String>();
        linhas.add("AERIA - ETIQUETA DE ENVIO");
        linhas.add("Pedido " + PedidoResponseMapper.numero(pedido.getId()));
        linhas.add("");
        linhas.add("REMETENTE");
        linhas.add(configuracao.getRemetentePadrao());
        linhas.add("CEP de origem: " + configuracao.getCepOrigemFrete());
        linhas.add("");
        linhas.add("DESTINATÁRIO");
        linhas.add(endereco.getDestinatario());
        linhas.add(endereco.getLogradouro() + ", " + endereco.getNumero());
        if (endereco.getComplemento() != null && !endereco.getComplemento().isBlank())
            linhas.add(endereco.getComplemento());
        linhas.add(endereco.getBairro());
        linhas.add(endereco.getCidade() + " / " + endereco.getUf());
        linhas.add("CEP: " + endereco.getCep());
        linhas.add("");
        linhas.add("Projeto acadêmico - etiqueta local, sem postagem contratada.");
        return linhas;
    }
}
