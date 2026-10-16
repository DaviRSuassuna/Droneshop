package br.com.droneshop.administracao.service;

import br.com.droneshop.administracao.dto.ConfiguracaoLojaRequestDTO;
import br.com.droneshop.administracao.model.ConfiguracaoLoja;
import br.com.droneshop.administracao.repository.ConfiguracaoLojaRepository;
import br.com.droneshop.exception.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ConfiguracaoLojaServiceImpl implements ConfiguracaoLojaService {

    @Inject
    ConfiguracaoLojaRepository repository;

    @Override
    public ConfiguracaoLoja buscar() {
        return buscarOuFalhar();
    }

    @Override
    @Transactional
    public void atualizar(ConfiguracaoLojaRequestDTO dto) {
        ConfiguracaoLoja configuracao = buscarOuFalhar();
        configuracao.setDadosFiscais(dto.dadosFiscais().trim());
        configuracao.setCepOrigemFrete(dto.cepOrigemFrete());
        configuracao.setRemetentePadrao(dto.remetentePadrao().trim());
        configuracao.setPrazoArrependimentoDias(dto.prazoArrependimentoDias());
        configuracao.setPrazoGarantiaDias(dto.prazoGarantiaDias());
        configuracao.setPrazoAnaliseDevolucaoDiasUteis(dto.prazoAnaliseDevolucaoDiasUteis());
        configuracao.setFreteReversoPorContaLoja(dto.freteReversoPorContaLoja());
    }

    private ConfiguracaoLoja buscarOuFalhar() {
        ConfiguracaoLoja configuracao = repository.buscarUnica();
        if (configuracao == null)
            throw new ResourceNotFoundException("Configuração da loja não encontrada.");
        return configuracao;
    }
}
