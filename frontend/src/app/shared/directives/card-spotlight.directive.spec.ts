import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { CardSpotlightDirective } from './card-spotlight.directive';

@Component({
  imports: [CardSpotlightDirective],
  template: `<article class="pcard" appCardSpotlight>Falcon</article>`,
})
class Card {}

describe('CardSpotlightDirective', () => {
  it('liga o efeito e segue o mouse com --x e --y relativos ao card', () => {
    TestBed.configureTestingModule({ imports: [Card] });
    const fixture = TestBed.createComponent(Card);
    fixture.detectChanges();
    const card = (fixture.nativeElement as HTMLElement).querySelector('article')!;
    expect(card.classList.contains('ac-glow')).toBe(true);
    expect(card.classList.contains('pcard')).toBe(true);

    vi.spyOn(card, 'getBoundingClientRect').mockReturnValue({ left: 100, top: 50 } as DOMRect);
    const e = new MouseEvent('pointermove', { clientX: 130, clientY: 70 });
    card.dispatchEvent(e);
    expect(card.style.getPropertyValue('--x')).toBe('30px');
    expect(card.style.getPropertyValue('--y')).toBe('20px');
  });
});
