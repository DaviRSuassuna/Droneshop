import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { PreferenciaNotificacaoService } from '../../core/services/preferencia-notificacao.service';

/** Destino do link de descadastro dos e-mails de marketing, sem login (#42, RN10). */
@Component({
  selector: 'app-descadastro',
  imports: [RouterLink],
  template: `
    <main>
      <section class="wrap sec">
        <div class="panel" aria-live="polite">
          @switch (estado()) {
            @case ('ok') {
              <h1>Pronto, você saiu da lista.</h1>
              <p class="formnote">
                Não mandaremos mais esse tipo de e-mail. Os avisos do seu pedido continuam chegando.
                Mude quando quiser em <a routerLink="/conta/notificacoes">Notificações</a>.
              </p>
            }
            @case ('invalido') {
              <h1>Link inválido.</h1>
              <p class="formnote">
                Este link de descadastro não existe. Ajuste as preferências em
                <a routerLink="/conta/notificacoes">Notificações</a>.
              </p>
            }
            @default {
              <p class="formnote">Processando…</p>
            }
          }
        </div>
      </section>
    </main>
  `,
})
export class Descadastro implements OnInit {
  private readonly service = inject(PreferenciaNotificacaoService);

  private readonly rota = inject(ActivatedRoute);
  readonly estado = signal<'carregando' | 'ok' | 'invalido'>('carregando');

  ngOnInit(): void {
    this.service.descadastrar(this.rota.snapshot.paramMap.get('token') ?? '').subscribe({
      next: () => this.estado.set('ok'),
      error: () => this.estado.set('invalido'),
    });
  }
}
