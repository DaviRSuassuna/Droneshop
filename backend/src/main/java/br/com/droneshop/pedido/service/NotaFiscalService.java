package br.com.droneshop.pedido.service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import br.com.droneshop.exception.ConflitoException;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.pedido.mapper.PedidoResponseMapper;
import br.com.droneshop.pedido.model.ItemPedido;
import br.com.droneshop.pedido.model.NotaFiscal;
import br.com.droneshop.pedido.model.Pedido;
import br.com.droneshop.pedido.model.StatusPedido;
import br.com.droneshop.pedido.model.TipoOrigemNota;
import br.com.droneshop.pedido.repository.NotaFiscalRepository;
import br.com.droneshop.pedido.repository.PedidoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * NF-e simulada (#35): número sequencial por série, chave de acesso de 44 dígitos com DV
 * módulo 11, XML e DANFE gerados na hora. Sem certificado, sem SEFAZ.
 */
@ApplicationScoped
public class NotaFiscalService {

    static final String SERIE = "1";
    // CNPJ fictício da loja (projeto de estudo); UF 17 = Tocantins
    static final String CNPJ_EMITENTE = "12345678000195";
    static final String UF = "17";

    private static final SecureRandom ALEATORIO = new SecureRandom();

    @Inject
    NotaFiscalRepository notas;

    @Inject
    PedidoRepository pedidos;

    public static int dv(String chave43) {
        int soma = 0, peso = 2;
        for (int i = chave43.length() - 1; i >= 0; i--) {
            soma += (chave43.charAt(i) - '0') * peso;
            peso = peso == 9 ? 2 : peso + 1;
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private Pedido pedido(Long id) {
        Pedido p = pedidos.findById(id);
        if (p == null)
            throw new ResourceNotFoundException("Pedido não encontrado.");
        return p;
    }

    @Transactional
    public NotaFiscal emitirVenda(Long idPedido) {
        Pedido p = pedido(idPedido);
        if (p.getStatus() == StatusPedido.PENDENTE || p.getStatus() == StatusPedido.CANCELADO)
            throw new ConflitoException("A nota de venda só sai depois do pagamento.");
        if (notas.existe(p.getId(), TipoOrigemNota.VENDA))
            throw new ConflitoException("A nota de venda deste pedido já foi emitida.");
        return emitir(p, TipoOrigemNota.VENDA, null);
    }

    /** Nota de devolução, gerada pelo fluxo de devolução (#37). */
    @Transactional
    public NotaFiscal emitirDevolucao(Pedido p, Long idDevolucao) {
        return emitir(p, TipoOrigemNota.DEVOLUCAO, idDevolucao);
    }

    private NotaFiscal emitir(Pedido p, TipoOrigemNota tipo, Long idDevolucao) {
        LocalDateTime agora = LocalDateTime.now();
        long numero = notas.ultimoNumero(SERIE) + 1;
        String chave43 = UF + agora.format(DateTimeFormatter.ofPattern("yyMM")) + CNPJ_EMITENTE + "55"
                + String.format("%03d", Integer.parseInt(SERIE)) + String.format("%09d", numero) + "1"
                + String.format("%08d", ALEATORIO.nextInt(100_000_000));
        NotaFiscal n = new NotaFiscal();
        n.setPedido(p);
        n.setTipoOrigem(tipo);
        n.setNumero(String.valueOf(numero));
        n.setSerie(SERIE);
        n.setChaveAcesso(chave43 + dv(chave43));
        n.setDataEmissao(agora);
        n.setIdDevolucao(idDevolucao);
        notas.persist(n);
        n.setXmlUrl("/notas-fiscais/" + n.getId() + "/xml");
        n.setDanfeUrl("/notas-fiscais/" + n.getId() + "/danfe");
        return n;
    }

    public NotaFiscal buscar(Long id) {
        NotaFiscal n = notas.findById(id);
        if (n == null)
            throw new ResourceNotFoundException("Nota fiscal não encontrada.");
        return n;
    }

    public List<NotaFiscal> doPedido(Long idPedido) {
        return notas.doPedido(pedido(idPedido).getId());
    }

    @Transactional
    public byte[] xml(Long id) {
        NotaFiscal n = buscar(id);
        Pedido p = n.getPedido();
        StringBuilder x = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        x.append("<!-- NF-e simulada: projeto de estudo, sem validade fiscal -->\n");
        x.append("<nfeProc><NFe><infNFe Id=\"NFe").append(n.getChaveAcesso()).append("\">");
        x.append("<ide><serie>").append(n.getSerie()).append("</serie><nNF>").append(n.getNumero())
                .append("</nNF><tpNF>").append(n.getTipoOrigem() == TipoOrigemNota.VENDA ? 1 : 0)
                .append("</tpNF><dhEmi>").append(n.getDataEmissao()).append("</dhEmi></ide>");
        x.append("<emit><CNPJ>").append(CNPJ_EMITENTE).append("</CNPJ><xNome>Aeria Drones</xNome></emit>");
        x.append("<dest><xNome>").append(escapar(p.getCliente().getNome())).append("</xNome></dest>");
        int i = 1;
        for (ItemPedido item : p.getItens()) {
            x.append("<det nItem=\"").append(i++).append("\"><prod><cProd>").append(escapar(item.getProduto().getSku()))
                    .append("</cProd><xProd>").append(escapar(item.getProduto().getNome())).append("</xProd><qCom>")
                    .append(item.getQuantidade()).append("</qCom><vUnCom>").append(item.getPrecoUnitarioCongelado())
                    .append("</vUnCom></prod></det>");
        }
        x.append("<total><vNF>").append(p.getValorTotal()).append("</vNF></total></infNFe></NFe></nfeProc>\n");
        return x.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional
    public byte[] danfe(Long id) {
        NotaFiscal n = buscar(id);
        Pedido p = n.getPedido();
        List<String> linhas = new ArrayList<>();
        linhas.add("DANFE simulado - sem validade fiscal");
        linhas.add("Aeria Drones - CNPJ " + CNPJ_EMITENTE);
        linhas.add("NF-e " + n.getNumero() + " serie " + n.getSerie() + " ("
                + (n.getTipoOrigem() == TipoOrigemNota.VENDA ? "venda" : "devolucao") + ")");
        linhas.add("Chave: " + n.getChaveAcesso());
        linhas.add("Pedido " + PedidoResponseMapper.numero(p.getId()) + " - " + p.getCliente().getNome());
        linhas.add("");
        for (ItemPedido item : p.getItens())
            linhas.add(item.getQuantidade() + " x " + item.getProduto().getNome() + "  R$ "
                    + item.getPrecoUnitarioCongelado());
        linhas.add("");
        linhas.add("Total: R$ " + p.getValorTotal());
        return PdfEtiqueta.gerar(linhas);
    }

    private static String escapar(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
