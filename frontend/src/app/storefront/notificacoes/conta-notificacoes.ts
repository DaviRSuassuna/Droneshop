import { Component, OnInit, inject, signal } from '@angular/core';
import { PreferenciaNotificacao } from '../../core/models/notificacao.model';
import { PreferenciaNotificacaoService } from '../../core/services/preferencia-notificacao.service';

const ROTULOS: Record<string, { nome: string; descricao: string }> = {
  MARKETING: { nome: 'Promoções e ofertas', descricao: 'Cupons, lançamentos com desconto e campanhas.' },
  CARRINHO_ABANDONADO: {
    nome: 'Lembrete de carrinho',
    descricao: 'Um aviso quando você deixa itens no carrinho.',
  },
  NOVIDADES: { nome: 'Novidades da Aeria', descricao: 'Novos drones, acessórios e conteúdos.' },
};

/** Preferências de e-mail do cliente (#42): marketing só com opt-in; transacional sempre sai. */
@Component({
  selector: 'app-conta-notificacoes',
  templateUrl: './conta-notificacoes.html',
})
export class ContaNotificacoes implements OnInit {
  private readonly service = inject(PreferenciaNotificacaoService);

  readonly preferencias = signal<PreferenciaNotificacao[]>([]);
  readonly aviso = signal<string | null>(null);

  ngOnInit(): void {
    this.service.listar().subscribe({
      next: (l) => this.preferencias.set(Array.isArray(l) ? l : []),
      error: () => this.aviso.set('Não foi possível carregar as preferências.'),
    });
  }

  rotulo(tipo: string): { nome: string; descricao: string } {
    return ROTULOS[tipo] ?? { nome: tipo, descricao: '' };
  }

  alternar(p: PreferenciaNotificacao, habilitado: boolean): void {
    const lista = this.preferencias().map((x) =>
      x.tipoNotificacao === p.tipoNotificacao ? { ...x, habilitado } : x,
    );
    this.service.salvar([{ tipoNotificacao: p.tipoNotificacao, habilitado }]).subscribe({
      next: () => {
        this.preferencias.set(lista);
        this.aviso.set('Preferência salva.');
      },
      error: () => this.aviso.set('Não foi possível salvar a preferência.'),
    });
  }
}
