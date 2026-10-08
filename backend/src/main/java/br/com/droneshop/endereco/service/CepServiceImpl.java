package br.com.droneshop.endereco.service;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import br.com.droneshop.endereco.client.ViaCepClient;
import br.com.droneshop.endereco.client.ViaCepResponse;
import br.com.droneshop.endereco.dto.CepResponseDTO;
import br.com.droneshop.endereco.dto.MunicipioResponseDTO;
import br.com.droneshop.endereco.model.Municipio;
import br.com.droneshop.endereco.repository.MunicipioRepository;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ServicoExternoIndisponivelException;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.util.Documentos;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;

// A consulta de CEP so SUGERE o endereco: o que vale e o que for salvo no cadastro.
// Por isso nenhuma falha aqui impede o cadastro manual (404/503 sao tratados no front).
@ApplicationScoped
public class CepServiceImpl implements CepService {

    private static final Logger LOG = Logger.getLogger(CepServiceImpl.class);

    @Inject
    @RestClient
    ViaCepClient viaCepClient;

    @Inject
    MunicipioRepository municipioRepository;

    @Override
    public CepResponseDTO buscar(String cep) {
        String digitos = Documentos.somenteDigitos(cep);
        if (digitos == null || digitos.length() != 8)
            throw ValidationException.of("cep", "CEP inválido. Informe os 8 dígitos (back).");

        ViaCepResponse resposta = consultarViaCep(digitos);
        if (resposta == null || Boolean.TRUE.equals(resposta.erro()))
            throw new ResourceNotFoundException("CEP não encontrado. Confira o número ou preencha o endereço manualmente.");

        Municipio municipio = resolverMunicipio(resposta);

        return new CepResponseDTO(
            digitos,
            vazioParaNulo(resposta.logradouro()),
            vazioParaNulo(resposta.complemento()),
            vazioParaNulo(resposta.bairro()),
            resposta.localidade(),
            resposta.uf(),
            MunicipioResponseDTO.valueOf(municipio));
    }

    private ViaCepResponse consultarViaCep(String cep) {
        try {
            return viaCepClient.buscar(cep);
        } catch (ProcessingException | WebApplicationException e) {
            // timeout, sem rede ou ViaCEP respondendo erro HTTP
            LOG.warnf("Falha ao consultar o ViaCEP para o CEP %s: %s", cep, e.getMessage());
            throw new ServicoExternoIndisponivelException(
                "Consulta de CEP indisponível no momento. Preencha o endereço manualmente.", e);
        }
    }

    // 1) pelo codigo IBGE (exato); 2) plano B: nome + UF. Se nada bater, devolve null
    // e o front pede para escolher Estado e Municipio nos selects.
    private Municipio resolverMunicipio(ViaCepResponse resposta) {
        if (resposta.ibge() != null && resposta.ibge().matches("\\d{7}")) {
            Municipio municipio = municipioRepository.findByCodigoIbge(Integer.valueOf(resposta.ibge()));
            if (municipio != null)
                return municipio;
        }
        if (resposta.localidade() != null && resposta.uf() != null)
            return municipioRepository.findByNomeAndSiglaEstado(resposta.localidade(), resposta.uf());
        return null;
    }

    private String vazioParaNulo(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

}
