import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { CHAVE_FAIXA_FECHADA, FaixaPromocao } from './faixa-promocao';

function montar() {
  const fixture = TestBed.createComponent(FaixaPromocao);
  fixture.detectChanges();
  return { fixture, el: fixture.nativeElement as HTMLElement };
}

describe('FaixaPromocao', () => {
  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({ imports: [FaixaPromocao], providers: [provideRouter([])] });
  });

  it('mostra frete grátis, o cupom fixo e o link para ofertas', () => {
    const { el } = montar();
    expect(el.querySelector('.ac-ann')?.textContent).toContain('Frete grátis');
    expect(el.querySelector('code')?.textContent).toBe('PILOTO10');
    expect(el.querySelector('a')?.getAttribute('href')).toBe('/ofertas');
  });

  it('fecha no "×" e lembra na sessão', () => {
    const { fixture, el } = montar();
    el.querySelector<HTMLButtonElement>('button[aria-label="Fechar aviso"]')!.click();
    fixture.detectChanges();
    expect(el.querySelector('.ac-ann')).toBeNull();
    expect(sessionStorage.getItem(CHAVE_FAIXA_FECHADA)).toBe('1');
  });

  it('não aparece quando já foi fechada nesta sessão', () => {
    sessionStorage.setItem(CHAVE_FAIXA_FECHADA, '1');
    const { el } = montar();
    expect(el.querySelector('.ac-ann')).toBeNull();
  });

  it('fecha mesmo sem sessionStorage disponível', () => {
    const espiao = vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
      throw new Error('bloqueado');
    });
    const { fixture, el } = montar();
    el.querySelector<HTMLButtonElement>('button')!.click();
    fixture.detectChanges();
    expect(el.querySelector('.ac-ann')).toBeNull();
    espiao.mockRestore();
  });
});
