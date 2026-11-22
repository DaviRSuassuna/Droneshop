import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';

const AREA_PROTEGIDA = /^\/(admin|conta|checkout|pedido-confirmado)(\/|\?|$)/;

export const authInterceptor: HttpInterceptorFn = (requisicao, proximo) => {
  const autenticacao = inject(AuthService);
  const router = inject(Router);
  const token = autenticacao.token();
  const api = new URL(environment.apiUrl);
  const url = new URL(requisicao.url, api);
  const rotaLogin = `${api.pathname.replace(/\/$/, '')}/auth/login`;
  const requisicaoDaApi = url.origin === api.origin;
  const requisicaoDeLogin = url.pathname === rotaLogin;
  const autenticada = token && requisicaoDaApi && !requisicaoDeLogin
    ? requisicao.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : requisicao;

  return proximo(autenticada).pipe(
    catchError((erro: unknown) => {
      const sessaoExpirada =
        erro instanceof HttpErrorResponse && erro.status === 401 && autenticada !== requisicao;
      if (!sessaoExpirada) return throwError(() => erro);
      autenticacao.limparSessao();
      // na loja pública a sessão vencida não impede de navegar: refaz a chamada sem o token
      if (!AREA_PROTEGIDA.test(router.url)) return proximo(requisicao);
      void router.navigate(['/entrar'], { queryParams: { returnUrl: router.url } });
      return throwError(() => erro);
    }),
  );
};
