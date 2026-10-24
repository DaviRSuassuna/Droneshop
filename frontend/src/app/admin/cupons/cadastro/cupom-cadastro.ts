import { STATUS_PRODUTO } from '../../../core/models/status.model';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin, Observable } from 'rxjs';
import { Categoria } from '../../../core/models/categoria.model';
import { Cupom, CupomDTO, TIPO_DESCONTO_CUPOM } from '../../../core/models/cupom.model';
import { Marca } from '../../../core/models/marca.model';
import { Produto } from '../../../core/models/produto.model';
import { CategoriaService } from '../../../core/services/categoria.service';
import { CupomService } from '../../../core/services/cupom.service';
import { MarcaService } from '../../../core/services/marca.service';
import { ProdutoService } from '../../../core/services/produto.service';

/** Rascunho e Pausado gravam ativo=false (o cupom não tem status próprio); Ativo grava ativo=true. */
type StatusCupom = 'RASCUNHO' | 'ATIVO' | 'PAUSADO';

/**
 * Cadastro de cupom (#15). Restrições por categoria, marca e produto (só ATIVOS, D40).
 * Descrição interna, "primeiro pedido", "acumula com promoções" e "exibir em Ofertas" do
 * protótipo ainda não têm API e ficam de fora (ver e2e/conformidade/telas.ts).
 */
