import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { BrilhoCardsDirective } from './brilho-cards.directive';

@Component({
  imports: [BrilhoCardsDirective],
  template: `<div appBrilhoCards>
    <main>
      <section class="card"><p>Estoque</p></section>
    </main>
    <p class="fora">Fora</p>
  </div>`,
})
class Tela {}

describe('BrilhoCardsDirective', () => {
  it('põe --x e --y no card sob o ponteiro, relativos a ele', () => {
    TestBed.configureTestingModule({ imports: [Tela] });
    const fixture = TestBed.createComponent(Tela);
    fixture.detectChanges();
    const raiz = fixture.nativeElement as HTMLElement;
    const card = raiz.querySelector<HTMLElement>('.card')!;
    vi.spyOn(card, 'getBoundingClientRect').mockReturnValue({ left: 100, top: 50 } as DOMRect);

    raiz
      .querySelector('p')!
      .dispatchEvent(new MouseEvent('pointermove', { bubbles: true, clientX: 130, clientY: 70 }));

    expect(card.style.getPropertyValue('--x')).toBe('30px');
    expect(card.style.getPropertyValue('--y')).toBe('20px');
  });

  it('ignora o ponteiro fora de um card', () => {
    TestBed.configureTestingModule({ imports: [Tela] });
    const fixture = TestBed.createComponent(Tela);
    fixture.detectChanges();
    const raiz = fixture.nativeElement as HTMLElement;
    raiz.querySelector('.fora')!.dispatchEvent(new MouseEvent('pointermove', { bubbles: true }));
    expect(raiz.querySelector<HTMLElement>('.card')!.style.getPropertyValue('--x')).toBe('');
  });
});
