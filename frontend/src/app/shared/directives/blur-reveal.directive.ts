import { DOCUMENT, DestroyRef, Directive, ElementRef, afterNextRender, inject } from '@angular/core';
import { reduzirMovimento } from './reduzir-movimento';

/**
 * 1. Blur Reveal (protótipo: spell.js): o título entra palavra por palavra quando aparece na tela.
 * Uso: <h1 appBlurReveal>Falcon <br />X2 <span>Pro</span></h1>. Só para texto fixo: a diretiva
 * troca os nós de texto por <span class="sp-w">, então não use com interpolação que muda.
 * Com "reduzir movimento" o texto fica como está. Estilo: styles/efeitos/_blur-reveal.scss.
 */
@Directive({ selector: '[appBlurReveal]' })
export class BlurRevealDirective {
  private readonly el = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly doc = inject(DOCUMENT);

  constructor() {
    const destroyRef = inject(DestroyRef);
    afterNextRender(() => {
      const janela = this.doc.defaultView;
      if (reduzirMovimento(janela)) return;
      const host = this.el.nativeElement;
      // leitor de tela lê o título inteiro, não palavra por palavra
      host.setAttribute('aria-label', (host.textContent ?? '').replace(/\s+/g, ' ').trim());
      this.dividir(host, { i: 0 });

      const Observer = janela?.IntersectionObserver;
      if (!Observer) {
        host.classList.add('sp-in');
        return;
      }
      const io = new Observer(
        (entradas) =>
          entradas.forEach((e) => {
            if (e.isIntersecting) {
              host.classList.add('sp-in');
              io.disconnect();
            }
          }),
        { threshold: 0.2 },
      );
      io.observe(host);
      destroyRef.onDestroy(() => io.disconnect());
    });
  }

  private dividir(no: Node, contador: { i: number }): void {
    for (const filho of Array.from(no.childNodes)) {
      if (filho.nodeType === Node.TEXT_NODE && filho.textContent?.trim()) {
        const partes = this.doc.createDocumentFragment();
        for (const parte of filho.textContent.split(/(\s+)/)) {
          if (!parte) continue;
          if (/^\s+$/.test(parte)) {
            partes.append(parte);
            continue;
          }
          const palavra = this.doc.createElement('span');
          palavra.className = 'sp-w';
          palavra.setAttribute('aria-hidden', 'true');
          palavra.style.setProperty('--i', String(contador.i++));
          palavra.textContent = parte;
          partes.append(palavra);
        }
        filho.replaceWith(partes);
      } else if (filho instanceof Element && !filho.matches('br, svg, .sp-w')) {
        this.dividir(filho, contador);
      }
    }
  }
}
