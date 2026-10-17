import { HttpErrorResponse } from '@angular/common/http';
import { Component, Input, OnChanges, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  MidiaProduto,
  NomeTipoMidia,
  TipoMidia,
} from '../../../../../core/models/midia-produto.model';
import { ProblemDetail } from '../../../../../core/models/problem.model';
import { MidiaProdutoService } from '../../../../../core/services/midia-produto.service';

@Component({
  selector: 'app-produto-midias',
  imports: [FormsModule],
  templateUrl: './produto-midias.html',
  styleUrl: './produto-midias.scss',
})
export class ProdutoMidias implements OnChanges {
  @Input({ required: true }) produtoId: number | null = null;

  private readonly service = inject(MidiaProdutoService);

  readonly tipos = signal<TipoMidia[]>([]);
  readonly tipoSelecionado = signal<NomeTipoMidia>('IMAGEM');
  readonly midias = signal<MidiaProduto[]>([]);
  readonly arquivo = signal<File | null>(null);
  readonly altText = signal('');
  readonly url = signal('');
  readonly principal = signal(false);
  readonly carregando = signal(false);
  readonly enviando = signal(false);
  readonly salvandoOrdem = signal(false);
  readonly erro = signal<string | null>(null);
  readonly ordemAlterada = signal(false);

  ngOnChanges(): void {
    if (this.produtoId === null) return;
    this.carregando.set(true);
    this.service.listarTipos().subscribe({
      next: (tipos) => this.tipos.set(tipos),
      error: (err: HttpErrorResponse) =>
        this.mostrarErro(err, 'Não foi possível carregar os tipos de mídia.'),
    });
    this.carregarMidias();
  }

  get aceitaArquivo(): string {
    return this.tipoSelecionado() === 'MANUAL_PDF'
      ? '.pdf,application/pdf'
      : '.png,.jpg,.jpeg,image/png,image/jpeg';
  }

  get podeEnviar(): boolean {
    if (this.produtoId === null || this.enviando()) return false;
    if (this.tipoSelecionado() === 'VIDEO') return this.url().trim().length > 0;
    if (this.arquivo() === null) return false;
    return this.tipoSelecionado() !== 'IMAGEM' || this.altText().trim().length > 0;
  }

  aoMudarTipo(nome: string): void {
    if (nome === 'IMAGEM' || nome === 'VIDEO' || nome === 'MANUAL_PDF') {
      this.tipoSelecionado.set(nome);
      this.arquivo.set(null);
      this.url.set('');
      this.altText.set('');
    }
  }

  aoEscolherArquivo(event: Event): void {
    const input = event.currentTarget;
    if (input instanceof HTMLInputElement) this.arquivo.set(input.files?.item(0) ?? null);
  }

  enviar(): void {
    this.erro.set(null);
    if (this.produtoId === null || !this.podeEnviar) return;
    const tipo = this.tipos().find((t) => t.nome === this.tipoSelecionado());
    if (!tipo) {
      this.erro.set('Não foi possível identificar o tipo de mídia selecionado.');
      return;
    }

    this.enviando.set(true);
    this.service
      .adicionar(this.produtoId, {
        idTipo: tipo.id,
        arquivo: this.arquivo() ?? undefined,
        url: this.url().trim() || undefined,
        altText: this.altText().trim() || undefined,
        principal: this.principal(),
      })
      .subscribe({
        next: (midia) => {
          this.midias.update((lista) => [...lista, midia].sort((a, b) => a.ordem - b.ordem));
          this.arquivo.set(null);
          this.altText.set('');
          this.url.set('');
          this.principal.set(false);
          this.enviando.set(false);
        },
        error: (err: HttpErrorResponse) => {
          this.enviando.set(false);
          this.mostrarErro(err, 'Não foi possível enviar a mídia.');
        },
      });
  }

  definirPrincipal(midia: MidiaProduto): void {
    this.erro.set(null);
    this.service.alterar(midia.id, { principal: true }).subscribe({
      next: () =>
        this.midias.update((lista) =>
          lista.map((item) => ({ ...item, principal: item.id === midia.id })),
        ),
      error: (err: HttpErrorResponse) =>
        this.mostrarErro(err, 'Não foi possível definir a mídia principal.'),
    });
  }

  mover(indice: number, direcao: -1 | 1): void {
    const lista = [...this.midias()];
    const destino = indice + direcao;
    if (destino < 0 || destino >= lista.length) return;
    [lista[indice], lista[destino]] = [lista[destino], lista[indice]];
    this.midias.set(lista.map((midia, i) => ({ ...midia, ordem: i + 1 })));
    this.ordemAlterada.set(true);
  }

  salvarOrdem(): void {
    if (this.produtoId === null || !this.ordemAlterada()) return;
    this.erro.set(null);
    this.salvandoOrdem.set(true);
    this.service
      .reordenar(
        this.produtoId,
        this.midias().map((midia) => midia.id),
      )
      .subscribe({
        next: () => {
          this.ordemAlterada.set(false);
          this.salvandoOrdem.set(false);
        },
        error: (err: HttpErrorResponse) => {
          this.salvandoOrdem.set(false);
          this.mostrarErro(err, 'Não foi possível salvar a ordem das mídias.');
          this.carregarMidias();
        },
      });
  }

  excluir(midia: MidiaProduto): void {
    if (!confirm('Excluir esta mídia?')) return;
    this.erro.set(null);
    this.service.excluir(midia.id).subscribe({
      next: () => {
        this.midias.update((lista) =>
          lista
            .filter((item) => item.id !== midia.id)
            .map((item, indice) => ({ ...item, ordem: indice + 1 })),
        );
        this.ordemAlterada.set(true);
      },
      error: (err: HttpErrorResponse) => this.mostrarErro(err, 'Não foi possível excluir a mídia.'),
    });
  }

  arquivoUrl(midia: MidiaProduto): string {
    return this.service.urlArquivo(midia.url);
  }

  private carregarMidias(): void {
    if (this.produtoId === null) return;
    this.service.listar(this.produtoId).subscribe({
      next: (midias) => {
        this.midias.set(midias);
        this.carregando.set(false);
        this.ordemAlterada.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.carregando.set(false);
        this.mostrarErro(err, 'Não foi possível carregar as mídias do produto.');
      },
    });
  }

  private mostrarErro(err: HttpErrorResponse, padrao: string): void {
    const problem = err.error as ProblemDetail | undefined;
    this.erro.set(problem?.detail ?? padrao);
  }
}
