import { DOCUMENT, Directive, ElementRef, inject } from '@angular/core';
import { reduzirMovimento } from './reduzir-movimento';

const FAISCAS = 8;
const RAIO = 34;

/**
 * 5. Pop Button (protótipo: spell.js): o botão da sacola pula e solta faíscas no clique.
 * Uso: <button appPopButton (click)="adicionar()">. Não impede o clique nem muda o contador:
 * quem soma no carrinho é a tela. Estilo: styles/efeitos/_pop-button.scss.
 */
@Directive({
  selector: '[appPopButton]',
  host: { '(click)': 'pop()' },
})
export class PopButtonDirective {
  private readonly el = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly doc = inject(DOCUMENT);

  pop(): void {
    if (reduzirMovimento(this.doc.defaultView)) return;
    const b = this.el.nativeElement;
    b.classList.remove('sp-pop');
    void b.offsetWidth; // reinicia a animação em cliques seguidos
    b.classList.add('sp-pop');

    const r = b.getBoundingClientRect();
    const cx = r.left + r.width / 2;
    const cy = r.top + r.height / 2;
    for (let k = 0; k < FAISCAS; k++) {
      const a = (k / FAISCAS) * Math.PI * 2;
      const s = this.doc.createElement('i');
      s.className = 'sp-spark';
      s.setAttribute('aria-hidden', 'true');
      s.style.left = `${cx - 3}px`;
      s.style.top = `${cy - 3}px`;
      s.style.setProperty('--dx', `${Math.cos(a) * RAIO}px`);
      s.style.setProperty('--dy', `${Math.sin(a) * RAIO}px`);
      s.addEventListener('animationend', () => s.remove());
      this.doc.body.append(s);
      setTimeout(() => s.remove(), 650);
    }
  }
}
