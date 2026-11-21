import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { PopButtonDirective } from './pop-button.directive';

@Component({
  imports: [PopButtonDirective],
  template: `<button appPopButton (click)="cliques = cliques + 1">Sacola</button>`,
})
class Botao {
  cliques = 0;
}

function reduzir(sim: boolean) {
  vi.stubGlobal('matchMedia', vi.fn(() => ({ matches: sim })));
}

function montar() {
  TestBed.configureTestingModule({ imports: [Botao] });
  const fixture = TestBed.createComponent(Botao);
  fixture.detectChanges();
  const botao = (fixture.nativeElement as HTMLElement).querySelector('button')!;
  return { fixture, botao };
}

describe('PopButtonDirective', () => {
  beforeEach(() => vi.useFakeTimers());
  afterEach(() => {
    document.querySelectorAll('.sp-spark').forEach((s) => s.remove());
    vi.useRealTimers();
    vi.unstubAllGlobals();
  });

  it('pula, solta 8 faíscas que somem e não impede o clique', () => {
    reduzir(false);
    const { fixture, botao } = montar();
    botao.click();
    expect(botao.classList.contains('sp-pop')).toBe(true);
    expect(document.querySelectorAll('.sp-spark').length).toBe(8);
    expect(fixture.componentInstance.cliques).toBe(1);
    vi.advanceTimersByTime(650);
    expect(document.querySelectorAll('.sp-spark').length).toBe(0);
  });

  it('com "reduzir movimento" só deixa o clique seguir', () => {
    reduzir(true);
    const { fixture, botao } = montar();
    botao.click();
    expect(botao.classList.contains('sp-pop')).toBe(false);
    expect(document.querySelectorAll('.sp-spark').length).toBe(0);
    expect(fixture.componentInstance.cliques).toBe(1);
  });
});
