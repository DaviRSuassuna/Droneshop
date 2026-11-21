package br.com.droneshop.pedido.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import br.com.droneshop.administracao.model.ConfiguracaoLoja;
import br.com.droneshop.administracao.repository.ConfiguracaoLojaRepository;
import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.service.KitCalculo;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.pedido.client.FreteClient;
import br.com.droneshop.pedido.dto.CotacaoFreteResponseDTO;
import br.com.droneshop.pedido.dto.RegraFreteRequestDTO;
import br.com.droneshop.pedido.model.ModalidadeEntrega;
import br.com.droneshop.pedido.model.OrigemCalculoFrete;
import br.com.droneshop.pedido.model.RegraFrete;
import br.com.droneshop.pedido.repository.RegraFreteRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class FreteServiceImpl implements FreteService {

    private static final Logger LOG = Logger.getLogger(FreteServiceImpl.class);
    private static final String CEP_ORIGEM_PADRAO = "77001000";

    @Inject
    @RestClient
    FreteClient agregador;

    @Inject
    RegraFreteRepository regras;

    @Inject
    ConfiguracaoLojaRepository configuracao;

    public static String cepValido(String cep) {
        String d = cep == null ? "" : cep.replaceAll("\\D", "");
        if (d.length() != 8)
            throw ValidationException.of("cep", "Informe um CEP com 8 dígitos (back)");
        return d;
    }

    @Override
    @Transactional
    public List<CotacaoFreteResponseDTO> cotar(String cep, Map<Produto, Integer> itens) {
        String destino = cepValido(cep);
        List<CotacaoFreteResponseDTO> cotacoes = new ArrayList<>();
        List<CotacaoFreteResponseDTO> doAgregador = itens.isEmpty() ? List.of() : agregador(destino, itens);
        if (doAgregador.isEmpty()) {
            regra(ModalidadeEntrega.EXPRESSA).ifPresent(cotacoes::add);
            regra(ModalidadeEntrega.PADRAO).ifPresent(cotacoes::add);
        } else {
            cotacoes.addAll(doAgregador);
        }
        regra(ModalidadeEntrega.RETIRADA_SHOWROOM).ifPresent(cotacoes::add);
        return cotacoes;
    }

    private java.util.Optional<CotacaoFreteResponseDTO> regra(ModalidadeEntrega m) {
        RegraFrete r = regras.daModalidade(m);
        if (r == null)
            return java.util.Optional.empty();
        String transportadora = m == ModalidadeEntrega.RETIRADA_SHOWROOM ? "Showroom Aeria" : "Aeria";
        return java.util.Optional.of(new CotacaoFreteResponseDTO(m, OrigemCalculoFrete.REGRA_FRETE, transportadora,
                "REGRA-" + m.getNome(), r.getCustoFixo(), r.getPrazoDias()));
    }

    private String cepOrigem() {
        ConfiguracaoLoja c = configuracao.buscarUnica();
        String cep = c == null || c.getCepOrigemFrete() == null ? null : c.getCepOrigemFrete().replaceAll("\\D", "");
        return cep == null || cep.length() != 8 ? CEP_ORIGEM_PADRAO : cep;
    }

    // Pacote único: peso somado de todas as unidades, maior altura e largura, comprimentos somados (D16)
    static FreteClient.Pacote pacote(Map<Produto, Integer> itens) {
        BigDecimal peso = BigDecimal.ZERO;
        int altura = 1, largura = 1, comprimento = 0;
        for (Map.Entry<Produto, Integer> e : itens.entrySet()) {
            Produto p = e.getKey();
            int q = e.getValue();
            BigDecimal pp = KitCalculo.peso(p);
            peso = peso.add((pp == null ? BigDecimal.ONE : pp).multiply(BigDecimal.valueOf(q)));
            Integer a = KitCalculo.altura(p), l = KitCalculo.largura(p), c = KitCalculo.comprimento(p);
            altura = Math.max(altura, a == null ? 10 : a);
            largura = Math.max(largura, l == null ? 10 : l);
            comprimento += (c == null ? 10 : c) * q;
        }
        return new FreteClient.Pacote(peso.setScale(3, RoundingMode.HALF_UP), largura, altura, Math.max(comprimento, 1));
    }

    // O mais barato vira PADRAO e o mais rápido vira EXPRESSA; falha vira log de integração (RNF10)
    private List<CotacaoFreteResponseDTO> agregador(String destino, Map<Produto, Integer> itens) {
        FreteClient.Cotacao pedido = new FreteClient.Cotacao(new FreteClient.Cep(cepOrigem()),
                new FreteClient.Cep(destino), pacote(itens));
        try {
            LOG.infof("Cotação de frete no agregador: destino %s, pacote %s", destino, pedido.pacote());
            List<FreteClient.Servico> servicos = agregador.calcular(pedido).stream()
                    .filter(s -> s.error() == null && s.price() != null && s.deliveryTime() != null).toList();
            if (servicos.isEmpty()) {
                LOG.warnf("Agregador de frete sem serviços para o CEP %s", destino);
                return List.of();
            }
            FreteClient.Servico barato = servicos.stream().min(Comparator.comparing(FreteClient.Servico::price)).get();
            FreteClient.Servico rapido = servicos.stream()
                    .min(Comparator.comparing(FreteClient.Servico::deliveryTime)).get();
            return List.of(cotacao(ModalidadeEntrega.EXPRESSA, rapido), cotacao(ModalidadeEntrega.PADRAO, barato));
        } catch (RuntimeException e) {
            LOG.errorf("Falha de integração com o agregador de frete (CEP %s): %s", destino, e.getMessage());
            return List.of();
        }
    }

    private static CotacaoFreteResponseDTO cotacao(ModalidadeEntrega m, FreteClient.Servico s) {
        return new CotacaoFreteResponseDTO(m, OrigemCalculoFrete.INTEGRACAO_AGREGADOR,
                s.company() == null ? null : s.company().name(), s.name(),
                s.price().setScale(2, RoundingMode.HALF_UP), s.deliveryTime());
    }

    // ---------------------------------------------------------------- RegraFrete (admin)

    @Override
    @Transactional
    public RegraFrete criar(RegraFreteRequestDTO dto) {
        RegraFrete r = new RegraFrete();
        preencher(r, dto);
        regras.persist(r);
        return r;
    }

    @Override
    @Transactional
    public RegraFrete alterar(Long id, RegraFreteRequestDTO dto) {
        RegraFrete r = buscar(id);
        preencher(r, dto);
        return r;
    }

    @Override
    public RegraFrete buscar(Long id) {
        RegraFrete r = regras.findById(id);
        if (r == null)
            throw new ResourceNotFoundException("Regra de frete não encontrada.");
        return r;
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        regras.delete(buscar(id));
    }

    private static void preencher(RegraFrete r, RegraFreteRequestDTO dto) {
        ModalidadeEntrega m = ModalidadeEntrega.valueOf(dto.idModalidade());
        if (m == null)
            throw ValidationException.of("idModalidade", "Modalidade inválida (back)");
        r.setModalidade(m);
        r.setCustoFixo(dto.custoFixo());
        r.setPrazoDias(dto.prazoDias());
        r.setCepOrigem(dto.cepOrigem() == null || dto.cepOrigem().isBlank() ? null : cepValidoOrigem(dto.cepOrigem()));
    }

    private static String cepValidoOrigem(String cep) {
        String d = cep.replaceAll("\\D", "");
        if (d.length() != 8)
            throw ValidationException.of("cepOrigem", "Informe um CEP com 8 dígitos (back)");
        return d;
    }
}
