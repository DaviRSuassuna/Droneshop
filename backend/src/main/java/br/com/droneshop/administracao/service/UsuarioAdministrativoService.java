package br.com.droneshop.administracao.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import br.com.droneshop.administracao.dto.PermissaoItemRequestDTO;
import br.com.droneshop.administracao.dto.PermissaoItemResponseDTO;
import br.com.droneshop.administracao.dto.UsuarioAdministrativoRequestDTO;
import br.com.droneshop.administracao.dto.UsuarioAdministrativoResponseDTO;
import br.com.droneshop.administracao.model.ModuloSistema;
import br.com.droneshop.administracao.model.PapelAdministrativo;
import br.com.droneshop.administracao.model.PerfilAdministrativo;
import br.com.droneshop.administracao.model.PermissaoModulo;
import br.com.droneshop.administracao.repository.PerfilAdministrativoRepository;
import br.com.droneshop.dto.PageResponse;
import br.com.droneshop.exception.ConflitoException;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.identidade.model.Usuario;
import br.com.droneshop.identidade.repository.UsuarioRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class UsuarioAdministrativoService {

    private static final Pattern EMAIL_VALIDO = Pattern.compile(
            "^[A-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?(?:\\.[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?)+$",
            Pattern.CASE_INSENSITIVE);

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    PerfilAdministrativoRepository perfilAdministrativoRepository;

    public List<PermissaoItemResponseDTO> permissoesPadraoDoPapel(Long idPapel) {
        PapelAdministrativo papel = PapelAdministrativo.valueOf(idPapel);
        if (papel == null) {
            throw new ResourceNotFoundException("Papel não encontrado.");
        }

        List<PermissaoItemResponseDTO> lista = new ArrayList<>();
        for (ModuloSistema modulo : ModuloSistema.values()) {
            boolean podeVer = true;
            boolean podeEditar = switch (papel) {
                case ADMIN -> true;
                case OPERADOR -> modulo == ModuloSistema.ESTOQUE
                        || modulo == ModuloSistema.PEDIDO
                        || modulo == ModuloSistema.DEVOLUCAO;
                case CATALOGO -> modulo == ModuloSistema.CATALOGO
                        || modulo == ModuloSistema.ESTOQUE;
                case ATENDIMENTO -> modulo == ModuloSistema.ATENDIMENTO
                        || modulo == ModuloSistema.DEVOLUCAO;
                case SOMENTE_LEITURA -> false;
            };
            boolean podeExcluir = switch (papel) {
                case ADMIN -> true;
                case CATALOGO -> modulo == ModuloSistema.CATALOGO;
                default -> false;
            };
            lista.add(new PermissaoItemResponseDTO(modulo, podeVer, podeEditar, podeExcluir));
        }
        return lista;
    }

    @Transactional
    public UsuarioAdministrativoResponseDTO criar(UsuarioAdministrativoRequestDTO dto) {
        if (dto.idPapel() == null || PapelAdministrativo.valueOf(dto.idPapel()) == null) {
            throw ValidationException.of("idPapel", "Papel administrativo inválido.");
        }
        PapelAdministrativo papel = PapelAdministrativo.valueOf(dto.idPapel());

        if (!emailValido(dto.email())) {
            throw ValidationException.of("email", "E-mail inválido.");
        }
        String emailNorm = dto.email().trim().toLowerCase(Locale.ROOT);
        Usuario existente = usuarioRepository.porEmail(emailNorm);
        if (existente != null && existente.getPerfilAdministrativo() != null) {
            throw ValidationException.of("email", "Já existe um usuário administrativo com este e-mail.");
        }

        if (existente == null) {
            if (dto.senha() == null || dto.senha().length() < 8) {
                throw ValidationException.of("senha", "A senha deve ter no mínimo 8 caracteres.");
            }
        }

        String nome = dto.nome() != null ? dto.nome().trim() : (existente != null ? existente.getNome() : "");
        if (nome.isBlank()) {
            throw ValidationException.of("nome", "O nome é obrigatório.");
        }
        Map<Long, PermissaoItemRequestDTO> overrides = mapearOverrides(dto.permissoes());

        Usuario usuario;
        if (existente == null) {
            usuario = new Usuario();
            usuario.setNome(nome);
            usuario.setEmail(emailNorm);
            usuario.setSenhaHash(BcryptUtil.bcryptHash(dto.senha()));
            usuario.setEmailVerificado(true);
            usuario.setAtivo(dto.ativo() == null || dto.ativo());
        } else {
            usuario = existente;
            if (dto.ativo() != null) {
                usuario.setAtivo(dto.ativo());
            }
        }

        PerfilAdministrativo perfil = new PerfilAdministrativo();
        perfil.setPapel(papel);
        perfil.setUsuario(usuario);

        List<PermissaoItemResponseDTO> padroes = permissoesPadraoDoPapel(papel.getId());
        List<PermissaoModulo> listaPermissoes = new ArrayList<>();
        for (PermissaoItemResponseDTO p : padroes) {
            PermissaoItemRequestDTO ov = overrides.get(p.modulo().getId());
            boolean ver = ov != null && ov.podeVer() != null ? ov.podeVer() : p.podeVer();
            boolean editar = ov != null && ov.podeEditar() != null ? ov.podeEditar() : p.podeEditar();
            boolean excluir = ov != null && ov.podeExcluir() != null ? ov.podeExcluir() : p.podeExcluir();

            PermissaoModulo pm = new PermissaoModulo(perfil, p.modulo(), ver, editar, excluir);
            listaPermissoes.add(pm);
        }
        perfil.setPermissoes(listaPermissoes);
        usuario.setPerfilAdministrativo(perfil);

        if (existente == null) {
            usuarioRepository.persist(usuario);
        } else {
            perfilAdministrativoRepository.persist(perfil);
        }

        return toResponseDTO(usuario);
    }

    public UsuarioAdministrativoResponseDTO buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id);
        if (usuario == null || usuario.getPerfilAdministrativo() == null) {
            throw new ResourceNotFoundException("Usuário não encontrado.");
        }
        return toResponseDTO(usuario);
    }

    public PageResponse<UsuarioAdministrativoResponseDTO> listar(
            String nome, Long idPapel, Boolean ativo, int page, int pageSize) {
        if (page < 0) throw ValidationException.of("page", "A página não pode ser negativa.");
        if (pageSize < 1) throw ValidationException.of("pageSize", "O tamanho da página deve ser positivo.");

        StringBuilder hql = new StringBuilder("from Usuario u where u.perfilAdministrativo is not null");
        Map<String, Object> params = new HashMap<>();

        if (nome != null && !nome.isBlank()) {
            hql.append(" and lower(u.nome) like :nome");
            params.put("nome", "%" + nome.trim().toLowerCase(Locale.ROOT) + "%");
        }
        if (idPapel != null) {
            PapelAdministrativo papel = PapelAdministrativo.valueOf(idPapel);
            if (papel == null) throw ValidationException.of("idPapel", "Papel administrativo inválido.");
            hql.append(" and u.perfilAdministrativo.papel = :papel");
            params.put("papel", papel);
        }
        if (ativo != null) {
            hql.append(" and u.ativo = :ativo");
            params.put("ativo", ativo);
        }
        hql.append(" order by u.id asc");

        var query = usuarioRepository.find(hql.toString(), params);
        long totalItems = query.count();
        List<Usuario> list = query.page(page, pageSize).list();

        return PageResponse.of(list, page, pageSize, totalItems, this::toResponseDTO);
    }

    @Transactional
    public void atualizar(Long id, UsuarioAdministrativoRequestDTO dto, Long currentUserId) {
        Usuario usuario = usuarioRepository.findById(id);
        if (usuario == null || usuario.getPerfilAdministrativo() == null) {
            throw new ResourceNotFoundException("Usuário não encontrado.");
        }

        if (currentUserId != null && currentUserId.equals(id)) {
            if (dto.ativo() != null && !dto.ativo()) {
                throw new ConflitoException("Ninguém desativa nem rebaixa o próprio usuário.");
            }
            if (dto.idPapel() != null) {
                PapelAdministrativo novoPapel = PapelAdministrativo.valueOf(dto.idPapel());
                if (novoPapel != null && !novoPapel.equals(usuario.getPerfilAdministrativo().getPapel())) {
                    throw new ConflitoException("Ninguém desativa nem rebaixa o próprio usuário.");
                }
            }
            if (removePermissao(usuario.getPerfilAdministrativo(), mapearOverrides(dto.permissoes()))) {
                throw new ConflitoException("Ninguém desativa nem rebaixa o próprio usuário.");
            }
        }

        if (dto.nome() != null) {
            if (dto.nome().isBlank()) throw ValidationException.of("nome", "O nome é obrigatório.");
            usuario.setNome(dto.nome().trim());
        }
        if (dto.email() != null) {
            if (!emailValido(dto.email())) throw ValidationException.of("email", "E-mail inválido.");
            String emailNorm = dto.email().trim().toLowerCase(Locale.ROOT);
            Usuario outroUsuario = usuarioRepository.porEmail(emailNorm);
            if (outroUsuario != null && !outroUsuario.getId().equals(usuario.getId())) {
                throw ValidationException.of("email", "Já existe um usuário com este e-mail.");
            }
            usuario.setEmail(emailNorm);
        }
        if (dto.ativo() != null) {
            usuario.setAtivo(dto.ativo());
        }
        if (dto.senha() != null && !dto.senha().isBlank()) {
            if (dto.senha().length() < 8) {
                throw ValidationException.of("senha", "A senha deve ter no mínimo 8 caracteres.");
            }
            usuario.setSenhaHash(BcryptUtil.bcryptHash(dto.senha()));
        }
        boolean papelAlterado = false;
        if (dto.idPapel() != null) {
            PapelAdministrativo novoPapel = PapelAdministrativo.valueOf(dto.idPapel());
            if (novoPapel == null) {
                throw ValidationException.of("idPapel", "Papel administrativo inválido.");
            }
            papelAlterado = !novoPapel.equals(usuario.getPerfilAdministrativo().getPapel());
            usuario.getPerfilAdministrativo().setPapel(novoPapel);
        }

        PerfilAdministrativo perfil = usuario.getPerfilAdministrativo();
        if (papelAlterado) aplicarPadroes(perfil, perfil.getPapel());
        if (dto.permissoes() != null) {
            aplicarOverrides(perfil, mapearOverrides(dto.permissoes()));
        }
    }

    private static boolean emailValido(String email) {
        return email != null && EMAIL_VALIDO.matcher(email.trim()).matches();
    }

    private Map<Long, PermissaoItemRequestDTO> mapearOverrides(List<PermissaoItemRequestDTO> permissoes) {
        Map<Long, PermissaoItemRequestDTO> overrides = new HashMap<>();
        if (permissoes == null) return overrides;
        for (PermissaoItemRequestDTO permissao : permissoes) {
            if (permissao == null || permissao.idModulo() == null
                    || ModuloSistema.valueOf(permissao.idModulo()) == null) {
                throw ValidationException.of("idModulo", "Módulo de permissão inválido.");
            }
            if (overrides.putIfAbsent(permissao.idModulo(), permissao) != null) {
                throw ValidationException.of("idModulo", "O módulo não pode ser informado mais de uma vez.");
            }
        }
        return overrides;
    }

    // rebaixar também é tirar do próprio perfil uma permissão que ele já tem
    private static boolean removePermissao(PerfilAdministrativo perfil, Map<Long, PermissaoItemRequestDTO> overrides) {
        for (PermissaoModulo atual : perfil.getPermissoes()) {
            PermissaoItemRequestDTO novo = overrides.get(atual.getModulo().getId());
            if (novo == null) continue;
            if ((atual.isPodeVer() && Boolean.FALSE.equals(novo.podeVer()))
                    || (atual.isPodeEditar() && Boolean.FALSE.equals(novo.podeEditar()))
                    || (atual.isPodeExcluir() && Boolean.FALSE.equals(novo.podeExcluir()))) {
                return true;
            }
        }
        return false;
    }

    private void aplicarPadroes(PerfilAdministrativo perfil, PapelAdministrativo papel) {
        Map<Long, PermissaoModulo> existentes = new HashMap<>();
        for (PermissaoModulo permissao : perfil.getPermissoes()) {
            existentes.put(permissao.getModulo().getId(), permissao);
        }
        for (PermissaoItemResponseDTO padrao : permissoesPadraoDoPapel(papel.getId())) {
            PermissaoModulo permissao = existentes.get(padrao.modulo().getId());
            if (permissao == null) {
                permissao = new PermissaoModulo(perfil, padrao.modulo(), padrao.podeVer(),
                        padrao.podeEditar(), padrao.podeExcluir());
                perfil.getPermissoes().add(permissao);
            } else {
                permissao.setPodeVer(padrao.podeVer());
                permissao.setPodeEditar(padrao.podeEditar());
                permissao.setPodeExcluir(padrao.podeExcluir());
            }
        }
    }

    private void aplicarOverrides(PerfilAdministrativo perfil, Map<Long, PermissaoItemRequestDTO> overrides) {
        for (PermissaoModulo permissao : perfil.getPermissoes()) {
            PermissaoItemRequestDTO override = overrides.get(permissao.getModulo().getId());
            if (override == null) continue;
            if (override.podeVer() != null) permissao.setPodeVer(override.podeVer());
            if (override.podeEditar() != null) permissao.setPodeEditar(override.podeEditar());
            if (override.podeExcluir() != null) permissao.setPodeExcluir(override.podeExcluir());
        }
    }

    public UsuarioAdministrativoResponseDTO toResponseDTO(Usuario u) {
        PerfilAdministrativo p = u.getPerfilAdministrativo();
        List<PermissaoItemResponseDTO> permissoes = new ArrayList<>();
        if (p != null && p.getPermissoes() != null) {
            for (PermissaoModulo pm : p.getPermissoes()) {
                permissoes.add(new PermissaoItemResponseDTO(
                        pm.getModulo(),
                        pm.isPodeVer(),
                        pm.isPodeEditar(),
                        pm.isPodeExcluir()));
            }
        }
        return new UsuarioAdministrativoResponseDTO(
                u.getId(),
                u.getNome(),
                u.getEmail(),
                u.isAtivo(),
                p != null && p.isTwoFAHabilitado(),
                p != null ? p.getPapel() : null,
                permissoes);
    }
}
