import { DOCUMENT, Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

/** Chave da sessão que lembra que o cliente fechou a faixa (a mesma do protótipo). */
export const CHAVE_FAIXA_FECHADA = 'ac-ann';

/**
 * Faixa de promoção acima do menu (protótipo 02, navbar-aceternity.js, bloco A).
 * Texto e cupom fixos aqui; virão da configuração da loja numa issue futura.
 * Fechar vale para a sessão do navegador.
 */
@Component({
  selector: 'app-faixa-promocao',
  imports: [RouterLink],
  template: `
    @if (aberta()) {
      <div class="ac-ann">
        <span
          ><b>Frete grátis</b> acima de R$ 999<span class="ac-ann-x">
            · 10% off na primeira compra com <code>PILOTO10</code></span
          >
          · <a routerLink="/ofertas">Ver ofertas</a></span
        >
        <button type="button" aria-label="Fechar aviso" (click)="fechar()">×</button>
      </div>
    }
  `,
})
export class FaixaPromocao {
  private readonly sessao = sessaoDoNavegador(inject(DOCUMENT));
  readonly aberta = signal(!lerFechada(this.sessao));

  fechar(): void {
    this.aberta.set(false);
    try {
      this.sessao?.setItem(CHAVE_FAIXA_FECHADA, '1');
    } catch {
      // sem sessionStorage (janela privada): fecha só nesta página
    }
  }
}

function sessaoDoNavegador(doc: Document): Storage | null {
  try {
    return doc.defaultView?.sessionStorage ?? null;
  } catch {
    return null;
  }
}

function lerFechada(sessao: Storage | null): boolean {
  try {
    return !!sessao?.getItem(CHAVE_FAIXA_FECHADA);
  } catch {
    return false;
  }
}
