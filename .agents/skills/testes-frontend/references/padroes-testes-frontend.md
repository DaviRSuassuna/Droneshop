# Padroes de Testes e Checklist do Frontend Angular

Referencia complementar da skill `testes-frontend` com padroes de codigo para Vitest (`@angular/core/testing` + `HttpTestingController`) e checklist de revisao.

## 1. Configuracao Padrao com TestBed e Vitest

### 1.1. Servicos HTTP (`HttpTestingController`)

```typescript
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { ExemploService } from './exemplo.service';

describe('ExemploService', () => {
  const url = `${environment.apiUrl}/exemplos`;
  let service: ExemploService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ExemploService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista com paginacao padrao e filtros', () => {
    service.listar(0, 10).subscribe((res) => expect(res.items).toEqual([]));
    const req = http.expectOne((r) => r.url === url);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('pageSize')).toBe('10');
    req.flush({ items: [], page: 0, pageSize: 10, totalItems: 0, totalPages: 0 });
  });
});
```

### 1.2. Componentes com Renderizacao DOM, Estados e Prevencao de Duplicidade

Ao testar componentes de pagina ou subcomponentes, utilize `Subject` do RxJS para controlar o momento exato em que a requisicao responde. Isso permite testar de forma deterministica:
1. O estado de carregamento (`carregando()` / `salvando()`).
2. O bloqueio de clique/envio duplicado enquanto a requisicao esta pendente.
3. A transicao para o estado de sucesso (navegacao ou renderizacao) ou de erro (`ProblemDetail`).

```typescript
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router, ActivatedRoute, convertToParamMap } from '@angular/router';
import { Subject, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { vi } from 'vitest';

// Exemplo de teste de prevencao de envio duplicado e tratamento de ProblemDetail:
it('ignora cliques duplicados enquanto salva e exibe erros de campo no 400', () => {
  const resposta$ = new Subject<unknown>();
  servicoMock.criar.mockReturnValue(resposta$);

  const fixture = TestBed.createComponent(MarcaCadastro);
  const comp = fixture.componentInstance;
  fixture.detectChanges();

  comp.salvar(true);
  comp.salvar(true); // segundo disparo deve ser bloqueado

  expect(comp.salvando()).toBe(true);
  expect(servicoMock.criar).toHaveBeenCalledTimes(1);

  resposta$.error(
    new HttpErrorResponse({
      status: 400,
      error: {
        detail: 'Verifique os campos destacados.',
        errors: [{ field: 'nome', message: 'Nome da marca e obrigatorio (back).' }],
      },
    }),
  );
  fixture.detectChanges();

  expect(comp.salvando()).toBe(false);
  expect(comp.erroGeral()).toBe('Verifique os campos destacados.');
  expect(comp.mensagemErro('nome')).toBe('Nome da marca e obrigatorio (back).');

  const elemento = fixture.nativeElement as HTMLElement;
  expect(elemento.querySelector('#nome')?.getAttribute('aria-invalid')).toBe('true');
  expect(elemento.querySelector('[role="alert"]')?.textContent).toContain(
    'Verifique os campos destacados.',
  );
});
```

### 1.3. Guards, Interceptores e Sessao (`localStorage` / `sessionStorage`)

Em ambiente de teste, crie um helper de `Storage` isolado em memoria no `beforeEach` para evitar vazamento de estado entre testes:

```typescript
function storage(): Storage {
  const dados = new Map<string, string>();
  return {
    get length() {
      return dados.size;
    },
    clear: () => dados.clear(),
    getItem: (chave) => dados.get(chave) ?? null,
    key: (indice) => Array.from(dados.keys())[indice] ?? null,
    removeItem: (chave) => void dados.delete(chave),
    setItem: (chave, valor) => void dados.set(String(chave), String(valor)),
  };
}
```

Sempre restaure mocks e globals no `afterEach`:

```typescript
afterEach(() => {
  vi.restoreAllMocks();
  vi.unstubAllGlobals();
});
```

## 2. Checklist de Revisao de Testes Frontend

Use esta lista antes de concluir a criacao ou revisao de testes:

1. **Comportamento Observavel e Fidelidade**:
   - [ ] Os testes verificam resultados observaveis (DOM, `role="alert"`, `aria-invalid`, chamadas HTTP, rotas, storage) em vez de apenas ler variaveis internas isoladas?
   - [ ] Os mocks refletem fielmente os contratos de `core/models/*.model.ts` e da API (`PageResponse`, `ProblemDetail`, `STATUS_PUBLICACAO`)?
   - [ ] Nenhum uso de `any` foi introduzido nos arquivos `*.spec.ts`?
2. **Cobertura de Fluxos Criticos**:
   - [ ] **Renderizacao e Estados**: estados de carregamento (`loading`), lista preenchida e estado vazio (`empty state`) foram testados?
   - [ ] **Interacao e Formularios**: filtros, paginacao, modo rascunho vs. publicacao (D21/D28) e calculo de `pendencias()` foram cobertos?
   - [ ] **Chamadas HTTP**: verbo, URL, query params (`HttpParams`), body e `http.verify()` no `afterEach` estao presentes?
   - [ ] **Erros**: respostas `400` (`ProblemDetail` com `errors` por campo e `detail` geral), `404` (registro nao encontrado) e `409` (conflito ao excluir registro em uso) foram verificadas?
   - [ ] **Navegacao e Autorizacao**: redirecionamento pos-acao, bloqueio por guard com `returnUrl` e sanitizacao contra open redirect foram testados?
   - [ ] **Sessao Expirada**: remocao de token expirado/invalido e limpeza + redirecionamento ao receber `401` no interceptor foram cobertos?
   - [ ] **Prevencao de Envio Duplicado**: submissoes ou cliques repetidos enquanto uma requisicao esta em andamento sao bloqueados e testados com `Subject`?
3. **Execucao e Evidencia**:
   - [ ] `npx ng test --watch=false` foi executado e todos os testes passaram sem erros?
   - [ ] Os resultados reais (comandos, contagens de arquivos e testes) foram registrados na resposta final?
