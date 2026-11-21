import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { StorefrontLayout } from './storefront-layout';

describe('StorefrontLayout', () => {
  it('monta faixa, menu, conteúdo da rota e rodapé, nessa ordem', () => {
    sessionStorage.clear();
    TestBed.configureTestingModule({ imports: [StorefrontLayout], providers: [provideRouter([])] });
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
