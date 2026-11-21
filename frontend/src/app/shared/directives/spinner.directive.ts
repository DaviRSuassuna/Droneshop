import { DOCUMENT, Directive, ElementRef, effect, inject, input } from '@angular/core';

/**
 * 8. Spinner (protótipo: spell.js): botão que processa mostra o círculo girando antes do texto.
 * Uso: <button class="btn p" [appSpinner]="salvando()">Salvar</button>. O texto do botão fica;
 * o estado vai para leitor de tela por aria-busy. Com "reduzir movimento" o círculo fica parado
 * (styles/efeitos/_spinner.scss).
 */
@Directive({
  selector: '[appSpinner]',
  host: {
    '[class.sp-busy]': 'appSpinner()',
    '[attr.aria-busy]': 'appSpinner() || null',
  },
})
export class SpinnerDirective {
  readonly appSpinner = input(false);
  private readonly el = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly doc = inject(DOCUMENT);
  private circulo: HTMLElement | null = null;

  constructor() {
    effect(() => {
      if (this.appSpinner()) {
        if (this.circulo) return;
        this.circulo = this.doc.createElement('span');
        this.circulo.className = 'sp-spin';
        this.circulo.setAttribute('aria-hidden', 'true');
        this.el.nativeElement.prepend(this.circulo);
      } else {
        this.circulo?.remove();
        this.circulo = null;
      }
    });
  }
}
