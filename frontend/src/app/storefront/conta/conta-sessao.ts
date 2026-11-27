import { Injectable, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { ResumoConta } from '../../layout/storefront/account-menu/areas-conta';
import { AuthService } from '../../core/services/auth.service';

/**
 * Dados do menu da "Minha conta" (<app-account-menu>) e o "Sair" de verdade (#19).
 * Uma instância por tela da conta: carrega o perfil e expõe o resumo.
 */
@Injectable()
export class ContaSessao {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly resumo = signal<ResumoConta | null>(null);

  carregar(): void {
    this.auth.eu().subscribe((p) => this.resumo.set({ nome: p.nome, detalhe: p.email }));
  }

  /** Atualiza o nome mostrado no menu depois de salvar o perfil. */
  renomear(nome: string): void {
    this.resumo.update((r) => (r ? { ...r, nome } : r));
  }

  sair(): void {
    this.auth.sair().subscribe({
      complete: () => void this.router.navigateByUrl('/entrar'),
      error: () => void this.router.navigateByUrl('/entrar'),
    });
  }
}
