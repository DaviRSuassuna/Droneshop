package br.com.droneshop.identidade.service;

import java.util.List;

import br.com.droneshop.endereco.model.Endereco;
import br.com.droneshop.endereco.model.Municipio;
import br.com.droneshop.endereco.repository.MunicipioRepository;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.identidade.dto.EnderecoClienteRequestDTO;
import br.com.droneshop.identidade.model.EnderecoCliente;
import br.com.droneshop.identidade.model.PerfilCliente;
import br.com.droneshop.identidade.repository.EnderecoClienteRepository;
import br.com.droneshop.util.Documentos;
import br.com.droneshop.util.Textos;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class EnderecoClienteServiceImpl implements EnderecoClienteService {

    @Inject
    EnderecoClienteRepository enderecos;

    @Inject
    MunicipioRepository municipios;

    @Inject
    ClienteService clientes;

    @Override
    public List<EnderecoCliente> listar(Long idUsuario) {
        return enderecos.doPerfil(clientes.perfilDoUsuario(idUsuario));
    }

    @Override
    @Transactional
    public EnderecoCliente criar(Long idUsuario, EnderecoClienteRequestDTO dto) {
        PerfilCliente perfil = clientes.perfilDoUsuario(idUsuario);
        Endereco endereco = montarEndereco(dto);
        // o primeiro endereço é sempre o principal
        boolean principal = dto.principal() || enderecos.contarDoPerfil(perfil) == 0;
        if (principal)
            enderecos.desmarcarPrincipal(perfil);

        EnderecoCliente e = new EnderecoCliente();
        e.setPerfilCliente(perfil);
        e.setApelido(Textos.nuloSeVazio(dto.apelido()));
        e.setEndereco(endereco);
        e.setPrincipal(principal);
        enderecos.persist(e);
        return e;
    }

    @Override
    @Transactional
    public void alterar(Long idUsuario, Long id, EnderecoClienteRequestDTO dto) {
        PerfilCliente perfil = clientes.perfilDoUsuario(idUsuario);
        EnderecoCliente e = buscarOuFalhar(id, perfil);
        e.setApelido(Textos.nuloSeVazio(dto.apelido()));
        e.setEndereco(montarEndereco(dto));
        // desmarcar o principal pela edição não é possível: a vez só passa marcando outro
        if (dto.principal() && !e.isPrincipal()) {
            enderecos.desmarcarPrincipal(perfil);
            e.setPrincipal(true);
        }
    }

    @Override
    @Transactional
    public void excluir(Long idUsuario, Long id) {
        PerfilCliente perfil = clientes.perfilDoUsuario(idUsuario);
        EnderecoCliente e = buscarOuFalhar(id, perfil);
        boolean eraPrincipal = e.isPrincipal();
        enderecos.delete(e);
        enderecos.flush();
        if (eraPrincipal) {
            EnderecoCliente maisAntigo = enderecos.maisAntigo(perfil);
            if (maisAntigo != null)
                maisAntigo.setPrincipal(true);
        }
    }

    @Override
    @Transactional
    public void marcarPrincipal(Long idUsuario, Long id) {
        PerfilCliente perfil = clientes.perfilDoUsuario(idUsuario);
        EnderecoCliente e = buscarOuFalhar(id, perfil);
        if (e.isPrincipal())
            return;
        enderecos.desmarcarPrincipal(perfil);
        e.setPrincipal(true);
    }

    private EnderecoCliente buscarOuFalhar(Long id, PerfilCliente perfil) {
        EnderecoCliente e = enderecos.doPerfil(id, perfil);
        if (e == null)
            throw new ResourceNotFoundException("Endereço não encontrado.");
        return e;
    }

    private Endereco montarEndereco(EnderecoClienteRequestDTO dto) {
        Municipio municipio = municipios.findById(dto.idMunicipio());
        if (municipio == null)
            throw ValidationException.of("idMunicipio", "Município não encontrado.");
        Endereco endereco = new Endereco();
        endereco.setCep(Documentos.somenteDigitos(dto.cep()));
        endereco.setLogradouro(dto.logradouro().trim());
        endereco.setNumero(dto.numero().trim());
        endereco.setComplemento(Textos.nuloSeVazio(dto.complemento()));
        endereco.setBairro(dto.bairro().trim());
        endereco.setMunicipio(municipio);
        return endereco;
    }
}
