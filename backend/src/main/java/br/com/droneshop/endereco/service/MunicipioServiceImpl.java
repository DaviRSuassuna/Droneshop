package br.com.droneshop.endereco.service;

import java.util.List;

import br.com.droneshop.endereco.model.Municipio;
import br.com.droneshop.endereco.repository.EstadoRepository;
import br.com.droneshop.endereco.repository.MunicipioRepository;
import br.com.droneshop.exception.ValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MunicipioServiceImpl implements MunicipioService {

    @Inject
    MunicipioRepository municipioRepository;

    @Inject
    EstadoRepository estadoRepository;

    // sao 5.571 municipios: a listagem exige o estado, para o select nao receber o pais inteiro
    @Override
    public List<Municipio> findByEstado(Long idEstado) {
        if (idEstado == null)
            throw ValidationException.of("idEstado", "Informe o estado.");
        if (estadoRepository.findById(idEstado) == null)
            throw ValidationException.of("idEstado", "Estado não encontrado.");
        return municipioRepository.findByEstado(idEstado);
    }

    @Override
    public Municipio findById(long id) {
        return municipioRepository.findById(id);
    }

}
