import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';
import { environment } from '../../../../environments/environment';
import { ContaFolha } from '../account-menu/conta-folha';
import { ALTURA_SOME, ATRASO_FECHA_MEGA, StorefrontNav } from './storefront-nav';

@Component({ template: '' })
class Vazia {}

function montar() {
  TestBed.configureTestingModule({
    imports: [StorefrontNav],
    providers: [
      provideRouter([{ path: '**', component: Vazia }]),
      provideHttpClient(),
      provideHttpClientTesting(),
    ],
  });
  const fixture = TestBed.createComponent(StorefrontNav);
  fixture.detectChanges();
  const el = fixture.nativeElement as HTMLElement;
  const navwrap = el.querySelector('.navwrap') as HTMLElement;
  const rolar = (y: number) => {
    Object.defineProperty(window, 'scrollY', { value: y, configurable: true });
    window.dispatchEvent(new Event('scroll'));
    fixture.detectChanges();
  };
  return { fixture, el, navwrap, rolar, nav: fixture.componentInstance };
}

function celular(sim: boolean) {
  vi.stubGlobal(
    'matchMedia',
    vi.fn((q: string) => ({ matches: sim && q.includes('max-width'), media: q })),
  );
}

describe('StorefrontNav', () => {
  afterEach(() => {
    Object.defineProperty(window, 'scrollY', { value: 0, configurable: true });
    vi.useRealTimers();
    vi.unstubAllGlobals();
  });

  it('tem os links do protótipo, com Ofertas em destaque', () => {
    const { el } = montar();
    const links = Array.from(el.querySelectorAll('nav.links a'));
    expect(links.map((a) => a.textContent?.trim())).toEqual([
      'Drones',
      'Acessórios',
      'Empresas',
      'Suporte',
      'Ofertas',
    ]);
    expect(links.map((a) => a.getAttribute('href'))).toEqual([
      '/drones',
      '/acessorios',
      '/empresas',
      '/suporte',
      '/ofertas',
    ]);
    expect(el.querySelector('nav.links a.hot')?.textContent).toBe('Ofertas');
    expect(el.querySelector('a.mark')?.getAttribute('href')).toBe('/');
  });

  it('encolhe depois do topo, some ao descer e volta ao subir', () => {
    const { navwrap, rolar } = montar();
    rolar(50);
    expect(navwrap.classList.contains('ac-small')).toBe(true);
    expect(navwrap.classList.contains('ac-hide')).toBe(false);

    rolar(ALTURA_SOME + 100);
    expect(navwrap.classList.contains('ac-hide')).toBe(true);

    rolar(ALTURA_SOME + 50);
    expect(navwrap.classList.contains('ac-hide')).toBe(false);

    rolar(0);
    expect(navwrap.classList.contains('ac-small')).toBe(false);
  });

  it('não some ao descer com o menu do celular aberto', () => {
    const { el, navwrap, rolar, fixture } = montar();
    el.querySelector<HTMLButtonElement>('button.menu')!.click();
    fixture.detectChanges();
    rolar(ALTURA_SOME + 300);
    expect(navwrap.classList.contains('ac-hide')).toBe(false);
  });

  it('o botão de menu abre e fecha os links, com aria-expanded', () => {
    const { el, fixture } = montar();
    const botao = el.querySelector<HTMLButtonElement>('button.menu')!;
    botao.click();
    fixture.detectChanges();
    expect(el.querySelector('nav.links')?.classList.contains('open')).toBe(true);
    expect(botao.getAttribute('aria-expanded')).toBe('true');
    botao.click();
    fixture.detectChanges();
    expect(el.querySelector('nav.links')?.classList.contains('open')).toBe(false);
  });

  it('a busca leva para /drones com o termo em q', async () => {
    const { el, fixture } = montar();
    const router = TestBed.inject(Router);
    const input = el.querySelector<HTMLInputElement>('form.search input')!;
    input.value = '  falcon  ';
    el.querySelector('form.search')!.dispatchEvent(new Event('submit', { cancelable: true }));
    await fixture.whenStable();
    expect(router.url).toBe('/drones?q=falcon');
  });

  it('busca vazia leva para /drones sem q', async () => {
    const { el, fixture } = montar();
    el.querySelector('form.msearch')!.dispatchEvent(new Event('submit', { cancelable: true }));
    await fixture.whenStable();
    expect(TestBed.inject(Router).url).toBe('/drones');
  });

  it('a lupa do celular abre o campo de busca e fecha o menu', () => {
    const { el, fixture } = montar();
    el.querySelector<HTMLButtonElement>('button.menu')!.click();
    fixture.detectChanges();
    el.querySelector<HTMLButtonElement>('button.sbtn')!.click();
    fixture.detectChanges();
    expect(el.querySelector('form.msearch')?.classList.contains('open')).toBe(true);
    expect(el.querySelector('nav.links')?.classList.contains('open')).toBe(false);
  });

  it('"/" leva o foco para a busca, fora de campos de texto', () => {
    const { el } = montar();
    document.body.append(el);
    const input = el.querySelector<HTMLInputElement>('form.search input')!;
    Object.defineProperty(input, 'offsetParent', { value: document.body, configurable: true });
    const e = new KeyboardEvent('keydown', { key: '/', cancelable: true });
    document.dispatchEvent(e);
    expect(document.activeElement).toBe(input);
    expect(e.defaultPrevented).toBe(true);
    el.remove();
  });

  it('mega menu de Drones abre no hover, fecha com atraso e com Esc', () => {
    vi.useFakeTimers();
    const { el, fixture } = montar();
    const gatilho = el.querySelector<HTMLAnchorElement>('nav.links a[href="/drones"]')!;
    const mega = el.querySelector('.ac-mega')!;

    gatilho.dispatchEvent(new MouseEvent('mouseenter'));
    fixture.detectChanges();
    expect(mega.classList.contains('on')).toBe(true);
    expect(gatilho.getAttribute('aria-expanded')).toBe('true');

    gatilho.dispatchEvent(new MouseEvent('mouseleave'));
    mega.dispatchEvent(new MouseEvent('mouseenter'));
    vi.advanceTimersByTime(ATRASO_FECHA_MEGA);
    fixture.detectChanges();
    expect(mega.classList.contains('on')).toBe(true);

    mega.dispatchEvent(new MouseEvent('mouseleave'));
    vi.advanceTimersByTime(ATRASO_FECHA_MEGA);
    fixture.detectChanges();
    expect(mega.classList.contains('on')).toBe(false);

    gatilho.dispatchEvent(new FocusEvent('focus'));
    fixture.detectChanges();
    expect(mega.classList.contains('on')).toBe(true);
    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
    fixture.detectChanges();
    expect(mega.classList.contains('on')).toBe(false);
  });

  it('mega menu tem as colunas do protótipo e o destaque do Falcon X2 Pro', () => {
    const { el } = montar();
    const colunas = Array.from(el.querySelectorAll('.ac-mega .col .lbl')).map((l) => l.textContent);
    expect(colunas).toEqual(['Por uso', 'Por marca', 'Atalhos']);
    expect(el.querySelector('.ac-mega .feat')?.getAttribute('href')).toBe('/produto/falcon-x2-pro');
  });

  it('mega menu monta "Por uso" e "Por marca" com as categorias e marcas da API', () => {
    const { el, fixture } = montar();
    const http = TestBed.inject(HttpTestingController);
    http
      .expectOne(
        (r) =>
          r.url === `${environment.apiUrl}/loja/categorias` && r.params.get('tipo') === 'DRONE',
      )
      .flush([
        {
          id: 1,
          nome: 'Profissionais',
          slug: 'profissionais',
          tipo: { id: 1, nome: 'DRONE' },
          subcategorias: [{ id: 7, nome: 'Cinema', slug: 'cinema' }],
        },
      ]);
    http
      .expectOne(
        (r) => r.url === `${environment.apiUrl}/loja/marcas` && r.params.get('destaque') === 'true',
      )
      .flush([{ id: 2, nome: 'Aerion', slug: 'aerion', logoUrl: null }]);
    fixture.detectChanges();
    const links = Array.from(el.querySelectorAll('.ac-mega .col a')).map((a) =>
      a.getAttribute('href'),
    );
    expect(links).toContain('/drones?subcategoria=cinema');
    expect(links).toContain('/drones?marca=aerion');
  });

  it('navegar fecha menu, busca e mega menu', async () => {
    const { el, fixture, nav } = montar();
    nav.menuAberto.set(true);
    nav.buscaAberta.set(true);
    nav.megaAberto.set(true);
    await TestBed.inject(Router).navigateByUrl('/ofertas');
    fixture.detectChanges();
    expect(el.querySelector('nav.links')?.classList.contains('open')).toBe(false);
    expect(el.querySelector('form.msearch')?.classList.contains('open')).toBe(false);
    expect(el.querySelector('.ac-mega')?.classList.contains('on')).toBe(false);
  });

  it('no celular o ícone da conta abre a folha; no desktop vai para o perfil', () => {
    const { el, fixture } = montar();
    const folha = fixture.debugElement.query((d) => d.componentInstance instanceof ContaFolha)
      .componentInstance as ContaFolha;
    const abrir = vi.spyOn(folha, 'abrir').mockImplementation(() => undefined);
    const conta = el.querySelector<HTMLAnchorElement>('a.acct')!;

    celular(true);
    const noCelular = new MouseEvent('click', { cancelable: true, bubbles: true });
    conta.dispatchEvent(noCelular);
    expect(abrir).toHaveBeenCalledTimes(1);
    expect(noCelular.defaultPrevented).toBe(true);

    celular(false);
    conta.dispatchEvent(new MouseEvent('click', { cancelable: true, bubbles: true }));
    expect(abrir).toHaveBeenCalledTimes(1);
    expect(conta.getAttribute('href')).toBe('/conta/perfil');
  });
});
