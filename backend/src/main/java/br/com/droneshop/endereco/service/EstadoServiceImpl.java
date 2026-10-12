package br.com.droneshop.endereco.service;

import java.util.List;

import br.com.droneshop.endereco.model.Estado;
import br.com.droneshop.endereco.repository.EstadoRepository;
import br.com.droneshop.exception.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EstadoServiceImpl implements EstadoService {

    @Inject
    EstadoRepository estadoRepository;

    @Override
    public List<Estado> findAll() {
        return estadoRepository.findAllOrdenados();
    }

    @Override
    public Estado findById(long id) {
        Estado estado = estadoRepository.findById(id);
        if (estado == null)
            throw new ResourceNotFoundException("Estado não encontrado.");
        return estado;
    }

}
