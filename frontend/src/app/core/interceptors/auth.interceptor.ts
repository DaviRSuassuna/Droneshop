import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { DOCUMENT, inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, map, of, switchMap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';

const AUTENTICACAO_PUBLICA = /^\/auth\/(login|cadastro|renovar|recuperar-senha|sair)$/;

export const authInterceptor: HttpInterceptorFn = (requisicao, proximo) => {
  const base = inject(DOCUMENT).baseURI;
  const api = new URL(environment.apiUrl, base);
  const url = new URL(requisicao.url, base);
  const prefixo = api.pathname.replace(/\/$/, '');
  if (
    api.origin !== url.origin ||
    !url.pathname.startsWith(`${prefixo}/`) ||
    AUTENTICACAO_PUBLICA.test(url.pathname.slice(prefixo.length))
  ) {
    return proximo(requisicao);
  }

  const auth = inject(AuthService);
  const router = inject(Router);
  const comToken = (token: string | null) =>
    token ? requisicao.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : requisicao;
  const tokenOriginal = auth.token();
  const versao = auth.versaoSessao();

  function tratarErro(erro: unknown, token: string | null) {
    if (erro instanceof HttpErrorResponse && auth.versaoSessao() === versao) {
      if (erro.status === 403) {
        void router.navigate(['/acesso-negado']);
      } else if (erro.status === 401 && auth.token() === token) {
        auth.limparSessao();
        const volta = router.currentNavigation()?.finalUrl?.toString() ?? router.url;
        if (!/^\/entrar(?:[?#]|$)/.test(volta)) {
          void router.navigate(['/entrar'], { queryParams: { volta } });
        }
      }
    }
    return throwError(() => erro);
  }

  return proximo(comToken(tokenOriginal)).pipe(
    catchError((erro: unknown) => {
      if (!(erro instanceof HttpErrorResponse) || erro.status !== 401) {
        return tratarErro(erro, tokenOriginal);
      }

      if (auth.versaoSessao() !== versao) return throwError(() => erro);
      const atual = auth.token();
      const renovacao =
        atual !== tokenOriginal
          ? of(atual)
          : auth.renovar().pipe(
              map(() => auth.token()),
              catchError(() => tratarErro(erro, tokenOriginal)),
            );

      return renovacao.pipe(
        switchMap((token) =>
          token && auth.versaoSessao() === versao
            ? proximo(comToken(token)).pipe(
                catchError((falha: unknown) => tratarErro(falha, token)),
              )
            : throwError(() => erro),
        ),
      );
    }),
  );
};
