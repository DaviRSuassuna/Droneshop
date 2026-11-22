import { HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { map } from 'rxjs';
import { environment } from '../../../environments/environment';

const PREFIXO = '/midias/arquivos/';

/** Troca os caminhos relativos de mídia em qualquer campo do JSON pela URL absoluta da API. */
export function absolutizarMidias(valor: unknown, api: string): unknown {
  if (typeof valor === 'string') return valor.startsWith(PREFIXO) ? api + valor : valor;
  if (Array.isArray(valor)) return valor.map((item) => absolutizarMidias(item, api));
  if (valor && typeof valor === 'object') {
    const copia: Record<string, unknown> = {};
    for (const [chave, item] of Object.entries(valor)) copia[chave] = absolutizarMidias(item, api);
    return copia;
  }
  return valor;
}

/**
 * A API devolve as imagens do S3 como "/midias/arquivos/<nome>" (o arquivo é servido por ela);
 * sem isso o navegador buscaria o caminho no servidor do Angular.
 */
export const midiaUrlInterceptor: HttpInterceptorFn = (requisicao, proximo) => {
  const api = environment.apiUrl.replace(/\/$/, '');
  if (!requisicao.url.startsWith(api)) return proximo(requisicao);
  return proximo(requisicao).pipe(
    map((evento) =>
      evento instanceof HttpResponse && evento.body && typeof evento.body === 'object'
        ? evento.clone({ body: absolutizarMidias(evento.body, api) })
        : evento,
    ),
  );
};
