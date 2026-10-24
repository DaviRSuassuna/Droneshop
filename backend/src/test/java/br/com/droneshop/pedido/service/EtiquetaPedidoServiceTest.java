package br.com.droneshop.pedido.service;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.charset.Charset;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import br.com.droneshop.administracao.dto.ConfiguracaoLojaRequestDTO;
import br.com.droneshop.administracao.model.ConfiguracaoLoja;
import br.com.droneshop.administracao.service.ConfiguracaoLojaService;
import br.com.droneshop.exception.ConflitoException;
import br.com.droneshop.pedido.model.EnderecoPedido;
import br.com.droneshop.pedido.model.Pedido;

@Tag("issue-61")
class EtiquetaPedidoServiceTest {
    private static final Charset ANSI = Charset.forName("windows-1252");

    @Test
    void usaRemetenteConfiguradoEEnderecoCongelado() {
        var pedido = new Pedido();
        pedido.setId(7L);
        var endereco = new EnderecoPedido();
        endereco.setDestinatario("Cliente fictício");
        endereco.setLogradouro("Rua de teste");
        endereco.setNumero("10");
        endereco.setBairro("Centro");
        endereco.setCidade("Palmas");
        endereco.setUf("TO");
        endereco.setCep("77001002");
        pedido.setEnderecoEntrega(endereco);
        var service = service(pedido);
        String pdf = new String(service.gerar(7), ANSI);
        assertTrue(pdf.startsWith("%PDF-1.4"));
        assertTrue(pdf.contains("Cliente fictício"));
        assertTrue(pdf.contains("Rua de teste, 10"));
        assertTrue(pdf.contains("Remetente fictício"));
        assertTrue(pdf.contains("AER-000007"));
    }

    @Test
    void semEnderecoRespondeConflito() {
        assertThrows(ConflitoException.class, () -> service(new Pedido()).gerar(7));
    }

    @Test
    void pdfEscapaTextoEApontaXrefParaOsObjetosCorretos() {
        String pdf = new String(PdfEtiqueta.gerar(List.of("João (teste) \\ exemplo\nlinha")), ANSI);
        assertTrue(pdf.contains("João \\(teste\\) \\\\ exemplo linha"));
        int inicio = Integer.parseInt(pdf.substring(pdf.lastIndexOf("startxref\n") + 10).split("\n")[0]);
        assertTrue(pdf.substring(inicio).startsWith("xref\n0 6\n"));
        String[] offsets = pdf.substring(inicio).split("\n");
        for (int i = 1; i <= 5; i++) {
            int offset = Integer.parseInt(offsets[i + 2].substring(0, 10));
            assertTrue(pdf.substring(offset).startsWith(i + " 0 obj"));
        }
    }

    private EtiquetaPedidoServiceImpl service(Pedido pedido) {
        var service = new EtiquetaPedidoServiceImpl();
        service.pedidoService = new PedidoServiceImpl() {
            @Override public Pedido findById(long id) { return pedido; }
        };
        service.configuracaoService = new ConfiguracaoLojaService() {
            @Override public ConfiguracaoLoja buscar() {
                var c = new ConfiguracaoLoja();
                c.setRemetentePadrao("Remetente fictício");
                c.setCepOrigemFrete("77001000");
                return c;
            }
            @Override public void atualizar(ConfiguracaoLojaRequestDTO dto) { throw new UnsupportedOperationException(); }
        };
        return service;
    }
}
