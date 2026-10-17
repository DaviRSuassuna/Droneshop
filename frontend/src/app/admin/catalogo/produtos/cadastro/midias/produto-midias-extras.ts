import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ItemMidia, ProdutoMidiasEstado } from './produto-midias-estado';

const TAMANHO_MAXIMO = 20 * 1024 * 1024;

/** Vídeo por URL e manual em PDF, dentro de "Mostrar mais opções" como no protótipo (#12, D29). */
@Component({
  selector: 'app-produto-midias-extras',
  imports: [FormsModule],
  templateUrl: './produto-midias-extras.html',
  styleUrl: './produto-midias.scss',
  host: { style: 'display: contents' },
})
export class ProdutoMidiasExtras {
  readonly estado = inject(ProdutoMidiasEstado);

  readonly url = signal('');
  readonly erro = signal<string | null>(null);

  adicionarVideo(): void {
    this.erro.set(null);
    const url = this.url().trim();
    if (!/^https?:\/\/\S+$/i.test(url)) {
      this.erro.set('Informe uma URL HTTP ou HTTPS válida.');
      return;
    }
    this.estado.adicionar({ tipo: 'VIDEO', url });
    this.url.set('');
  }

  aoEscolherManual(event: Event): void {
    this.erro.set(null);
    const input = event.target as HTMLInputElement;
    const arquivo = input.files?.item(0) ?? null;
    input.value = '';
    if (!arquivo) return;
    if (!/\.pdf$/i.test(arquivo.name)) {
      this.erro.set('O manual deve ser um arquivo PDF.');
      return;
    }
    if (arquivo.size > TAMANHO_MAXIMO) {
      this.erro.set('O manual deve ter no máximo 20 MB.');
      return;
    }
    this.estado.adicionar({ tipo: 'MANUAL_PDF', arquivo });
  }

  excluir(item: ItemMidia): void {
    if (confirm('Excluir esta mídia?')) this.estado.excluir(item);
  }
}
