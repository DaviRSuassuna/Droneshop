package br.com.droneshop.catalogo.service;

import java.util.List;

import br.com.droneshop.catalogo.dto.FornecedorRequestDTO;
import br.com.droneshop.catalogo.model.Fornecedor;
import br.com.droneshop.catalogo.model.StatusFornecedor;
import br.com.droneshop.catalogo.repository.FornecedorRepository;
import br.com.droneshop.catalogo.repository.ProdutoRepository;
import br.com.droneshop.endereco.dto.EnderecoRequestDTO;
import br.com.droneshop.endereco.model.Endereco;
import br.com.droneshop.endereco.model.Municipio;
import br.com.droneshop.endereco.repository.MunicipioRepository;
import br.com.droneshop.exception.ConflitoException;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.util.Documentos;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class FornecedorServiceImpl implements FornecedorService {
    @Override
    public List<Fornecedor> findComFiltro(String busca, Long idStatus, String uf, int page, int pageSize) {
        return fornecedorRepository.findComFiltro(busca, idStatus, uf).page(page, pageSize).list();
    }
    @Override
    public long countComFiltro(String busca, Long idStatus, String uf) {
        return fornecedorRepository.findComFiltro(busca, idStatus, uf).count();
    }


    @Inject
    FornecedorRepository fornecedorRepository;

    @Inject
    MunicipioRepository municipioRepository;

    @Inject
    ProdutoRepository produtoRepository;

    @Override
    @Transactional
    public Fornecedor create(FornecedorRequestDTO dto) {
        StatusFornecedor status = buscarStatusOuFalhar(dto.idStatus());
        String cnpj = Documentos.somenteDigitos(dto.cnpj());
        validarCnpjUnico(cnpj, null);

        Fornecedor novoFornecedor = new Fornecedor();
        aplicarDados(novoFornecedor, dto, status, cnpj);
        fornecedorRepository.persist(novoFornecedor);
        return novoFornecedor;
    }

    @Override
    @Transactional
    public void update(long id, FornecedorRequestDTO dto) {
        Fornecedor edicaoFornecedor = buscarFornecedorOuFalhar(id);
        StatusFornecedor status = buscarStatusOuFalhar(dto.idStatus());
        String cnpj = Documentos.somenteDigitos(dto.cnpj());
        validarCnpjUnico(cnpj, id);

        aplicarDados(edicaoFornecedor, dto, status, cnpj);
    }

    @Override
    @Transactional
    public void delete(long id) {
        long emUso = produtoRepository.count("fornecedor.id", id);
        if (emUso > 0)
            throw new ConflitoException("Este fornecedor tem " + emUso + (emUso == 1 ? " produto" : " produtos")
                    + ". Inative o fornecedor em vez de excluir.");
        if (!fornecedorRepository.deleteById(id))
            throw new ResourceNotFoundException("Fornecedor não encontrado.");
    }

    @Override
    public Fornecedor findById(long id) {
        return buscarFornecedorOuFalhar(id);
    }

    @Override
    public Fornecedor findByCnpj(String cnpj) {
        Fornecedor fornecedor = fornecedorRepository.findByCnpj(Documentos.somenteDigitos(cnpj));
        if (fornecedor == null)
            throw new ResourceNotFoundException("Fornecedor não encontrado.");
        return fornecedor;
    }

    @Override
    public List<Fornecedor> findAll(int page, int pageSize) {
        return fornecedorRepository.findAll().page(page, pageSize).list();
    }

    @Override
    public List<Fornecedor> findByNome(String nome, int page, int pageSize) {
        return fornecedorRepository.findByNome(nome).page(page, pageSize).list();
    }

    @Override
    public long count() {
        return fornecedorRepository.findAll().count();
    }

    @Override
    public long count(String nome) {
        return fornecedorRepository.findByNome(nome).count();
    }

    // unicidade pelos digitos, para nao depender de como o usuario digitou
    private void validarCnpjUnico(String cnpj, Long id) {
        if (fornecedorRepository.findByCnpjExceptId(cnpj, id) != null)
            throw ValidationException.of("cnpj", "Já existe um fornecedor cadastrado com esse CNPJ.");
    }

    private void aplicarDados(Fornecedor destino, FornecedorRequestDTO origem, StatusFornecedor status, String cnpj) {
        destino.setRazaoSocial(origem.razaoSocial().trim());
        destino.setCnpj(cnpj);
        destino.setNomeContato(origem.nomeContato().trim());
        destino.setEmailContato(origem.emailContato().trim());
        destino.setTelefoneContato(Documentos.somenteDigitosOuNulo(origem.telefoneContato()));
        destino.setEndereco(montarEndereco(origem.endereco()));
        destino.setStatus(status);
    }

    private Endereco montarEndereco(EnderecoRequestDTO dto) {
        Endereco endereco = new Endereco();
        endereco.setCep(Documentos.somenteDigitos(dto.cep()));
        endereco.setLogradouro(dto.logradouro().trim());
        endereco.setNumero(dto.numero().trim());
        endereco.setComplemento((dto.complemento() == null || dto.complemento().isBlank()) ? null : dto.complemento().trim());
        endereco.setBairro(dto.bairro().trim());
        endereco.setMunicipio(buscarMunicipioOuFalhar(dto.idMunicipio()));
        return endereco;
    }

    private Municipio buscarMunicipioOuFalhar(Long idMunicipio) {
        Municipio municipio = municipioRepository.findById(idMunicipio);
        if (municipio == null)
            throw ValidationException.of("idMunicipio", "Município não encontrado.");
        return municipio;
    }

    private Fornecedor buscarFornecedorOuFalhar(long id) {
        Fornecedor fornecedor = fornecedorRepository.findById(id);
        if (fornecedor == null)
            throw new ResourceNotFoundException("Fornecedor não encontrado.");
        return fornecedor;
    }

    private StatusFornecedor buscarStatusOuFalhar(Long idStatus) {
        StatusFornecedor status = StatusFornecedor.valueOf(idStatus);
        if (status == null)
            throw ValidationException.of("idStatus", "Status não encontrado.");
        return status;
    }

}
