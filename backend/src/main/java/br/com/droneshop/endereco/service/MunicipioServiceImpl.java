package br.com.droneshop.endereco.service;

import java.util.List;

import br.com.droneshop.endereco.model.Municipio;
import br.com.droneshop.endereco.repository.EstadoRepository;
import br.com.droneshop.endereco.repository.MunicipioRepository;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MunicipioServiceImpl implements MunicipioService {

    @Inject
    MunicipioRepository municipioRepository;

    @Inject
    EstadoRepository estadoRepository;

    @Override
    public Municipio findById(long id) {
        Municipio municipio = municipioRepository.findById(id);
        if (municipio == null)
            throw new ResourceNotFoundException("Município não encontrado.");
        return municipio;
    }

    @Override
    public List<Municipio> findByEstado(Long idEstado, int page, int pageSize) {
        validarEstado(idEstado);
        return municipioRepository.findByEstado(idEstado).page(page, pageSize).list();
    }

    @Override
    public List<Municipio> findByNome(String nome, int page, int pageSize) {
        return municipioRepository.findByNome(nome).page(page, pageSize).list();
    }

    @Override
    public long count(Long idEstado) {
        validarEstado(idEstado);
        return municipioRepository.findByEstado(idEstado).count();
    }

    @Override
    public long count(String nome) {
        return municipioRepository.findByNome(nome).count();
    }

    private void validarEstado(Long idEstado) {
        if (idEstado == null)
            throw ValidationException.of("idEstado", "Informe o estado.");
        if (estadoRepository.findById(idEstado) == null)
            throw ValidationException.of("idEstado", "Estado não encontrado.");
    }

}
