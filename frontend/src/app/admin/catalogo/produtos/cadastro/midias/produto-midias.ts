import { Component, input, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ItemMidia, ProdutoMidiasEstado } from './produto-midias-estado';

// D42: imagem PNG, JPEG ou WebP até 5 MB (o back confere de novo, com a assinatura do arquivo)
const TAMANHO_MAXIMO = 5 * 1024 * 1024;
const LIMITE_GALERIA = 8;

interface Escolha {
  arquivo: File;
  previa: string;
  principal: boolean;
}

/** Card "Mídia" do trilho: imagem principal e galeria, como no protótipo (#12). */
@Component({
  selector: 'app-produto-midias',
  imports: [FormsModule],
  templateUrl: './produto-midias.html',
  styleUrl: './produto-midias.scss',
})
export class ProdutoMidias {
  /** Fora do rascunho a imagem principal vira pendência de publicação. */
  readonly publicado = input(false);

  readonly estado = inject(ProdutoMidiasEstado);
  readonly LIMITE_GALERIA = LIMITE_GALERIA;

  readonly escolha = signal<Escolha | null>(null);
  readonly altText = signal('');
  readonly erroArquivo = signal<string | null>(null);

  aoEscolher(event: Event, principal: boolean): void {
    const input = event.target as HTMLInputElement;
    this.escolher(input.files?.item(0) ?? null, principal);
    input.value = '';
  }

  aoSoltar(event: DragEvent, principal: boolean): void {
    event.preventDefault();
    this.escolher(event.dataTransfer?.files.item(0) ?? null, principal);
  }

  permitirSoltar(event: DragEvent): void {
    event.preventDefault();
  }

  confirmar(): void {
    const escolha = this.escolha();
    const alt = this.altText().trim();
    if (!escolha || !alt) return;
    this.estado.adicionar({
      tipo: 'IMAGEM',
      arquivo: escolha.arquivo,
      altText: alt,
      principal: escolha.principal,
    });
    this.descartarEscolha(false);
  }

  cancelar(): void {
    this.descartarEscolha(true);
  }

  excluir(item: ItemMidia): void {
    if (confirm('Excluir esta imagem?')) this.estado.excluir(item);
  }

  private escolher(arquivo: File | null, principal: boolean): void {
    this.erroArquivo.set(null);
    if (!arquivo) return;
    if (!/\.(png|jpe?g|webp)$/i.test(arquivo.name)) {
      this.erroArquivo.set('Envie uma imagem JPG, PNG ou WebP.');
      return;
    }
    if (arquivo.size > TAMANHO_MAXIMO) {
      this.erroArquivo.set('A imagem deve ter no máximo 5 MB.');
      return;
    }
    this.descartarEscolha(true);
    this.escolha.set({ arquivo, previa: URL.createObjectURL(arquivo), principal });
  }

  private descartarEscolha(liberarPrevia: boolean): void {
    const atual = this.escolha();
    if (atual && liberarPrevia) URL.revokeObjectURL(atual.previa);
    this.escolha.set(null);
    this.altText.set('');
  }
}
