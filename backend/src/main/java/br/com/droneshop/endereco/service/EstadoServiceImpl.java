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
    public Estado findById(long id) {
        Estado estado = estadoRepository.findById(id);
        if (estado == null)
            throw new ResourceNotFoundException("Estado não encontrado.");
        return estado;
    }

    @Override
    public Estado findBySigla(String sigla) {
        Estado estado = estadoRepository.findBySigla(sigla);
        if (estado == null)
            throw new ResourceNotFoundException("Estado não encontrado.");
        return estado;
    }

    @Override
    public List<Estado> findAll(int page, int pageSize) {
        return estadoRepository.findAll().page(page, pageSize).list();
    }

    @Override
    public List<Estado> findByNome(String nome, int page, int pageSize) {
        return estadoRepository.findByNome(nome).page(page, pageSize).list();
    }

    @Override
    public long count() {
        return estadoRepository.findAll().count();
    }

    @Override
    public long count(String nome) {
        return estadoRepository.findByNome(nome).count();
    }

}
