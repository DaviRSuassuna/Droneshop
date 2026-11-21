import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { PageResponse } from '../../../core/models/page.model';
import { LeadEmpresa, LeadEmpresaService, STATUS_LEAD } from '../../../core/services/lead-empresa.service';
import { CnpjPipe } from './cnpj.pipe';

/** Propostas comerciais recebidas pela página Empresas (D36). */
@Component({
  selector: 'app-leads-empresas',
  imports: [DatePipe, CnpjPipe],
  templateUrl: './leads-empresas.html',
})
export class LeadsEmpresasPage implements OnInit {
  private readonly service = inject(LeadEmpresaService);

  readonly status = [
    { id: STATUS_LEAD.NOVO, nome: 'Novo' },
    { id: STATUS_LEAD.EM_CONTATO, nome: 'Em contato' },
    { id: STATUS_LEAD.FECHADO, nome: 'Fechado' },
    { id: STATUS_LEAD.DESCARTADO, nome: 'Descartado' },
  ];
  readonly idStatus = signal<number | null>(null);
  readonly resultado = signal<PageResponse<LeadEmpresa> | null>(null);
  readonly erro = signal<string | null>(null);

  ngOnInit(): void {
    this.carregar();
  }

  carregar(page = 0): void {
    this.service.listar(this.idStatus(), page).subscribe({
      next: (r) => this.resultado.set(r),
      error: () => this.erro.set('Não foi possível carregar as propostas.'),
    });
  }

  filtrar(valor: string): void {
    this.idStatus.set(valor ? Number(valor) : null);
    this.carregar();
  }

  mudar(lead: LeadEmpresa, valor: string): void {
    this.service.alterarStatus(lead.id, Number(valor)).subscribe({
      next: () => this.carregar(this.resultado()?.page ?? 0),
      error: () => this.erro.set('Não foi possível mudar o status.'),
    });
  }
}
