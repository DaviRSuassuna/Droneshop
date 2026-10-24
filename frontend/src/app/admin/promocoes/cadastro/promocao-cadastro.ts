import { STATUS_PRODUTO } from '../../../core/models/status.model';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { Produto } from '../../../core/models/produto.model';
import {
  ESCOPO_PROMOCAO,
  Promocao,
  PromocaoDTO,
} from '../../../core/models/promocao.model';
import { PromocaoService } from '../../../core/services/promocao.service';
import { ProdutoService } from '../../../core/services/produto.service';

type StatusFormulario = 'RASCUNHO' | 'AGENDADA' | 'ATIVA' | 'ENCERRADA';

/** Produto da tabela "Produtos em promoção": o que a tela mostra (o salvo pode ter saído do select). */
interface ProdutoEmPromocao {
  id: number;
  nome: string | null;
  sku: string | null;
  preco: number | null;
}

/**
 * Cadastro de promoção (#14). O select de produto lista só ATIVOS (D40), kits inclusive.
 * Prioridade, chamada, teto, "encerrar sem estoque" e os destaques de vitrine do protótipo
 * ainda não têm API e ficam de fora (ver e2e/conformidade/telas.ts).
 */
@Component({
  selector: 'app-promocao-cadastro',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './promocao-cadastro.html',
})
export class PromocaoCadastro implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly promocaoService = inject(PromocaoService);
  private readonly produtoService = inject(ProdutoService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly promocaoId = signal<number | null>(null);
  readonly carregando = signal(false);
  readonly salvando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly produtosDisponiveis = signal<Produto[]>([]);
  // os já salvos na promoção, para a tabela mostrar mesmo um produto que saiu do select
  private readonly produtosSalvos = signal<ProdutoEmPromocao[]>([]);
  readonly idsProdutos = signal<number[]>([]);
  readonly escolhendoProduto = signal(false);
  readonly modoEdicao = computed(() => this.promocaoId() !== null);
  readonly produtosSelecionados = computed<ProdutoEmPromocao[]>(() =>
    this.idsProdutos().map(
      (id) =>
        this.produtosDisponiveis().find((p) => p.id === id) ??
        this.produtosSalvos().find((p) => p.id === id) ?? { id, nome: 'Produto indisponível', sku: null, preco: null },
    ),
  );
  readonly opcoesLivres = computed(() => {
    const escolhidos = new Set(this.idsProdutos());
    return this.produtosDisponiveis().filter((p) => !escolhidos.has(p.id));
  });

  readonly ESCOPO = ESCOPO_PROMOCAO;

  readonly escopos = [
    { id: ESCOPO_PROMOCAO.PRODUTO, nome: 'Produto · desconta itens selecionados' },
    { id: ESCOPO_PROMOCAO.CARRINHO, nome: 'Carrinho · desconta o total' },
    { id: ESCOPO_PROMOCAO.FRETE, nome: 'Frete · zera ou reduz o frete' },
  ];

  readonly form = this.fb.nonNullable.group({
    nome: ['', Validators.required],
    idEscopo: [ESCOPO_PROMOCAO.PRODUTO as number, Validators.required],
    percentualDesconto: [''],
    valorDesconto: [''],
    valorMinimoCarrinho: [''],
    dataInicio: ['', Validators.required],
    dataFim: ['', Validators.required],
    status: ['RASCUNHO' as StatusFormulario],
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.promocaoId.set(Number(idParam));
      this.carregando.set(true);
    }

    this.produtoService.listar({ idStatus: STATUS_PRODUTO.ATIVO }, 0, 1000).subscribe({
      next: (pagina) => {
        this.produtosDisponiveis.set(pagina.items ?? []);
        if (this.promocaoId() !== null) this.carregarPromocao(this.promocaoId()!);
      },
      error: () => {
        this.erro.set('Não foi possível carregar os produtos disponíveis.');
        this.carregando.set(false);
      },
    });
  }

  pendencias(): string[] {
    const v = this.form.getRawValue();
    const faltando: string[] = [];
    if (!v.nome.trim()) faltando.push('Nome da promoção');
    if (!v.dataInicio) faltando.push('Data de início');
    if (!v.dataFim) faltando.push('Data de fim');
    if (
      Number(v.idEscopo) === ESCOPO_PROMOCAO.PRODUTO &&
      this.idsProdutos().length === 0
    ) {
      faltando.push('Produtos em promoção');
    }
    return faltando;
  }

  aoMudarEscopo(valor: string): void {
    this.form.controls.idEscopo.setValue(Number(valor));
  }

  adicionarProduto(evento: Event): void {
    const select = evento.target as HTMLSelectElement;
    const id = Number(select.value);
    select.value = '';
    if (!id || this.idsProdutos().includes(id)) return;
    this.idsProdutos.update((ids) => [...ids, id]);
    this.escolhendoProduto.set(false);
  }

  removerProduto(id: number): void {
    this.idsProdutos.update((ids) => ids.filter((item) => item !== id));
  }

  moeda(valor: number | null): string {
    if (valor == null) return '—';
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(valor);
  }

  precoPromocional(produto: ProdutoEmPromocao): number | null {
    const valor = this.form.controls.valorDesconto.value.trim();
    const percentual = this.form.controls.percentualDesconto.value.trim();
    const preco = produto.preco;
    if (preco == null) return null;
    if (percentual) return Math.max(0, preco * (1 - Number(percentual) / 100));
    if (valor) return Math.max(0, preco - Number(valor));
    return null;
  }

  salvar(publicar = false): void {
    this.erro.set(null);
    const v = this.form.getRawValue();
    const nome = v.nome.trim();
    const percentual = this.numeroOpcional(v.percentualDesconto);
    const valor = this.numeroOpcional(v.valorDesconto);

    if (!nome || !v.dataInicio || !v.dataFim) {
      this.erro.set('Informe o nome e o período da promoção.');
      return;
    }
    if (percentual !== null && valor !== null) {
      this.erro.set('Preencha percentual OU valor fixo, nunca os dois.');
      return;
    }
    if (percentual === null && valor === null) {
      this.erro.set('Informe o percentual ou o valor fixo do desconto.');
      return;
    }
    if (new Date(v.dataFim).getTime() <= new Date(v.dataInicio).getTime()) {
      this.erro.set('A data de fim deve ser posterior à data de início.');
      return;
    }
    if (Number(v.idEscopo) === ESCOPO_PROMOCAO.PRODUTO && this.idsProdutos().length === 0) {
      this.erro.set('Adicione ao menos um produto para promoções com escopo Produto.');
      return;
    }

    const dto: PromocaoDTO = {
      nome,
      idEscopo: Number(v.idEscopo),
      dataInicio: v.dataInicio,
      dataFim: v.dataFim,
      percentualDesconto: percentual,
      valorDesconto: valor,
      valorMinimoCarrinho: this.numeroOpcional(v.valorMinimoCarrinho),
      ativa: publicar || v.status === 'ATIVA' || v.status === 'AGENDADA',
      idsProdutos: Number(v.idEscopo) === ESCOPO_PROMOCAO.PRODUTO ? this.idsProdutos() : [],
    };

    this.salvando.set(true);
    const id = this.promocaoId();
    const request$: Observable<unknown> = id
      ? this.promocaoService.atualizar(id, dto)
      : this.promocaoService.criar(dto);
    request$.subscribe({
      next: () => {
        this.salvando.set(false);
        void this.router.navigate(['/admin/promocoes']);
      },
      error: (err: HttpErrorResponse) => {
        this.salvando.set(false);
        this.erro.set(err.error?.detail ?? 'Não foi possível salvar a promoção. Tente novamente.');
      },
    });
  }

  private carregarPromocao(id: number): void {
    this.promocaoService.buscarPorId(id).subscribe({
      next: (promocao) => {
        this.form.patchValue({
          nome: promocao.nome,
          idEscopo: promocao.escopo.id,
          percentualDesconto: promocao.percentualDesconto?.toString() ?? '',
          valorDesconto: promocao.valorDesconto?.toString() ?? '',
          valorMinimoCarrinho: promocao.valorMinimoCarrinho?.toString() ?? '',
          dataInicio: this.dataParaInput(promocao.dataInicio),
          dataFim: this.dataParaInput(promocao.dataFim),
          status: this.statusFormulario(promocao),
        });
        this.produtosSalvos.set((promocao.produtos ?? []).map((p) => ({ ...p, preco: null })));
        this.idsProdutos.set((promocao.produtos ?? []).map((p) => p.id));
        this.carregando.set(false);
      },
      error: () => {
        this.erro.set('Não foi possível carregar esta promoção.');
        this.carregando.set(false);
      },
    });
  }

  private statusFormulario(promocao: Promocao): StatusFormulario {
    if (promocao.situacao === 'VIGENTE') return 'ATIVA';
    if (promocao.situacao === 'AGENDADA') return 'AGENDADA';
    if (promocao.situacao === 'ENCERRADA') return 'ENCERRADA';
    return 'RASCUNHO';
  }

  private dataParaInput(valor: string): string {
    return valor?.slice(0, 16) ?? '';
  }

  private numeroOpcional(valor: string): number | null {
    const normalizado = valor.trim().replace(',', '.');
    if (!normalizado) return null;
    const numero = Number(normalizado);
    return Number.isFinite(numero) ? numero : null;
  }
}
