import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { Chamado, STATUS_CHAMADO } from '../../../core/models/chamado.model';
import { PageResponse } from '../../../core/models/page.model';
import { ChamadoService } from '../../../core/services/chamado.service';

/** Fila de chamados do atendimento (#38): responder põe o chamado EM_ANDAMENTO. */
@Component({
  selector: 'app-chamados',
  imports: [DatePipe, ReactiveFormsModule],
  templateUrl: './chamados.html',
  styleUrl: './chamados.scss',
})
export class ChamadosPage implements OnInit {
  private readonly service = inject(ChamadoService);

  readonly status = [
    { id: STATUS_CHAMADO.ABERTO, nome: 'Aberto' },
    { id: STATUS_CHAMADO.EM_ANDAMENTO, nome: 'Em andamento' },
    { id: STATUS_CHAMADO.ENCERRADO, nome: 'Encerrado' },
  ];
  readonly ENCERRADO = STATUS_CHAMADO.ENCERRADO;

  readonly idStatus = signal<number | null>(null);
  readonly resultado = signal<PageResponse<Chamado> | null>(null);
  readonly selecionado = signal<Chamado | null>(null);
  readonly erro = signal<string | null>(null);
  readonly resposta = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.maxLength(5000)],
  });

  ngOnInit(): void {
    this.carregar();
  }

  carregar(page = 0): void {
    this.service.listarAdmin(this.idStatus(), page).subscribe({
      next: (r) => this.resultado.set(r),
      error: () => this.erro.set('Não foi possível carregar os chamados.'),
    });
  }

  filtrar(valor: string): void {
    this.idStatus.set(valor ? Number(valor) : null);
    this.carregar();
  }

  abrir(id: number): void {
    this.erro.set(null);
    this.service.buscarAdmin(id).subscribe({
      next: (c) => this.selecionado.set(c),
      error: () => this.erro.set('Chamado não encontrado.'),
    });
  }

  responder(): void {
    const c = this.selecionado();
    if (!c || this.resposta.invalid) return;
    this.service.atender(c.id, this.resposta.value).subscribe({
      next: () => {
        this.resposta.reset('');
        this.abrir(c.id);
        this.carregar(this.resultado()?.page ?? 0);
      },
      error: (e: HttpErrorResponse) =>
        this.erro.set(e.status === 409 ? 'Chamado encerrado não recebe mensagem.' : 'Falha ao responder.'),
    });
  }

  mudarStatus(idStatus: number): void {
    const c = this.selecionado();
    if (!c) return;
    this.service.alterarStatus(c.id, idStatus).subscribe({
      next: () => {
        this.abrir(c.id);
        this.carregar(this.resultado()?.page ?? 0);
      },
      error: () => this.erro.set('Não foi possível mudar o status.'),
    });
  }
}
