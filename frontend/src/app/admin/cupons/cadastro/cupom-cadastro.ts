import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin, Observable } from 'rxjs';
import { Categoria } from '../../../core/models/categoria.model';
import { Cupom, CupomDTO, TIPO_DESCONTO_CUPOM } from '../../../core/models/cupom.model';
import { Marca } from '../../../core/models/marca.model';
import { VarianteListagem } from '../../../core/models/produto.model';
import { CategoriaService } from '../../../core/services/categoria.service';
import { CupomService } from '../../../core/services/cupom.service';
import { MarcaService } from '../../../core/services/marca.service';
import { VarianteService } from '../../../core/services/variante.service';
import { AdminNav } from '../../../layout/admin/admin-nav/admin-nav';

@Component({
  selector: 'app-cupom-cadastro',
  standalone: true,
  imports: [AdminNav, ReactiveFormsModule, RouterLink],
  templateUrl: './cupom-cadastro.html',
  styleUrl: './cupom-cadastro.scss',
})
export class CupomCadastro implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly cupomService = inject(CupomService);
  private readonly categoriaService = inject(CategoriaService);
  private readonly marcaService = inject(MarcaService);
  private readonly varianteService = inject(VarianteService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly cupomId = signal<number | null>(null);
  readonly carregando = signal(false);
  readonly salvando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly categorias = signal<Categoria[]>([]);
  readonly marcas = signal<Marca[]>([]);
  readonly variantes = signal<VarianteListagem[]>([]);
  readonly idsCategorias = signal<number[]>([]);
  readonly idsMarcas = signal<number[]>([]);
  readonly idsVariantes = signal<number[]>([]);
  readonly modoEdicao = computed(() => this.cupomId() !== null);

  readonly form = this.fb.nonNullable.group({
    codigo: ['', Validators.required],
    idTipoDesconto: [TIPO_DESCONTO_CUPOM.PERCENTUAL as number, Validators.required],
    valor: ['', Validators.required],
    valorMinimo: [''],
    descricao: [''],
    dataInicio: ['', Validators.required],
    dataFim: [''],
    limiteTotalUso: [''],
    limitePorCliente: [''],
    primeiroPedido: [false],
    acumulaPromocoes: [false],
    ativo: [true],
    exibirOfertas: [false],
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
      variantes: this.varianteService.listar({ ativa: true }, 0, 1000),
    }).subscribe({
      next: ({ categorias, marcas, variantes }) => {
        this.categorias.set(categorias.items ?? []);
        this.marcas.set(marcas.items ?? []);
        this.variantes.set(variantes.items ?? []);
        if (this.cupomId() !== null) this.carregarCupom(this.cupomId()!);
      },
      error: () => {
        this.erro.set('Não foi possível carregar as opções de categoria, marca e variante.');
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

  aoSelecionar(event: Event, destino: 'categorias' | 'marcas' | 'variantes'): void {
    const select = event.target as HTMLSelectElement;
    const ids = Array.from(select.selectedOptions).map((option) => Number(option.value));
    if (destino === 'categorias') this.idsCategorias.set(ids);
    if (destino === 'marcas') this.idsMarcas.set(ids);
    if (destino === 'variantes') this.idsVariantes.set(ids);
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

  idsParaSelect(ids: number[]): string[] {
    return ids.map((id) => String(id));
  }

  moeda(valor: number): string {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(valor);
  }

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
      ativo: ativar || values.ativo,
      idsCategorias: this.idsCategorias(),
      idsMarcas: this.idsMarcas(),
      idsVariantes: this.idsVariantes(),
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
          ativo: cupom.ativo,
        });
        this.idsCategorias.set(cupom.categorias.map((item) => item.id));
        this.idsMarcas.set(cupom.marcas.map((item) => item.id));
        this.idsVariantes.set(cupom.variantes.map((item) => item.id));
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
