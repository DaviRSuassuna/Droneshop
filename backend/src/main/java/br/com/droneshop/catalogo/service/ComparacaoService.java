package br.com.droneshop.catalogo.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import br.com.droneshop.catalogo.dto.ComparacaoDTO;
import br.com.droneshop.catalogo.model.ComponenteKit;
import br.com.droneshop.catalogo.model.Drone;
import br.com.droneshop.catalogo.model.GrupoProduto;
import br.com.droneshop.catalogo.model.Kit;
import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.model.StatusProduto;
import br.com.droneshop.catalogo.repository.ProdutoRepository;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

/** Comparação de 2 a 4 drones pelas specs fixas (#44, D14, D40). */
@ApplicationScoped
public class ComparacaoService {

    private record Campo(String campo, String rotulo, Function<Drone, Object> valor, boolean maiorMelhor) {
    }

    private static final List<Campo> CAMPOS = List.of(
            new Campo("autonomiaMinutos", "Autonomia (min)", Drone::getAutonomiaMinutos, true),
            new Campo("alcanceMetros", "Alcance (m)", Drone::getAlcanceMetros, true),
            new Campo("velocidadeMaximaKmh", "Velocidade máxima (km/h)", Drone::getVelocidadeMaximaKmh, true),
            new Campo("resolucaoFotoMp", "Resolução de foto (MP)", Drone::getResolucaoFotoMp, true),
            new Campo("capacidadeBateriaMah", "Bateria (mAh)", Drone::getCapacidadeBateriaMah, true),
            new Campo("pesoGramas", "Peso (g)", Drone::getPesoGramas, false),
            new Campo("resolucaoVideo", "Vídeo", Drone::getResolucaoVideo, false),
            new Campo("tipoSensor", "Sensor", Drone::getTipoSensor, false),
            new Campo("estabilizacao", "Estabilização", Drone::getEstabilizacao, false),
            new Campo("tempoCarregamentoMinutos", "Recarga (min)", Drone::getTempoCarregamentoMinutos, false),
            new Campo("dimensoesDobrado", "Dimensões dobrado", Drone::getDimensoesDobrado, false),
            new Campo("gpsIntegrado", "GPS integrado", Drone::isGpsIntegrado, false),
            new Campo("sensoresAnticolisao", "Sensores anticolisão", Drone::isSensoresAnticolisao, false),
            new Campo("classeAnac", "Classe ANAC", Drone::getClasseAnac, false));

    @Inject
    ProdutoRepository produtos;

    @Inject
    EntityManager em;

    @Transactional
    public ComparacaoDTO comparar(String grupo, String ids) {
        Map<Long, Boolean> escolhidos = new LinkedHashMap<>();
        if (grupo != null && !grupo.isBlank()) {
            GrupoProduto g = em.createQuery("select g from GrupoProduto g where g.slug = :s and g.ativo = true",
                    GrupoProduto.class).setParameter("s", grupo.trim()).getResultStream().findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado."));
            g.getProdutos().stream().filter(p -> p.getStatus() == StatusProduto.ATIVO)
                    .forEach(p -> escolhidos.put(p.getId(), true));
        }
        if (ids != null && !ids.isBlank())
            for (String s : ids.split(",")) {
                try {
                    escolhidos.putIfAbsent(Long.valueOf(s.trim()), false);
                } catch (NumberFormatException e) {
                    throw ValidationException.of("ids", "Id inválido: " + s.trim() + " (back)");
                }
            }
        if (escolhidos.size() < 2 || escolhidos.size() > 4)
            throw ValidationException.of("ids", "Compare de 2 a 4 drones (back)");

        List<ComparacaoDTO.Produto> lista = new ArrayList<>();
        List<Drone> drones = new ArrayList<>();
        for (var e : escolhidos.entrySet()) {
            Produto p = produtos.findById(e.getKey());
            if (p == null || p.getStatus() != StatusProduto.ATIVO)
                throw ValidationException.of("ids", "Produto " + e.getKey() + " inexistente ou fora da loja (back)");
            Drone d;
            List<String> extras = List.of();
            if (p instanceof Drone dr) {
                d = dr;
            } else if (p instanceof Kit k) {
                d = k.getComponentes().stream().map(ComponenteKit::getProduto).filter(Drone.class::isInstance)
                        .map(Drone.class::cast).findFirst()
                        .orElseThrow(() -> ValidationException.of("ids", "O kit " + p.getId() + " não tem drone (back)"));
                extras = k.getComponentes().stream().filter(c -> !(c.getProduto() instanceof Drone))
                        .map(c -> c.getQuantidade() + "x " + c.getProduto().getNome()).toList();
            } else {
                throw ValidationException.of("ids", "Só drones e kits com drone se comparam (back)");
            }
            drones.add(d);
            Object[] nota = nota(p.getId());
            lista.add(new ComparacaoDTO.Produto(p.getId(), p.getNome(), p.getSlug(), p.getPreco(),
                    (Double) nota[0], (Long) nota[1], e.getValue(), extras));
        }

        List<ComparacaoDTO.Spec> specs = new ArrayList<>();
        for (Campo c : CAMPOS) {
            List<Object> valores = drones.stream().map(c.valor()).toList();
            boolean diferente = valores.stream().distinct().count() > 1;
            Integer melhor = null;
            if (c.maiorMelhor() && diferente) {
                int max = -1;
                Integer maior = null;
                for (int i = 0; i < valores.size(); i++)
                    if (valores.get(i) instanceof Integer v && (maior == null || v > maior)) {
                        maior = v;
                        max = i;
                    }
                melhor = max >= 0 ? max : null;
            }
            specs.add(new ComparacaoDTO.Spec(c.campo(), c.rotulo(), new ArrayList<>(valores), diferente, melhor));
        }
        return new ComparacaoDTO(lista, specs);
    }

    /** Nota média e total de avaliações aprovadas (#39); sem avaliações, nota nula. */
    Object[] nota(Long idProduto) {
        return new Object[] { null, 0L };
    }
}
