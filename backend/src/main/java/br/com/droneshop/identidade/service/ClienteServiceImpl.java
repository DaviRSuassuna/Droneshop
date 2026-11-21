package br.com.droneshop.identidade.service;

import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.identidade.dto.ContaRequestDTO;
import br.com.droneshop.identidade.dto.PerfilRequestDTO;
import br.com.droneshop.identidade.dto.SenhaRequestDTO;
import br.com.droneshop.identidade.model.PerfilCliente;
import br.com.droneshop.identidade.model.TipoPessoa;
import br.com.droneshop.identidade.model.Usuario;
import br.com.droneshop.identidade.repository.PerfilClienteRepository;
import br.com.droneshop.identidade.repository.UsuarioRepository;
import br.com.droneshop.util.Documentos;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;

@ApplicationScoped
public class ClienteServiceImpl implements ClienteService {

    @Inject
    LgpdService lgpd;

    @Inject
    UsuarioRepository usuarios;

    @Inject
    PerfilClienteRepository perfis;

    @Override
    @Transactional
    public PerfilCliente criarConta(ContaRequestDTO dto) {
        TipoPessoa tipo = TipoPessoa.valueOf(dto.idTipoPessoa());
        if (tipo == null)
            throw ValidationException.of("idTipoPessoa", "Tipo de pessoa não encontrado.");

        String documento = Documentos.somenteDigitos(dto.documento());
        boolean valido = (tipo == TipoPessoa.PF) ? Documentos.cpfValido(documento) : Documentos.cnpjValido(documento);
        if (!valido)
            throw ValidationException.of("documento", (tipo == TipoPessoa.PF ? "CPF" : "CNPJ") + " inválido.");
        if (perfis.documentoEmUso(documento))
            throw ValidationException.of("documento", "Já existe uma conta com esse documento.");
        if (usuarios.porEmail(dto.email()) != null)
            throw ValidationException.of("email", "Já existe uma conta com esse e-mail.");
        lgpd.conferirObrigatorios(dto.consentimentos());

        Usuario usuario = new Usuario();
        usuario.setNome(dto.nome().trim());
        usuario.setEmail(dto.email());
        usuario.setSenhaHash(BcryptUtil.bcryptHash(dto.senha()));
        usuario.setAtivo(true);
        usuarios.persist(usuario);

        PerfilCliente perfil = new PerfilCliente();
        perfil.setUsuario(usuario);
        perfil.setTipoPessoa(tipo);
        perfil.setNome(dto.nome().trim());
        perfil.setDocumento(documento);
        perfil.setTelefone(Documentos.somenteDigitosOuNulo(dto.telefone()));
        perfis.persist(perfil);
        usuario.setPerfilCliente(perfil);
        lgpd.registrar(usuario, dto.consentimentos());
        return perfil;
    }

    @Override
    public PerfilCliente perfilDoUsuario(Long idUsuario) {
        PerfilCliente perfil = perfis.porUsuario(idUsuario);
        if (perfil == null)
            throw new ForbiddenException("Usuário sem perfil de cliente.");
        return perfil;
    }

    @Override
    @Transactional
    public void alterarPerfil(Long idUsuario, PerfilRequestDTO dto) {
        PerfilCliente perfil = perfilDoUsuario(idUsuario);
        perfil.setNome(dto.nome().trim());
        perfil.setTelefone(Documentos.somenteDigitosOuNulo(dto.telefone()));
        perfil.getUsuario().setNome(dto.nome().trim());
    }

    @Override
    @Transactional
    public void trocarSenha(Long idUsuario, SenhaRequestDTO dto) {
        Usuario usuario = perfilDoUsuario(idUsuario).getUsuario();
        if (usuario.getSenhaHash() == null || !BcryptUtil.matches(dto.senhaAtual(), usuario.getSenhaHash()))
            throw ValidationException.of("senhaAtual", "Senha atual incorreta.");
        usuario.setSenhaHash(BcryptUtil.bcryptHash(dto.senhaNova()));
    }
}
