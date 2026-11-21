import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { AdminNav } from './admin-nav';

@Component({ template: '' })
class Vazia {}

async function montar(url: string) {
  TestBed.configureTestingModule({
    imports: [AdminNav],
    providers: [provideRouter([{ path: '**', component: Vazia }])],
  });
  await TestBed.inject(Router).navigateByUrl(url);
  const fixture = TestBed.createComponent(AdminNav);
  fixture.detectChanges();
  await fixture.whenStable();
  fixture.detectChanges();
  return { fixture, raiz: fixture.nativeElement as HTMLElement };
}

describe('AdminNav', () => {
  it('mostra Dashboard e os três grupos do protótipo 02', async () => {
    const { raiz } = await montar('/admin');
    const itens = Array.from(raiz.querySelectorAll('.tabs > a, .tabs > button')).map((e) =>
      e.textContent?.trim(),
    );
    expect(itens).toEqual(['Dashboard', 'Catálogo', 'Vendas', 'Administração']);
    expect(raiz.querySelector('.tabs > a')!.getAttribute('aria-current')).toBe('page');
  });

  it('marca o grupo da tela atual e o link dentro do menu', async () => {
    const { raiz } = await montar('/admin/drones/novo');
    const botoes = raiz.querySelectorAll('.tabs > button');
    expect(botoes[0].classList.contains('on')).toBe(true);
    expect(botoes[1].classList.contains('on')).toBe(false);
    const atual = raiz.querySelector('#menu-admin-0 a[aria-current]');
    expect(atual?.textContent).toContain('Produtos');
    expect(raiz.querySelector('.tabs > a')!.getAttribute('aria-current')).toBeNull();
  });

  it('separa Posição de estoque de Movimentações', async () => {
    const { raiz } = await montar('/admin/estoque/movimento');
    const atuais = Array.from(raiz.querySelectorAll('#menu-admin-2 a[aria-current]')).map(
      (a) => a.textContent,
    );
    expect(atuais.length).toBe(1);
    expect(atuais[0]).toContain('Movimentações');
  });

  it('abre o menu no clique e fecha com Esc', async () => {
    const { fixture, raiz } = await montar('/admin');
    const vendas = raiz.querySelectorAll<HTMLButtonElement>('.tabs > button')[1];
    vendas.click();
    fixture.detectChanges();
    expect(vendas.getAttribute('aria-expanded')).toBe('true');
    expect(raiz.querySelector('#menu-admin-1')!.classList.contains('on')).toBe(true);

    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
    fixture.detectChanges();
    expect(vendas.getAttribute('aria-expanded')).toBe('false');
  });

  it('mantém todos os links soltos na faixa do celular', async () => {
    const { raiz } = await montar('/admin/cupons');
    const links = raiz.querySelectorAll('.mnav a');
    expect(links.length).toBe(13);
    expect(raiz.querySelector('.mnav a[aria-current]')!.textContent).toBe('Cupons');
  });
});
