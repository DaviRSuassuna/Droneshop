package br.com.droneshop.catalogo.service;

import java.util.List;

import br.com.droneshop.catalogo.dto.FornecedorDTO;
import br.com.droneshop.catalogo.model.Fornecedor;
import br.com.droneshop.catalogo.model.StatusFornecedor;
import br.com.droneshop.catalogo.repository.FornecedorRepository;
import br.com.droneshop.exception.ValidationException;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class FornecedorServiceImpl implements FornecedorService {

    @Inject
    FornecedorRepository fornecedorRepository;

    @Override
    @Transactional
    public Fornecedor create(FornecedorDTO dto) {
        StatusFornecedor status = parseStatus(dto.getStatus());

        String cnpj = normalizarCnpj(dto.getCnpj());
        validarCnpjUnico(cnpj, null);

        Fornecedor novoFornecedor = new Fornecedor();
        novoFornecedor.setRazaoSocial(dto.getRazaoSocial().trim());
        novoFornecedor.setCnpj(cnpj);
        novoFornecedor.setNomeContato(dto.getNomeContato().trim());
        novoFornecedor.setEmailContato(dto.getEmailContato().trim());
        novoFornecedor.setTelefoneContato(valorOuNulo(dto.getTelefoneContato()));
        novoFornecedor.setStatus(status);

        // realizando inclusao
        fornecedorRepository.persist(novoFornecedor);

        return novoFornecedor;
    }

    @Override
    @Transactional
    public void update(long id, FornecedorDTO dto) {
        StatusFornecedor status = parseStatus(dto.getStatus());

        String cnpj = normalizarCnpj(dto.getCnpj());
        validarCnpjUnico(cnpj, id);

        Fornecedor edicaoFornecedor = fornecedorRepository.findById(id);

        edicaoFornecedor.setRazaoSocial(dto.getRazaoSocial().trim());
        edicaoFornecedor.setCnpj(cnpj);
        edicaoFornecedor.setNomeContato(dto.getNomeContato().trim());
        edicaoFornecedor.setEmailContato(dto.getEmailContato().trim());
        edicaoFornecedor.setTelefoneContato(valorOuNulo(dto.getTelefoneContato()));
        edicaoFornecedor.setStatus(status);
    }

    private StatusFornecedor parseStatus(String status) {
        try {
            return StatusFornecedor.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ValidationException.of("status", "Status inválido. Use ATIVO ou INATIVO.");
        }
    }

    private String valorOuNulo(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    private void validarCnpjUnico(String cnpj, Long id) {
        Fornecedor fornecedor = fornecedorRepository.findByCnpjExceptId(cnpj, id);
        if (fornecedor != null)
            throw ValidationException.of("cnpj", "Já existe um fornecedor cadastrado com esse CNPJ.");
    }

    // o @CNPJ do DTO aceita com ou sem mascara; aqui padroniza para 00.000.000/0000-00
    // para que a unicidade nao dependa de como o usuario digitou
    private String normalizarCnpj(String cnpj) {
        String digitos = cnpj.replaceAll("\\D", "");
        if (digitos.length() != 14)
            return cnpj.trim();
        return digitos.replaceFirst("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
    }

    @Override
    @Transactional
    public void delete(long id) {
        fornecedorRepository.deleteById(id);
    }

    @Override
    public Fornecedor findById(long id) {
        return fornecedorRepository.findById(id);
    }

    @Override
    public Fornecedor findByCnpj(String cnpj) {
        return fornecedorRepository.findByCnpj(normalizarCnpj(cnpj));
    }

    @Override
    public List<Fornecedor> findAll(Integer page, Integer pageSize) {
        PanacheQuery<Fornecedor> query = fornecedorRepository.findAll().page(page, pageSize);
        return query.list();
    }

    @Override
    public List<Fornecedor> findByNome(String nome, Integer page, Integer pageSize) {
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

}
