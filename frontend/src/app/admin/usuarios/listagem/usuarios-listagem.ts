import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { UsuarioService } from '../../../core/services/usuario.service';
import { PapelAdministrativo, UsuarioAdministrativo } from '../../../core/models/usuario.model';

const TAMANHO_PAGINA = 10;

@Component({
  selector: 'app-usuarios-listagem',
  imports: [RouterLink],
  templateUrl: './usuarios-listagem.html',
  styleUrl: './usuarios-listagem.scss',
})
export class UsuariosListagem implements OnInit, OnDestroy {
  private readonly usuarioService = inject(UsuarioService);

  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly usuarios = signal<UsuarioAdministrativo[]>([]);
  readonly papeis = signal<PapelAdministrativo[]>([]);

  readonly termoBusca = signal('');
  readonly filtroPapel = signal('');
  readonly filtroStatus = signal('');
  readonly pagina = signal(0);
  readonly totalItens = signal(0);
  readonly totalPaginasApi = signal(0);

  readonly totalAtivos = computed(() => this.usuarios().filter((u) => u.ativo).length);
  readonly totalPaginas = computed(() => Math.max(1, this.totalPaginasApi()));
  readonly usuariosPagina = computed(() => this.usuarios());
  readonly intervaloExibido = computed(() => {
    if (this.totalItens() === 0) return '0–0';
    const inicio = this.pagina() * TAMANHO_PAGINA + 1;
    return `${inicio}–${Math.min(inicio + this.usuarios().length - 1, this.totalItens())}`;
  });
  private buscaTimeout: ReturnType<typeof setTimeout> | undefined;

  ngOnInit(): void {
    this.erro.set(null);
    this.carregando.set(true);
    forkJoin({
      papeis: this.usuarioService.papeis(),
      usuarios: this.usuarioService.listar(
        this.termoBusca().trim(),
        this.filtroPapel() ? Number(this.filtroPapel()) : undefined,
        this.pagina(),
        TAMANHO_PAGINA,
        this.filtroStatus() === '' ? undefined : this.filtroStatus() === 'true',
      ),
    }).subscribe({
      next: ({ papeis, usuarios }) => {
        this.papeis.set(papeis);
        this.usuarios.set(usuarios.items);
        this.totalItens.set(usuarios.totalItems);
        this.totalPaginasApi.set(usuarios.totalPages);
        this.carregando.set(false);
      },
      error: () => {
        this.erro.set('Não foi possível carregar usuários e papéis.');
        this.carregando.set(false);
      },
    });
  }

  ngOnDestroy(): void {
    if (this.buscaTimeout !== undefined) clearTimeout(this.buscaTimeout);
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);

    const filtroAtivo = this.filtroStatus() === '' ? undefined : this.filtroStatus() === 'true';
    this.usuarioService
      .listar(
        this.termoBusca().trim(),
        this.filtroPapel() ? Number(this.filtroPapel()) : undefined,
        this.pagina(),
        TAMANHO_PAGINA,
        filtroAtivo,
      )
      .subscribe({
        next: (res) => {
          this.usuarios.set(res.items);
          this.totalItens.set(res.totalItems);
          this.totalPaginasApi.set(res.totalPages);
          this.carregando.set(false);
        },
        error: () => {
          this.erro.set('Erro ao carregar usuários.');
          this.carregando.set(false);
        },
      });
  }

  nomePapelExibicao(nome?: string): string {
    switch (nome) {
      case 'ADMIN':
        return 'Administrador';
      case 'CATALOGO':
        return 'Catálogo';
      case 'OPERADOR':
        return 'Operador';
      case 'ATENDIMENTO':
        return 'Atendimento';
      case 'SOMENTE_LEITURA':
        return 'Somente leitura';
      default:
        return nome ?? '';
    }
  }

  aoDigitarBusca(valor: string): void {
    this.termoBusca.set(valor);
    this.pagina.set(0);
    if (this.buscaTimeout !== undefined) clearTimeout(this.buscaTimeout);
    this.buscaTimeout = setTimeout(() => this.carregar(), 300);
  }

  aoMudarPapel(valor: string): void {
    this.filtroPapel.set(valor);
    this.pagina.set(0);
    this.carregar();
  }

  aoMudarStatus(valor: string): void {
    this.filtroStatus.set(valor);
    this.pagina.set(0);
    this.carregar();
  }

  paginaAnterior(): void {
    if (this.pagina() === 0) return;
    this.pagina.update((pagina) => pagina - 1);
    this.carregar();
  }

  proximaPagina(): void {
    if (this.pagina() + 1 >= this.totalPaginas()) return;
    this.pagina.update((pagina) => pagina + 1);
    this.carregar();
  }
}
