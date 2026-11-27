import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-acesso-negado',
  imports: [RouterLink],
  template: `
    <main class="wrap">
      <section aria-labelledby="titulo-acesso">
        <h1 id="titulo-acesso">Acesso negado</h1>
        <p>Sua conta não tem permissão para acessar esta página.</p>
        <a class="btn p" routerLink="/">Voltar para a loja</a>
      </section>
    </main>
  `,
  styles: `
    section {
      margin-block: clamp(26px, 6vw, 88px);
      padding: clamp(24px, 5vw, 40px);
      border: 1px solid var(--line);
      border-radius: 16px;
      background: var(--bg2);
      text-align: center;
    }
    h1 {
      margin: 0;
      font: 300 clamp(30px, 5vw, 58px)/1.1 var(--display);
      color: var(--ink);
    }
    p {
      margin-block: 12px 26px;
      color: var(--ink2);
    }
  `,
})
export class AcessoNegado {}
