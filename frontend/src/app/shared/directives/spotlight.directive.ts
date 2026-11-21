import { Directive, ElementRef, inject } from '@angular/core';

/**
 * G. Spotlight (protótipo: navbar-aceternity.js): luz que segue o mouse na capa.
 * Uso: <section class="hero" appSpotlight><div class="ac-spot" aria-hidden="true"></div>...
 * Estilo: styles/efeitos/_spotlight.scss. Sem mouse a luz fica parada no ponto padrão.
 */
@Directive({
  selector: '[appSpotlight]',
  host: { '(pointermove)': 'mover($event)' },
})
export class SpotlightDirective {
  private readonly el = inject<ElementRef<HTMLElement>>(ElementRef);

  mover(e: PointerEvent): void {
    const host = this.el.nativeElement;
    const r = host.getBoundingClientRect();
    host.style.setProperty('--x', `${e.clientX - r.left}px`);
    host.style.setProperty('--y', `${e.clientY - r.top}px`);
  }
}
