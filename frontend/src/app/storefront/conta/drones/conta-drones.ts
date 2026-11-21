import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DroneDaConta } from '../../../core/models/compra.model';
import { ProblemDetail } from '../../../core/models/problem.model';
import { ContaService } from '../../../core/services/conta.service';
import { AccountMenu } from '../../../layout/storefront/account-menu/account-menu';
import { ContaSessao } from '../conta-sessao';

/** "Minha conta > Meus drones" (#88): série, garantia e registro ANAC dos drones comprados. */
@Component({
  selector: 'app-conta-drones',
  imports: [AccountMenu, RouterLink, DatePipe, FormsModule],
  providers: [ContaSessao],
  templateUrl: './conta-drones.html',
  styleUrl: './conta-drones.scss',
})
export class ContaDrones implements OnInit {
  protected readonly sessao = inject(ContaSessao);
  private readonly conta = inject(ContaService);

  readonly drones = signal<DroneDaConta[]>([]);
  readonly carregado = signal(false);
  readonly mensagem = signal<{ id: number; texto: string; ok: boolean } | null>(null);
  registros: Record<number, string> = {};

  ngOnInit(): void {
    this.sessao.carregar();
    this.conta.drones().subscribe((d) => {
      this.drones.set(d);
      this.registros = Object.fromEntries(d.map((x) => [x.idUnidade, x.registroAnac ?? '']));
      this.carregado.set(true);
    });
  }

  salvar(d: DroneDaConta): void {
    this.conta.registrarAnac(d.idUnidade, (this.registros[d.idUnidade] ?? '').trim()).subscribe({
      next: () => this.mensagem.set({ id: d.idUnidade, texto: 'Registro salvo.', ok: true }),
      error: (e: HttpErrorResponse) => {
        const p = e.error as ProblemDetail | null;
        this.mensagem.set({
          id: d.idUnidade,
          texto: p?.errors?.[0]?.message ?? 'Não foi possível salvar.',
          ok: false,
        });
      },
    });
  }
}
