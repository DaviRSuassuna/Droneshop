import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { StorefrontLayout } from './storefront-layout';

describe('StorefrontLayout', () => {
  it('monta faixa, menu, conteúdo da rota e rodapé, nessa ordem', () => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      imports: [StorefrontLayout],
      // o menu busca categorias e marcas da loja (#21)
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    const fixture = TestBed.createComponent(StorefrontLayout);
    fixture.detectChanges();
    const filhos = Array.from((fixture.nativeElement as HTMLElement).children).map((c) =>
      c.tagName.toLowerCase(),
    );
    expect(filhos).toEqual([
      'app-faixa-promocao',
      'app-storefront-nav',
      'router-outlet',
      'app-storefront-footer',
    ]);
  });
});
