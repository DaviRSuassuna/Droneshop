import { Directive } from '@angular/core';

/**
 * G. Brilho na borda dos cards do admin (protótipo: admin-fx.js, bloco G). Fica no layout e
 * escuta o ponteiro uma vez só: põe --x e --y no `.card` que está sob o mouse.
 * Estilo: styles/efeitos-admin/_brilho.scss. Sem ele, o card só não acende.
 */
@Directive({
  selector: '[appBrilhoCards]',
  host: { '(pointermove)': 'mover($event)' },
})
export class BrilhoCardsDirective {
  mover(e: PointerEvent): void {
    const card = (e.target as Element | null)?.closest?.<HTMLElement>('main .card');
    if (!card) return;
    const r = card.getBoundingClientRect();
    card.style.setProperty('--x', `${e.clientX - r.left}px`);
    card.style.setProperty('--y', `${e.clientY - r.top}px`);
  }
}
