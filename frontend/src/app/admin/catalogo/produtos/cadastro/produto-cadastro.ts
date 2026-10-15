import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { AdminNav } from '../../../../layout/admin/admin-nav/admin-nav';
import { ProdutoService } from '../../../../core/services/produto.service';
import { DroneService } from '../../../../core/services/drone.service';
import { AcessorioService } from '../../../../core/services/acessorio.service';
import { MarcaService } from '../../../../core/services/marca.service';
import { CategoriaService } from '../../../../core/services/categoria.service';
import { SubcategoriaService } from '../../../../core/services/subcategoria.service';
import { FornecedorService } from '../../../../core/services/fornecedor.service';
import {
  CompativeisAcessorio,
  VinculoCompativel,
  rotuloTipo,
} from './compativeis/compativeis-acessorio';
import {
  AcessorioDTO,
  DroneDTO,
  Produto,
  ProdutoDTO,
  ProdutoResumo,
  TIPO_PRODUTO,
} from '../../../../core/models/produto.model';
import { STATUS_FORNECEDOR, STATUS_PRODUTO } from '../../../../core/models/status.model';
import { Marca } from '../../../../core/models/marca.model';
import { Categoria } from '../../../../core/models/categoria.model';
import { Subcategoria } from '../../../../core/models/subcategoria.model';
import { Fornecedor } from '../../../../core/models/fornecedor.model';
import { ProblemDetail } from '../../../../core/models/problem.model';

/** Opções dos selects de spec, como no protótipo. O valor salvo é texto livre na API. */
const OPCOES = {
  sensor: ['CMOS 1/1.3"', 'CMOS 1"', 'CMOS 4/3'],
  video: ['4K 60 fps', '4K 30 fps', '5.1K 50 fps'],
  estabilizacao: ['Gimbal mecânico 3 eixos', 'Gimbal 2 eixos', 'Eletrônica'],
  bateria: ['LiPo 4S', 'LiPo 3S', 'Li-ion'],
  classeAnac: ['Classe 3', 'Classe 2', 'Classe 1'],
  tipoAcessorio: [
    'Bateria',
    'Hélice',
    'Carregador',
    'Maleta / mochila',
    'Filtro',
    'Cartão de memória',
    'Peça de reposição',
  ],
} as const;

type CampoOpcao = keyof typeof OPCOES;

/**
 * Cadastro de drone e de acessório (D27). A rota diz o tipo (data.tipo): drone mostra
 * as specs e salva em /drones; acessório mostra o tipo de acessório e salva em /acessorios.
 */
