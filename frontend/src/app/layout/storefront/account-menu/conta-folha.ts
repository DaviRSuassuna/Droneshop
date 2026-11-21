import { Component, ElementRef, computed, input, output, viewChild } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AREAS_CONTA, AreaConta, ResumoConta, iniciais } from './areas-conta';

const DURACAO_FECHAR = 320;
const ARRASTE_FECHA = 70;

/**
 * Folha da conta no celular (protótipo 02, conta-folha.js): o ícone da conta no menu abre uma
 * folha de baixo com as áreas da conta. Tocar fora, arrastar para baixo ou Esc fecha.
 */
@Component({
  selector: 'app-conta-folha',
  imports: [RouterLink, RouterLinkActive],
  host: { style: 'display: contents' },
  templateUrl: './conta-folha.html',
})
export class ContaFolha {
  readonly conta = input<ResumoConta | null>(null);
  readonly sair = output<void>();

  private readonly dialogo = viewChild.required<ElementRef<HTMLDialogElement>>('folha');
  private inicioToque: number | null = null;

  protected readonly areas = AREAS_CONTA;
  protected readonly avatar = computed(() => iniciais(this.conta()?.nome ?? ''));

  protected contagem(area: AreaConta): number | undefined {
    return this.conta()?.contagens?.[area] || undefined;
  }

  abrir(): void {
    const d = this.dialogo().nativeElement;
    if (!d.open) d.showModal?.();
    requestAnimationFrame(() => d.classList.add('on'));
  }

  fechar(): void {
    const d = this.dialogo().nativeElement;
    d.classList.remove('on');
    setTimeout(() => {
      if (d.open) d.close();
    }, DURACAO_FECHAR);
  }

  protected aoClicar(e: MouseEvent): void {
    // clique no fundo (o próprio <dialog>) fecha; clique num link navega e fecha
    const alvo = e.target as Element;
    if (alvo === this.dialogo().nativeElement || alvo.closest('a')) this.fechar();
  }

  protected aoCancelar(e: Event): void {
    e.preventDefault();
    this.fechar();
  }

  protected aoTocar(e: TouchEvent): void {
    this.inicioToque = e.touches[0]?.clientY ?? null;
  }

  protected aoArrastar(e: TouchEvent): void {
    const y = e.touches[0]?.clientY;
    if (this.inicioToque !== null && y !== undefined && y - this.inicioToque > ARRASTE_FECHA) {
      this.inicioToque = null;
      this.fechar();
    }
  }

  protected sairDaConta(): void {
    this.fechar();
    this.sair.emit();
  }
}
