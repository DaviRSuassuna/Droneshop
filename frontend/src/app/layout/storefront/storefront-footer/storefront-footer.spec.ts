import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { StorefrontFooter } from './storefront-footer';

describe('StorefrontFooter', () => {
  it('tem as três colunas de links e a linha legal do protótipo', () => {
    TestBed.configureTestingModule({ imports: [StorefrontFooter], providers: [provideRouter([])] });
    const fixture = TestBed.createComponent(StorefrontFooter);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;

    const colunas = Array.from(el.querySelectorAll('footer .lbl')).map((l) => l.textContent);
    expect(colunas).toEqual(['Explore', 'Suporte', 'Aeria']);
    const hrefs = Array.from(el.querySelectorAll('footer a')).map((a) => a.getAttribute('href'));
    expect(hrefs).toEqual([
      '/drones',
      '/acessorios',
      '/ofertas',
      '/suporte',
      '/suporte',
      '/conta/pedidos',
      '/empresas',
      '/empresas',
      '/suporte',
      '/admin',
    ]);
    expect(el.querySelector('.legal')?.textContent).toContain('projeto acadêmico, dados fictícios');
  });
});