@Component({
  selector: 'app-produto-cadastro',
  imports: [ReactiveFormsModule, RouterLink, AdminNav, CompativeisAcessorio],
  templateUrl: './produto-cadastro.html',
})
export class ProdutoCadastro implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly produtoService = inject(ProdutoService);
  private readonly droneService = inject(DroneService);
  private readonly acessorioService = inject(AcessorioService);
  private readonly marcaService = inject(MarcaService);
  private readonly categoriaService = inject(CategoriaService);
  private readonly subcategoriaService = inject(SubcategoriaService);
  private readonly fornecedorService = inject(FornecedorService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly STATUS = STATUS_PRODUTO;
  readonly FORNECEDOR_INATIVO = STATUS_FORNECEDOR.INATIVO;

  readonly tipo: number = this.route.snapshot.data['tipo'] ?? TIPO_PRODUTO.DRONE;
  readonly ehDrone = this.tipo === TIPO_PRODUTO.DRONE;
  private readonly rotaListagem = '/admin/produtos';

  readonly produtoId = signal<number | null>(null);
  readonly modoEdicao = computed(() => this.produtoId() !== null);
  readonly titulo = computed(
    () => `${this.modoEdicao() ? 'Editar' : 'Cadastrar'} ${this.ehDrone ? 'drone' : 'acessório'}`,
  );

  readonly marcas = signal<Marca[]>([]);
  readonly categorias = signal<Categoria[]>([]);
  readonly subcategorias = signal<Subcategoria[]>([]);
  readonly fornecedores = signal<Fornecedor[]>([]);
  readonly categoriaSelecionada = signal<number | null>(null);

  /** Subcategorias da categoria escolhida no select de cima. */
  readonly subcategoriasDisponiveis = computed(() =>
    this.subcategorias().filter((s) => s.categoria.id === this.categoriaSelecionada()),
  );

  readonly carregando = signal(false);
  readonly salvando = signal(false);
  readonly excluindo = signal(false);
  readonly erroGeral = signal<string | null>(null);
  readonly errosCampo = signal<Record<string, string>>({});

  readonly erroNoSeo = computed(() =>
    ['slug', 'metaTitle', 'metaDescription', 'tags'].some((c) => c in this.errosCampo()),
  );

  readonly form = this.fb.group({
    nome: this.fb.nonNullable.control(''),
    modelo: this.fb.nonNullable.control(''),
    descricaoCurta: this.fb.nonNullable.control(''),
    descricao: this.fb.nonNullable.control(''),
    slug: this.fb.nonNullable.control(''),
    metaTitle: this.fb.nonNullable.control(''),
    metaDescription: this.fb.nonNullable.control(''),
    tags: this.fb.nonNullable.control(''),
    garantiaMeses: this.fb.control<number | null>(null),
    dataLancamento: this.fb.nonNullable.control(''),
    idMarca: this.fb.control<number | null>(null),
    // idCategoria só ajuda a escolher; o que vai para a API é idSubcategoria
    idCategoria: this.fb.control<number | null>(null),
    idSubcategoria: this.fb.control<number | null>(null),
    idFornecedor: this.fb.control<number | null>(null),
    idStatus: this.fb.nonNullable.control<number>(STATUS_PRODUTO.RASCUNHO, Validators.required),
    destaque: this.fb.nonNullable.control(false),
  });

  // specs do drone (D14); alcance é digitado em km e vai em metros para a API
  readonly specs = this.fb.group({
    pesoGramas: this.fb.control<number | null>(null),
    dimensoesDobrado: this.fb.nonNullable.control(''),
    dimensoesAberto: this.fb.nonNullable.control(''),
    autonomiaMinutos: this.fb.control<number | null>(null),
    alcanceKm: this.fb.control<number | null>(null),
    velocidadeMaximaKmh: this.fb.control<number | null>(null),
    resolucaoFotoMp: this.fb.control<number | null>(null),
    tipoSensor: this.fb.nonNullable.control(''),
    resolucaoVideo: this.fb.nonNullable.control(''),
    estabilizacao: this.fb.nonNullable.control(''),
    tipoBateria: this.fb.nonNullable.control(''),
    capacidadeBateriaMah: this.fb.control<number | null>(null),
    tempoCarregamentoMinutos: this.fb.control<number | null>(null),
    gpsIntegrado: this.fb.nonNullable.control(false),
    sensoresAnticolisao: this.fb.nonNullable.control(false),
    exigeRegistroAnac: this.fb.nonNullable.control(false),
    classeAnac: this.fb.nonNullable.control(''),
    registroSisant: this.fb.nonNullable.control(''),
  });

  readonly tipoAcessorio = this.fb.nonNullable.control('');

  // compatibilidade (D17): o acessório grava a lista; os dois tipos mostram o lado recíproco
  readonly compativeis = signal<VinculoCompativel[]>([]);
  readonly acessoriosCompativeis = signal<ProdutoResumo[]>([]);
  readonly rotuloTipo = rotuloTipo;

  /** Opções do select, mais o valor salvo quando ele não estiver na lista. */
  opcoes(campo: CampoOpcao): readonly string[] {
    const atual = campo === 'tipoAcessorio' ? this.tipoAcessorio.value : this.valorSpec(campo);
    const lista: readonly string[] = OPCOES[campo];
    return atual && !lista.includes(atual) ? [...lista, atual] : lista;
  }

  private valorSpec(campo: Exclude<CampoOpcao, 'tipoAcessorio'>): string {
    const mapa = {
      sensor: this.specs.controls.tipoSensor,
      video: this.specs.controls.resolucaoVideo,
      estabilizacao: this.specs.controls.estabilizacao,
      bateria: this.specs.controls.tipoBateria,
      classeAnac: this.specs.controls.classeAnac,
    };
    return mapa[campo].value;
  }

  ngOnInit(): void {
    this.marcaService
      .listar(0, 1000)
      .subscribe((p) => this.marcas.set(p.items.filter((x) => x.nome)));
    // só as categorias do tipo desta tela: drone não entra em categoria de acessório
    this.categoriaService
      .listar(0, 1000)
      .subscribe((p) =>
        this.categorias.set(p.items.filter((x) => x.nome && x.tipo?.id === this.tipo)),
      );
    this.subcategoriaService
      .listar(null, 0, 1000)
      .subscribe((p) => this.subcategorias.set(p.items));
    this.fornecedorService.listar(0, 1000).subscribe((p) => this.fornecedores.set(p.items));

    // ao trocar a categoria, limpa a subcategoria se ela for de outra categoria
    this.form.controls.idCategoria.valueChanges.subscribe((id) => {
      const idCategoria = id ? Number(id) : null;
      this.categoriaSelecionada.set(idCategoria);
      const sub = this.subcategorias().find(
        (s) => s.id === this.form.controls.idSubcategoria.value,
      );
      if (sub && sub.categoria.id !== idCategoria) this.form.controls.idSubcategoria.setValue(null);
    });

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = Number(idParam);
      this.produtoId.set(id);
      this.carregarProduto(id);
    }
  }

  private carregarProduto(id: number): void {
    this.carregando.set(true);
    this.produtoService.buscarPorId(id).subscribe({
      next: (produto) => {
        if (produto.tipo.id !== this.tipo) {
          this.erroGeral.set(
            `Este produto é ${produto.tipo.id === TIPO_PRODUTO.DRONE ? 'um drone' : 'um acessório'}; abra pela listagem de produtos.`,
          );
          this.carregando.set(false);
          return;
        }
        this.preencherTipo(produto);
        this.form.patchValue({
          nome: produto.nome ?? '',
          modelo: produto.modelo ?? '',
          descricaoCurta: produto.descricaoCurta ?? '',
          descricao: produto.descricao ?? '',
          slug: produto.slug,
          metaTitle: produto.metaTitle ?? '',
          metaDescription: produto.metaDescription ?? '',
          tags: produto.tags ?? '',
          garantiaMeses: produto.garantiaMeses,
          dataLancamento: produto.dataLancamento ?? '',
          idMarca: produto.marca?.id ?? null,
          idCategoria: produto.subcategoria?.categoria.id ?? null,
          idSubcategoria: produto.subcategoria?.id ?? null,
          idFornecedor: produto.fornecedor?.id ?? null,
          idStatus: produto.status.id,
          destaque: produto.destaque,
        });
        this.carregando.set(false);
      },
      error: (err) => {
        this.erroGeral.set(
          err.status === 404
            ? 'Este produto não foi encontrado.'
            : 'Não foi possível carregar este produto.',
        );
        this.carregando.set(false);
      },
    });
  }

  private preencherTipo(produto: Produto): void {
    this.acessoriosCompativeis.set(produto.acessoriosCompativeis ?? []);
    if (!this.ehDrone) {
      this.compativeis.set((produto.compativeis ?? []).map((p) => ({ produto: p, salvo: true })));
      this.tipoAcessorio.setValue(produto.tipoAcessorio ?? '');
      return;
    }
    this.specs.patchValue({
      pesoGramas: produto.pesoGramas ?? null,
      dimensoesDobrado: produto.dimensoesDobrado ?? '',
      dimensoesAberto: produto.dimensoesAberto ?? '',
      autonomiaMinutos: produto.autonomiaMinutos ?? null,
      alcanceKm: produto.alcanceMetros == null ? null : produto.alcanceMetros / 1000,
      velocidadeMaximaKmh: produto.velocidadeMaximaKmh ?? null,
      resolucaoFotoMp: produto.resolucaoFotoMp ?? null,
      tipoSensor: produto.tipoSensor ?? '',
      resolucaoVideo: produto.resolucaoVideo ?? '',
      estabilizacao: produto.estabilizacao ?? '',
      tipoBateria: produto.tipoBateria ?? '',
      capacidadeBateriaMah: produto.capacidadeBateriaMah ?? null,
      tempoCarregamentoMinutos: produto.tempoCarregamentoMinutos ?? null,
      gpsIntegrado: produto.gpsIntegrado ?? false,
      sensoresAnticolisao: produto.sensoresAnticolisao ?? false,
      exigeRegistroAnac: produto.exigeRegistroAnac ?? false,
      classeAnac: produto.classeAnac ?? '',
      registroSisant: produto.registroSisant ?? '',
    });
  }

  /** Rota de edição do produto citado, conforme o tipo dele. */
  rotaEdicao(produto: ProdutoResumo): string {
    const base = produto.tipo.id === TIPO_PRODUTO.DRONE ? 'drones' : 'acessorios';
    return `/admin/${base}/${produto.id}/editar`;
  }

  campoInvalido(campo: string): boolean {
    return campo in this.errosCampo();
  }

  mensagemErro(campo: string): string | null {
    return this.errosCampo()[campo] ?? null;
  }

  salvar(): void {
    this.erroGeral.set(null);
    this.errosCampo.set({});

    const v = this.form.getRawValue();
    const dto: ProdutoDTO = {
      nome: v.nome.trim(),
      modelo: v.modelo.trim(),
      descricaoCurta: v.descricaoCurta.trim(),
      descricao: v.descricao.trim(),
      slug: v.slug.trim(),
      metaTitle: v.metaTitle.trim(),
      metaDescription: v.metaDescription.trim(),
      tags: v.tags.trim(),
      garantiaMeses: numero(v.garantiaMeses),
      dataLancamento: v.dataLancamento || null,
      // o select devolve string; a API espera Long
      idMarca: v.idMarca ? Number(v.idMarca) : null,
      idSubcategoria: v.idSubcategoria ? Number(v.idSubcategoria) : null,
      idFornecedor: v.idFornecedor ? Number(v.idFornecedor) : null,
      idStatus: Number(v.idStatus),
      destaque: v.destaque,
    };

    this.salvando.set(true);
    const id = this.produtoId();
    let request$: Observable<unknown>;
    if (this.ehDrone) {
      const drone = this.droneDTO(dto);
      request$ = id ? this.droneService.atualizar(id, drone) : this.droneService.criar(drone);
    } else {
      const acessorio: AcessorioDTO = {
        ...dto,
        tipoAcessorio: this.tipoAcessorio.value,
        idsCompativeis: this.compativeis().map((v) => v.produto.id),
      };
      request$ = id
        ? this.acessorioService.atualizar(id, acessorio)
        : this.acessorioService.criar(acessorio);
    }

    request$.subscribe({
      next: () => {
        this.salvando.set(false);
        this.router.navigate([this.rotaListagem]);
      },
      error: (err: HttpErrorResponse) => this.tratarErro(err),
    });
  }

  private droneDTO(comum: ProdutoDTO): DroneDTO {
    const s = this.specs.getRawValue();
    const alcanceKm = numero(s.alcanceKm);
    return {
      ...comum,
      pesoGramas: numero(s.pesoGramas),
      dimensoesDobrado: s.dimensoesDobrado.trim(),
      dimensoesAberto: s.dimensoesAberto.trim(),
      autonomiaMinutos: numero(s.autonomiaMinutos),
      alcanceMetros: alcanceKm === null ? null : Math.round(alcanceKm * 1000),
      velocidadeMaximaKmh: numero(s.velocidadeMaximaKmh),
      resolucaoFotoMp: numero(s.resolucaoFotoMp),
      tipoSensor: s.tipoSensor,
      resolucaoVideo: s.resolucaoVideo,
      estabilizacao: s.estabilizacao,
      tipoBateria: s.tipoBateria,
      capacidadeBateriaMah: numero(s.capacidadeBateriaMah),
      tempoCarregamentoMinutos: numero(s.tempoCarregamentoMinutos),
      gpsIntegrado: s.gpsIntegrado,
      sensoresAnticolisao: s.sensoresAnticolisao,
      exigeRegistroAnac: s.exigeRegistroAnac,
      classeAnac: s.classeAnac,
      registroSisant: s.registroSisant.trim(),
    };
  }

  excluir(): void {
    const id = this.produtoId();
    if (id === null) return;
    if (!confirm('Excluir este produto? Essa ação não pode ser desfeita.')) return;

    this.excluindo.set(true);
    this.produtoService.excluir(id).subscribe({
      next: () => this.router.navigate([this.rotaListagem]),
      error: (err: HttpErrorResponse) => {
        this.excluindo.set(false);
        // 409: produto em uso (D28); o detail explica e sugere inativar
        const problem = err.error as ProblemDetail | undefined;
        this.erroGeral.set(problem?.detail ?? 'Não foi possível excluir este produto.');
      },
    });
  }

  private tratarErro(err: HttpErrorResponse): void {
    this.salvando.set(false);
    const problem = err.error as ProblemDetail | undefined;
    if (problem?.errors?.length) {
      const mapa: Record<string, string> = {};
      for (const fe of problem.errors) mapa[fe.field] = fe.message;
      this.errosCampo.set(mapa);
      this.erroGeral.set(problem.detail ?? 'Verifique os campos destacados.');
    } else {
      this.erroGeral.set(problem?.detail ?? 'Não foi possível salvar o produto. Tente novamente.');
    }
  }
}

/** Campo numérico do form: vazio vira null (o input type=number pode devolver ''). */
function numero(valor: number | string | null): number | null {
  return valor === null || `${valor}` === '' ? null : Number(valor);
}
