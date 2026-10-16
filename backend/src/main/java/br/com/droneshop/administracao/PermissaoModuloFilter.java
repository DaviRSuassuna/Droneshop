package br.com.droneshop.administracao;

import java.lang.reflect.Method;
import java.util.Set;

import org.eclipse.microprofile.jwt.JsonWebToken;

import br.com.droneshop.administracao.model.ModuloSistema;
import br.com.droneshop.administracao.model.PapelAdministrativo;
import br.com.droneshop.administracao.model.PerfilAdministrativo;
import br.com.droneshop.administracao.model.PermissaoModulo;
import br.com.droneshop.identidade.model.Usuario;
import br.com.droneshop.identidade.repository.UsuarioRepository;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
@Priority(Priorities.AUTHORIZATION)
public class PermissaoModuloFilter implements ContainerRequestFilter {

    @Inject
    JsonWebToken jwt;

    @Inject
    UsuarioRepository usuarioRepository;

    @Context
    ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (resourceInfo == null) return;
        Method method = resourceInfo.getResourceMethod();
        Class<?> resourceClass = resourceInfo.getResourceClass();
        if (method == null || resourceClass == null) return;

        ModuloAdmin moduloAdmin = method.getAnnotation(ModuloAdmin.class);
        if (moduloAdmin == null) {
            moduloAdmin = resourceClass.getAnnotation(ModuloAdmin.class);
        }
        if (moduloAdmin == null) {
            return;
        }

        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new NotAuthorizedException("Credenciais ausentes ou inválidas.");
        }

        Set<String> groups = jwt.getGroups();
        if (groups == null || !groups.contains("ADMINISTRATIVO")) {
            throw new ForbiddenException("Usuário sem perfil administrativo.");
        }

        Long usuarioId;
        try {
            usuarioId = Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException e) {
            throw new ForbiddenException("Token inválido.");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId);
        if (usuario == null || !usuario.isAtivo() || usuario.getPerfilAdministrativo() == null) {
            throw new ForbiddenException("Usuário inativo ou sem perfil administrativo.");
        }

        PerfilAdministrativo perfil = usuario.getPerfilAdministrativo();
        if (perfil.getPapel() == PapelAdministrativo.ADMIN) {
            return;
        }

        ModuloSistema modulo = moduloAdmin.value();
        String httpMethod = requestContext.getMethod();

        PermissaoModulo permissao = null;
        if (perfil.getPermissoes() != null) {
            for (PermissaoModulo p : perfil.getPermissoes()) {
                if (p.getModulo() == modulo) {
                    permissao = p;
                    break;
                }
            }
        }

        boolean permitido = false;
        if (permissao != null) {
            if ("GET".equalsIgnoreCase(httpMethod) || "HEAD".equalsIgnoreCase(httpMethod) || "OPTIONS".equalsIgnoreCase(httpMethod)) {
                permitido = permissao.isPodeVer();
            } else if ("DELETE".equalsIgnoreCase(httpMethod)) {
                permitido = permissao.isPodeExcluir();
            } else {
                permitido = permissao.isPodeEditar();
            }
        }

        if (!permitido) {
            throw new ForbiddenException("Usuário sem permissão no módulo " + modulo.getNome());
        }
    }
}
