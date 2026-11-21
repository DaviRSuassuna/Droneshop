import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { SpinnerDirective } from './spinner.directive';

@Component({
  imports: [SpinnerDirective],
  template: `<button class="btn p" [appSpinner]="carregando()">Salvar</button>`,
})
class Botao {
  readonly carregando = signal(false);
}

describe('SpinnerDirective', () => {
  it('mostra o círculo e aria-busy enquanto carrega, sem trocar o texto', () => {
    TestBed.configureTestingModule({ imports: [Botao] });
    const fixture = TestBed.createComponent(Botao);
    fixture.detectChanges();
    const botao = (fixture.nativeElement as HTMLElement).querySelector('button')!;
    expect(botao.querySelector('.sp-spin')).toBeNull();
    expect(botao.hasAttribute('aria-busy')).toBe(false);

    fixture.componentInstance.carregando.set(true);
    fixture.detectChanges();
    expect(botao.querySelectorAll('.sp-spin').length).toBe(1);
    expect(botao.classList.contains('sp-busy')).toBe(true);
    expect(botao.getAttribute('aria-busy')).toBe('true');
    expect(botao.textContent?.trim()).toBe('Salvar');

    fixture.componentInstance.carregando.set(false);
    fixture.detectChanges();
    expect(botao.querySelector('.sp-spin')).toBeNull();
    expect(botao.classList.contains('sp-busy')).toBe(false);
    expect(botao.hasAttribute('aria-busy')).toBe(false);
  });
});
