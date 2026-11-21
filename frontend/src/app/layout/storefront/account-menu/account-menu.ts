import { Component, computed, input, output } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AREAS_CONTA, ResumoConta, iniciais } from './areas-conta';

/**
 * Menu lateral da "Minha conta" (protótipo 02, conta-perfil.html). Abaixo de 900px ele sai e
 * a conta abre na folha do celular (conta-folha), pelo ícone do menu da loja.
 * As telas da conta (trilha A) usam: <app-account-menu [conta]="..." (sair)="..." />.
 */
@Component({
  selector: 'app-account-menu',
  imports: [RouterLink, RouterLinkActive],
  template: `
    <nav class="side" aria-label="Minha conta">
      @if (conta(); as c) {
        <div class="me">
          <span class="av" aria-hidden="true">{{ avatar() }}</span>
          <div>
            <b>{{ c.nome }}</b>
            @if (c.detalhe) {
              <span>{{ c.detalhe }}</span>
            }
          </div>
        </div>
      }
      @for (item of areas; track item.area) {
        <a
          [routerLink]="item.rota"
          routerLinkActive
          ariaCurrentWhenActive="page"
          >{{ item.nome }}
          @if (contagem(item.area); as n) {
            <em>{{ n }}</em>
          }
        </a>
      }
      <button class="out" type="button" (click)="sair.emit()">Sair da conta</button>
    </nav>
  `,
})
export class AccountMenu {
  readonly conta = input<ResumoConta | null>(null);
  readonly sair = output<void>();

  protected readonly areas = AREAS_CONTA;
  protected readonly avatar = computed(() => iniciais(this.conta()?.nome ?? ''));

  protected contagem(area: (typeof AREAS_CONTA)[number]['area']): number | undefined {
    return this.conta()?.contagens?.[area] || undefined;
  }
}
