import { Directive, ElementRef, inject } from '@angular/core';

/**
 * I. Card Spotlight (protótipo: navbar-aceternity.js): a borda do card acende onde o mouse está.
 * Uso: <article class="pcard" appCardSpotlight>. Estilo: styles/efeitos/_card-spotlight.scss.
 */
@Directive({
  selector: '[appCardSpotlight]',
  host: {
    class: 'ac-glow',
    '(pointermove)': 'mover($event)',
  },
})
export class CardSpotlightDirective {
  private readonly el = inject<ElementRef<HTMLElement>>(ElementRef);

  mover(e: PointerEvent): void {
    const c = this.el.nativeElement;
    const r = c.getBoundingClientRect();
    c.style.setProperty('--x', `${e.clientX - r.left}px`);
    c.style.setProperty('--y', `${e.clientY - r.top}px`);
  }
}
