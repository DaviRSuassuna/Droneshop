import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { PageResponse } from '../../core/models/page.model';
import { StatusRef } from '../../core/models/status.model';
import { AuditoriaService, LogAuditoria } from '../../core/services/auditoria.service';

/** Log de auditoria das ações administrativas, só para o papel ADMIN (#41). */
@Component({
  selector: 'app-auditoria',
  imports: [DatePipe],
  templateUrl: './auditoria.html',
})
export class AuditoriaPage implements OnInit {
  private readonly service = inject(AuditoriaService);

  readonly acoes = signal<StatusRef[]>([]);
  readonly entidade = signal('');
  readonly entidadeId = signal('');
  readonly idAcao = signal<number | null>(null);
  readonly resultado = signal<PageResponse<LogAuditoria> | null>(null);
  readonly erro = signal<string | null>(null);

  ngOnInit(): void {
    this.service.acoes().subscribe({ next: (a) => this.acoes.set(a), error: () => this.acoes.set([]) });
    this.carregar();
  }

  carregar(page = 0): void {
    this.erro.set(null);
    this.service
      .listar({ entidade: this.entidade(), entidadeId: this.entidadeId(), idAcao: this.idAcao() }, page)
      .subscribe({
        next: (r) => this.resultado.set(r),
        error: (e: HttpErrorResponse) =>
          this.erro.set(
            e.status === 403
              ? 'Só o papel ADMIN consulta a auditoria.'
              : 'Não foi possível carregar a auditoria.',
          ),
      });
  }

  filtrarAcao(valor: string): void {
    this.idAcao.set(valor ? Number(valor) : null);
    this.carregar();
  }
}
