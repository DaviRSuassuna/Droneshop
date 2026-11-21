import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { PageResponse } from '../../../core/models/page.model';
import { TermoBuscado } from '../../../core/models/termo-buscado.model';
import { TermoBuscadoService } from '../../../core/services/termo-buscado.service';

/** Relatório de termos buscados na loja, agrupado por termo (#43). */
@Component({
  selector: 'app-termos-buscados',
  imports: [DatePipe],
  templateUrl: './termos-buscados.html',
})
export class TermosBuscadosPage implements OnInit {
  private readonly service = inject(TermoBuscadoService);

  readonly semResultado = signal(false);
  readonly dataInicio = signal('');
  readonly dataFim = signal('');
  readonly resultado = signal<PageResponse<TermoBuscado> | null>(null);
  readonly erro = signal(false);

  ngOnInit(): void {
    this.carregar();
  }

  carregar(page = 0): void {
    this.erro.set(false);
    this.service
      .listar(
        { semResultado: this.semResultado(), dataInicio: this.dataInicio(), dataFim: this.dataFim() },
        page,
      )
      .subscribe({ next: (r) => this.resultado.set(r), error: () => this.erro.set(true) });
  }
}
