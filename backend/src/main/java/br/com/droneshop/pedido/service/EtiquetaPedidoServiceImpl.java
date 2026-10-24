package br.com.droneshop.pedido.service;

import java.util.ArrayList;
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
        var endereco = pedido.getEnderecoEntrega();
        if (endereco == null) throw new ConflitoException("O pedido não tem endereço de entrega para gerar a etiqueta.");
        var configuracao = configuracaoService.buscar();
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
        return PdfEtiqueta.gerar(linhas);
    }
}