@Component({
  selector: 'app-cupom-cadastro',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './cupom-cadastro.html',
})
export class CupomCadastro implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly cupomService = inject(CupomService);
  private readonly categoriaService = inject(CategoriaService);
  private readonly marcaService = inject(MarcaService);
  private readonly produtoService = inject(ProdutoService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly cupomId = signal<number | null>(null);
  readonly carregando = signal(false);
  readonly salvando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly categorias = signal<Categoria[]>([]);
  readonly marcas = signal<Marca[]>([]);
  readonly produtos = signal<Produto[]>([]);
  readonly idsCategorias = signal<number[]>([]);
  readonly idsMarcas = signal<number[]>([]);
  readonly idsProdutos = signal<number[]>([]);
  // os já salvos no cupom, para o chip aparecer mesmo se o produto saiu do select
  private readonly produtosSalvos = signal<{ id: number; nome: string; sku: string | null }[]>([]);
  readonly buscaProduto = signal('');
  readonly modoEdicao = computed(() => this.cupomId() !== null);
  readonly TIPO = TIPO_DESCONTO_CUPOM;

  readonly categoriasEscolhidas = computed(() => this.categorias().filter((c) => this.idsCategorias().includes(c.id)));
  readonly marcasEscolhidas = computed(() => this.marcas().filter((m) => this.idsMarcas().includes(m.id)));
  readonly produtosEscolhidos = computed(() =>
    this.idsProdutos().map((id) => {
      const p = this.produtos().find((x) => x.id === id) ?? this.produtosSalvos().find((x) => x.id === id);
      return { id, nome: p?.nome ?? 'Produto indisponível', sku: p?.sku ?? null };
    }),
  );
  /** Até 8 produtos que batem com a busca (nome ou SKU) e ainda não estão no cupom. */
  readonly resultadosProduto = computed(() => {
    const termo = this.buscaProduto().trim().toLowerCase();
    if (termo.length < 2) return [];
    return this.produtos()
      .filter((p) => !this.idsProdutos().includes(p.id))
      .filter((p) => (p.nome ?? '').toLowerCase().includes(termo) || (p.sku ?? '').toLowerCase().includes(termo))
      .slice(0, 8);
  });

  readonly form = this.fb.nonNullable.group({
    codigo: ['', Validators.required],
    idTipoDesconto: [TIPO_DESCONTO_CUPOM.PERCENTUAL as number, Validators.required],
    valor: ['', Validators.required],
    valorMinimo: [''],
    dataInicio: ['', Validators.required],
    dataFim: [''],
    limiteTotalUso: [''],
    limitePorCliente: [''],
    status: ['RASCUNHO' as StatusCupom],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.cupomId.set(Number(id));
      this.carregando.set(true);
    }
    forkJoin({
      categorias: this.categoriaService.listar(0, 1000),
      marcas: this.marcaService.listar(0, 1000),
      produtos: this.produtoService.listar({ idStatus: STATUS_PRODUTO.ATIVO }, 0, 1000),
    }).subscribe({
      next: ({ categorias, marcas, produtos }) => {
        this.categorias.set(categorias.items ?? []);
        this.marcas.set(marcas.items ?? []);
        this.produtos.set(produtos.items ?? []);
        if (this.cupomId() !== null) this.carregarCupom(this.cupomId()!);
      },
      error: () => {
        this.erro.set('Não foi possível carregar as opções de categoria, marca e produto.');
        this.carregando.set(false);
      },
    });
  }

  pendencias(): string[] {
    const values = this.form.getRawValue();
    const faltantes: string[] = [];
    if (!values.codigo.trim()) faltantes.push('Código');
    if (!values.valor.trim()) faltantes.push('Valor');
    if (!values.dataInicio) faltantes.push('Data de início');
    return faltantes;
  }

  /** O select adiciona um item por vez; cada escolhido vira um chip removível. */
  adicionar(event: Event, destino: 'categorias' | 'marcas'): void {
    const select = event.target as HTMLSelectElement;
    const id = Number(select.value);
    select.value = '';
    if (!id) return;
    const alvo = destino === 'categorias' ? this.idsCategorias : this.idsMarcas;
    if (!alvo().includes(id)) alvo.update((ids) => [...ids, id]);
  }

  adicionarProduto(id: number): void {
    if (!this.idsProdutos().includes(id)) this.idsProdutos.update((ids) => [...ids, id]);
    this.buscaProduto.set('');
  }

  remover(destino: 'categorias' | 'marcas' | 'produtos', id: number): void {
    const alvo = { categorias: this.idsCategorias, marcas: this.idsMarcas, produtos: this.idsProdutos }[destino];
    alvo.update((ids) => ids.filter((x) => x !== id));
  }

  previewDesconto(): string {
    const valor = this.numeroOpcional(this.form.controls.valor.value) ?? 0;
    return this.form.controls.idTipoDesconto.value === TIPO_DESCONTO_CUPOM.PERCENTUAL
      ? `− ${valor}%`
      : `− ${this.moeda(valor)}`;
  }

  previewTotal(): string {
    const subtotal = 15228;
    const valor = this.numeroOpcional(this.form.controls.valor.value) ?? 0;
    const desconto = this.form.controls.idTipoDesconto.value === TIPO_DESCONTO_CUPOM.PERCENTUAL
      ? subtotal * valor / 100
      : valor;
    return this.moeda(Math.max(0, subtotal - desconto));
  }

  moeda(valor: number): string {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(valor);
  }

  /** "Salvar rascunho" grava inativo; "Ativar cupom" grava ativo; o status da lateral vale para os outros casos. */
  salvar(ativar = false): void {
    this.erro.set(null);
    const values = this.form.getRawValue();
    const codigo = values.codigo.trim().toUpperCase();
    const valor = this.numeroOpcional(values.valor);
    const valorMinimo = this.numeroOpcional(values.valorMinimo);
    const limiteTotalUso = this.inteiroOpcional(values.limiteTotalUso);
    const limitePorCliente = this.inteiroOpcional(values.limitePorCliente);

    if (!codigo || valor === null || !values.dataInicio) {
      this.erro.set('Informe o código, o valor e a data de início do cupom.');
      return;
    }
    if (!/^[A-Z0-9_-]+$/.test(codigo)) {
      this.erro.set('O código deve conter apenas letras, números, hífen e sublinhado, sem espaços.');
      return;
    }
    if (this.form.controls.idTipoDesconto.value === TIPO_DESCONTO_CUPOM.PERCENTUAL
      ? valor <= 0 || valor > 100
      : valor <= 0) {
      this.erro.set('Informe um desconto válido para o tipo selecionado.');
      return;
    }
    if (values.dataFim && new Date(values.dataFim).getTime() < new Date(values.dataInicio).getTime()) {
      this.erro.set('A data final não pode ser anterior à data de início.');
      return;
    }
    if (limitePorCliente !== null && limiteTotalUso !== null && limitePorCliente > limiteTotalUso) {
      this.erro.set('O limite por cliente não pode superar o limite total.');
      return;
    }

    const dto: CupomDTO = {
      codigo,
      idTipoDesconto: Number(values.idTipoDesconto),
      valor,
      dataInicio: values.dataInicio,
      dataFim: values.dataFim || null,
      valorMinimo,
      limiteTotalUso,
      limitePorCliente,
      ativo: ativar || values.status === 'ATIVO',
      idsCategorias: this.idsCategorias(),
      idsMarcas: this.idsMarcas(),
      idsProdutos: this.idsProdutos(),
    };
    this.salvando.set(true);
    const id = this.cupomId();
    const requisicao: Observable<unknown> = id
      ? this.cupomService.atualizar(id, dto)
      : this.cupomService.criar(dto);
    requisicao.subscribe({
      next: () => {
        this.salvando.set(false);
        void this.router.navigate(['/admin/cupons']);
      },
      error: (err: HttpErrorResponse) => {
        this.salvando.set(false);
        const primeiroErro = err.error?.errors?.[0];
        this.erro.set(primeiroErro?.message ?? err.error?.detail ?? 'Não foi possível salvar o cupom.');
      },
    });
  }

  private carregarCupom(id: number): void {
    this.cupomService.buscarPorId(id).subscribe({
      next: (cupom: Cupom) => {
        this.form.patchValue({
          codigo: cupom.codigo,
          idTipoDesconto: cupom.tipoDesconto.id,
          valor: String(cupom.valor),
          valorMinimo: cupom.valorMinimo == null ? '' : String(cupom.valorMinimo),
          dataInicio: this.dataParaInput(cupom.dataInicio),
          dataFim: cupom.dataFim ? this.dataParaInput(cupom.dataFim) : '',
          limiteTotalUso: cupom.limiteTotalUso == null ? '' : String(cupom.limiteTotalUso),
          limitePorCliente: cupom.limitePorCliente == null ? '' : String(cupom.limitePorCliente),
          status: cupom.ativo ? 'ATIVO' : 'PAUSADO',
        });
        this.idsCategorias.set(cupom.categorias.map((item) => item.id));
        this.idsMarcas.set(cupom.marcas.map((item) => item.id));
        this.produtosSalvos.set(cupom.produtos.map((p) => ({ id: p.id, nome: p.nome ?? 'Produto', sku: p.sku ?? null })));
        this.idsProdutos.set(cupom.produtos.map((item) => item.id));
        this.carregando.set(false);
      },
      error: () => {
        this.erro.set('Não foi possível carregar este cupom.');
        this.carregando.set(false);
      },
    });
  }

  private numeroOpcional(valor: string): number | null {
    if (!valor.trim()) return null;
    const numero = Number(valor.trim().replace(',', '.'));
    return Number.isFinite(numero) ? numero : null;
  }

  private inteiroOpcional(valor: string): number | null {
    const numero = this.numeroOpcional(valor);
    return numero !== null && Number.isInteger(numero) ? numero : null;
  }

  private dataParaInput(valor: string): string {
    return valor?.slice(0, 16) ?? '';
  }
}
