import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { BlurRevealDirective } from './blur-reveal.directive';

@Component({
  imports: [BlurRevealDirective],
  template: `<h1 appBlurReveal>Falcon <br />X2 <span>Pro</span></h1>`,
})
class Titulo {}

type Callback = (e: { isIntersecting: boolean }[]) => void;

function reduzir(sim: boolean) {
  vi.stubGlobal('matchMedia', vi.fn(() => ({ matches: sim })));
}

async function montar() {
  const fixture = TestBed.createComponent(Titulo);
  fixture.detectChanges();
  await fixture.whenStable();
  return (fixture.nativeElement as HTMLElement).querySelector('h1')!;
}

describe('BlurRevealDirective', () => {
  let aoCruzar: Callback | undefined;

  beforeEach(() => {
    aoCruzar = undefined;
    vi.stubGlobal(
      'IntersectionObserver',
      class {
        constructor(cb: Callback) {
          aoCruzar = cb;
        }
        observe() {}
        disconnect() {}
      },
    );
    TestBed.configureTestingModule({ imports: [Titulo] });
  });
  afterEach(() => vi.unstubAllGlobals());

  it('divide o título em palavras, inclusive dentro de elementos, mantendo o <br>', async () => {
    reduzir(false);
    const h1 = await montar();
    const palavras = Array.from(h1.querySelectorAll('.sp-w'));
    expect(palavras.map((p) => p.textContent)).toEqual(['Falcon', 'X2', 'Pro']);
    expect(palavras.map((p) => (p as HTMLElement).style.getPropertyValue('--i'))).toEqual([
      '0',
      '1',
      '2',
    ]);
    expect(h1.querySelector('br')).not.toBeNull();
    expect(h1.querySelector('span:not(.sp-w) .sp-w')?.textContent).toBe('Pro');
    expect(h1.getAttribute('aria-label')).toBe('Falcon X2 Pro');
  });

  it('acende quando o título aparece na tela', async () => {
    reduzir(false);
    const h1 = await montar();
    expect(h1.classList.contains('sp-in')).toBe(false);
    aoCruzar!([{ isIntersecting: true }]);
    expect(h1.classList.contains('sp-in')).toBe(true);
  });

  it('com "reduzir movimento" deixa o texto como está', async () => {
    reduzir(true);
    const h1 = await montar();
    expect(h1.querySelector('.sp-w')).toBeNull();
    expect(h1.textContent?.replace(/\s+/g, ' ').trim()).toBe('Falcon X2 Pro');
  });
});
