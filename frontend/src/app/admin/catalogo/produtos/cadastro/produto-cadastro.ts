import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Location } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Observable, startWith } from 'rxjs';
import { ProdutoService } from '../../../../core/services/produto.service';
import { DroneService } from '../../../../core/services/drone.service';
import { AcessorioService } from '../../../../core/services/acessorio.service';
import { KitService } from '../../../../core/services/kit.service';
import { MarcaService } from '../../../../core/services/marca.service';
import { CategoriaService } from '../../../../core/services/categoria.service';
import { SubcategoriaService } from '../../../../core/services/subcategoria.service';
import { FornecedorService } from '../../../../core/services/fornecedor.service';
import { GrupoProdutoService } from '../../../../core/services/grupo-produto.service';
import {
  CompativeisAcessorio,
  VinculoCompativel,
  rotuloTipo,
} from './compativeis/compativeis-acessorio';
import {
  AcessorioDTO,
  DroneDTO,
  KitDTO,
  Produto,
  ProdutoDTO,
  ProdutoResumo,
  TIPO_PRODUTO,
} from '../../../../core/models/produto.model';
import { GrupoProduto } from '../../../../core/models/grupo-produto.model';
import { STATUS_FORNECEDOR, STATUS_PRODUTO } from '../../../../core/models/status.model';
import { Marca } from '../../../../core/models/marca.model';
import { Categoria } from '../../../../core/models/categoria.model';
import { Subcategoria } from '../../../../core/models/subcategoria.model';
import { Fornecedor } from '../../../../core/models/fornecedor.model';
import { ProblemDetail } from '../../../../core/models/problem.model';
import { ProdutoMidias } from './midias/produto-midias';
import { ProdutoMidiasExtras } from './midias/produto-midias-extras';
import { ProdutoMidiasEstado } from './midias/produto-midias-estado';

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

/** Produto que pode ser componente do kit, com o que a tela mostra e usa para derivar a embalagem. */
interface OpcaoComponente {
  id: number;
  nome: string | null;
  sku: string | null;
  tipo: string;
  disponivel: number;
  imagemPrincipal?: string | null;
  pesoEmbalagemKg: number | null;
  alturaEmbalagemCm: number | null;
  larguraEmbalagemCm: number | null;
  comprimentoEmbalagemCm: number | null;
}

interface LinhaComponente {
  produto: OpcaoComponente;
  quantidade: number;
}

/** Linha do seletor "Versão" (outras versões do grupo e este produto). */
interface VersaoTela {
  id: number | null;
  nome: string;
  rotulo: string;
  tipo: string;
  preco: number | null;
  imagemPrincipal: string | null;
  este: boolean;
}

/**
 * Cadastro de drone, acessório e kit (D27, D40). A rota diz o tipo (data.tipo): drone mostra a
 * ficha técnica e salva em /drones; acessório mostra o tipo e a compatibilidade e salva em
 * /acessorios; kit mostra os componentes, não tem estoque próprio e salva em /kits.
 */
@Component({
  selector: 'app-produto-cadastro',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    CompativeisAcessorio,
    ProdutoMidias,
    ProdutoMidiasExtras,
  ],
  templateUrl: './produto-cadastro.html',
  providers: [ProdutoMidiasEstado],
})
export class ProdutoCadastro implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly produtoService = inject(ProdutoService);
  private readonly droneService = inject(DroneService);
  private readonly acessorioService = inject(AcessorioService);
  private readonly kitService = inject(KitService);
  private readonly marcaService = inject(MarcaService);
  private readonly categoriaService = inject(CategoriaService);
  private readonly subcategoriaService = inject(SubcategoriaService);
  private readonly fornecedorService = inject(FornecedorService);
  private readonly grupoService = inject(GrupoProdutoService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly location = inject(Location);
  readonly midias = inject(ProdutoMidiasEstado);

  readonly STATUS = STATUS_PRODUTO;
  readonly FORNECEDOR_INATIVO = STATUS_FORNECEDOR.INATIVO;

  readonly tipo: number = this.route.snapshot.data['tipo'] ?? TIPO_PRODUTO.DRONE;
  readonly ehDrone = this.tipo === TIPO_PRODUTO.DRONE;
  readonly ehAcessorio = this.tipo === TIPO_PRODUTO.ACESSORIO;
  readonly ehKit = this.tipo === TIPO_PRODUTO.KIT;
  readonly nomeTipo = this.ehDrone ? 'drone' : this.ehKit ? 'kit' : 'acessório';
  private readonly rotaTipo = this.ehDrone ? 'drones' : this.ehKit ? 'kits' : 'acessorios';
  private readonly rotaListagem = '/admin/produtos';

  readonly produtoId = signal<number | null>(null);
  readonly modoEdicao = computed(() => this.produtoId() !== null);
  readonly titulo = computed(
    () => `${this.modoEdicao() ? 'Editar' : 'Cadastrar'} ${this.nomeTipo}`,
  );
  readonly trilha = computed(() =>
    this.modoEdicao()
      ? `Editar ${this.nomeTipo}`
      : this.ehDrone
        ? 'Novo item'
        : `Novo ${this.nomeTipo}`,
  );
  readonly rotuloPublicar = this.ehDrone ? 'Publicar' : `Publicar ${this.nomeTipo}`;

  readonly marcas = signal<Marca[]>([]);
  readonly categorias = signal<Categoria[]>([]);
  readonly subcategorias = signal<Subcategoria[]>([]);
  readonly fornecedores = signal<Fornecedor[]>([]);
  readonly grupos = signal<GrupoProduto[]>([]);

  readonly carregando = signal(false);
  readonly salvando = signal(false);
  readonly excluindo = signal(false);
  readonly erroGeral = signal<string | null>(null);
  readonly errosCampo = signal<Record<string, string>>({});

  readonly form = this.fb.group({
    nome: this.fb.nonNullable.control(''),
    sku: this.fb.nonNullable.control(''),
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
    idStatus: this.fb.nonNullable.control<number>(STATUS_PRODUTO.RASCUNHO),
    destaque: this.fb.nonNullable.control(false),
    tipoAcessorio: this.fb.nonNullable.control(''),
    pesoItemG: this.fb.control<number | null>(null),
    sugerirNosCompativeis: this.fb.nonNullable.control(false),
    exibirEmOfertas: this.fb.nonNullable.control(false),
  });

  // preço, estoque e embalagem do próprio produto (D40); a caixa é digitada como A×L×C
  readonly dadosComerciais = this.fb.group({
    preco: this.fb.control<number | null>(null),
    custoAquisicao: this.fb.control<number | null>(null),
    estoqueFisico: this.fb.control<number | null>(null),
    estoqueMinimo: this.fb.control<number | null>(null),
    pesoEmbalagemKg: this.fb.control<number | null>(null),
    dimensoes: this.fb.nonNullable.control(''),
    // kit: sem sobrescrita, peso e dimensões vêm dos componentes (D16)
    sobrescreverEmbalagem: this.fb.nonNullable.control(false),
  });

  readonly pagina = this.fb.group({
    idGrupo: this.fb.control<number | null>(null),
    rotuloNoGrupo: this.fb.nonNullable.control(''),
    ordemNoGrupo: this.fb.control<number | null>(null),
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

  // valores do form como signal, para o que a tela calcula ao vivo (margem, pendências, versões)
  private readonly valores = toSignal(
    this.form.valueChanges.pipe(startWith(this.form.getRawValue())),
    {
      requireSync: true,
    },
  );
  private readonly valoresComerciais = toSignal(
    this.dadosComerciais.valueChanges.pipe(startWith(this.dadosComerciais.getRawValue())),
    { requireSync: true },
  );
  private readonly valoresPagina = toSignal(
    this.pagina.valueChanges.pipe(startWith(this.pagina.getRawValue())),
    { requireSync: true },
  );
  private readonly valoresSpecs = toSignal(
    this.specs.valueChanges.pipe(startWith(this.specs.getRawValue())),
    { requireSync: true },
  );

  /** Subcategorias da categoria escolhida no select de cima. */
  readonly subcategoriasDisponiveis = computed(() => {
    const idCategoria = numero(this.valores().idCategoria ?? null);
    return this.subcategorias().filter((s) => s.categoria?.id === idCategoria);
  });

  // compatibilidade (D17): o acessório grava a lista; todos mostram o lado recíproco
  readonly compativeis = signal<VinculoCompativel[]>([]);
  readonly acessoriosCompativeis = signal<ProdutoResumo[]>([]);
  readonly rotuloTipo = rotuloTipo;

  // kit (D7): componentes escolhidos e os produtos que podem entrar (ATIVOS, sem kit)
  readonly componentes = signal<LinhaComponente[]>([]);
  readonly opcoesComponente = signal<OpcaoComponente[]>([]);
  readonly escolhendoComponente = signal(false);
  readonly opcoesLivres = computed(() => {
    const escolhidos = new Set(this.componentes().map((c) => c.produto.id));
    return this.opcoesComponente().filter((o) => !escolhidos.has(o.id));
  });
  readonly unidadesNoKit = computed(() =>
    this.componentes().reduce((t, c) => t + (c.quantidade || 0), 0),
  );
  /** Peso e dimensões que o backend derivaria dos componentes (D16); nulo se falta medida. */
  readonly embalagemDerivada = computed(() => derivarEmbalagem(this.componentes()));
  /** Mínimo entre (disponível do componente ÷ quantidade), como o backend calcula (D7). */
  readonly disponivelKit = computed(() => {
    let menor: number | null = null;
    for (const c of this.componentes()) {
      if (!c.quantidade || c.quantidade < 1) continue;
      const kits = Math.floor(c.produto.disponivel / c.quantidade);
      menor = menor === null ? kits : Math.min(menor, kits);
    }
    return menor ?? 0;
  });
  readonly limitante = computed(() => {
    const disponivel = this.disponivelKit();
    return this.componentes().find(
      (c) => c.quantidade > 0 && Math.floor(c.produto.disponivel / c.quantidade) === disponivel,
    );
  });

  readonly formatoMoeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
  private readonly formatoPercentual = new Intl.NumberFormat('pt-BR', {
    minimumFractionDigits: 1,
    maximumFractionDigits: 1,
  });

  /** Margem ao vivo, com a mesma fórmula do backend (D26): (preço − custo) ÷ preço. */
  readonly margem = computed(() => {
    const v = this.valoresComerciais();
    const preco = numero(v.preco ?? null);
    const custo = numero(v.custoAquisicao ?? null);
    if (preco === null || custo === null || preco <= 0) return null;
    return ((preco - custo) / preco) * 100;
  });
  readonly margemTexto = computed(() => {
    const m = this.margem();
    return m === null ? '' : this.formatoPercentual.format(m);
  });

  readonly grupoSelecionado = computed(() => {
    const id = numero(this.valoresPagina().idGrupo ?? null);
    return this.grupos().find((g) => g.id === id) ?? null;
  });

  /** Versões do grupo no seletor da loja, com este produto no lugar dele (ou no fim, se novo). */
  readonly versoes = computed<VersaoTela[]>(() => {
    const grupo = this.grupoSelecionado();
    if (!grupo) return [];
    const id = this.produtoId();
    const v = this.valores();
    const pagina = this.valoresPagina();
    const outras = grupo.versoes
      .filter((x) => x.idProduto !== id)
      .map((x) => ({
        id: x.idProduto,
        nome: x.nome ?? 'Sem nome',
        rotulo: x.rotulo,
        tipo: tipoTexto(x.tipo.id),
        preco: x.preco,
        imagemPrincipal: x.imagemPrincipal ?? null,
        ordem: x.ordem,
        este: false,
      }));
    const este = {
      id,
      nome: v.nome?.trim() || 'Este produto',
      rotulo: pagina.rotuloNoGrupo?.trim() || '—',
      tipo: tipoTexto(this.tipo),
      preco: numero(this.valoresComerciais().preco ?? null),
      ordem: numero(pagina.ordemNoGrupo ?? null) ?? Number.MAX_SAFE_INTEGER,
      imagemPrincipal: grupo.versoes.find((x) => x.idProduto === id)?.imagemPrincipal ?? null,
      este: true,
    };
    return [...outras, este].sort((a, b) => a.ordem - b.ordem);
  });

  /**
   * Obrigatórios para publicar (D28), com os mesmos critérios do backend. Rascunho
   * aceita tudo vazio; a lista aparece no card de pendências e na barra de ações.
   */
  readonly pendencias = computed(() => {
    const v = this.valores();
    const vend = this.valoresComerciais();
    const s = this.valoresSpecs();
    const p = this.valoresPagina();
    const faltam: string[] = [];
    if (!v.nome?.trim()) faltam.push(this.ehKit ? 'Nome do kit' : 'Nome do produto');
    if (!v.idMarca) faltam.push('Marca');
    if (!v.sku?.trim()) faltam.push('SKU');
    if (!v.idSubcategoria) faltam.push('Subcategoria');
    if (!v.idFornecedor) faltam.push('Fornecedor');
    if (this.ehDrone && numero(s.pesoGramas ?? null) === null) faltam.push('Peso');
    if (this.ehDrone && s.exigeRegistroAnac && !s.classeAnac) faltam.push('Classe ANAC');
    if (this.ehAcessorio && !v.tipoAcessorio) faltam.push('Tipo de acessório');
    if (this.ehKit && !this.componentes().length) faltam.push('Componentes');
    if (numero(vend.preco ?? null) === null) faltam.push('Preço de venda');
    const derivada = this.ehKit && !vend.sobrescreverEmbalagem ? this.embalagemDerivada() : null;
    if (numero(vend.pesoEmbalagemKg ?? null) === null && derivada?.peso == null)
      faltam.push('Peso da caixa');
    if (
      !lerDimensoes(vend.dimensoes ?? '')?.every((m) => m !== null) &&
      derivada?.dimensoes == null
    )
      faltam.push('Caixa A × L × C');
    if (p.idGrupo && !p.rotuloNoGrupo?.trim()) faltam.push('Rótulo no grupo');
    return faltam;
  });

  readonly statusAtual = computed(() => {
    const id = numero(this.valores().idStatus ?? null);
    return id === STATUS_PRODUTO.ATIVO
      ? 'Ativo'
      : id === STATUS_PRODUTO.INATIVO
        ? 'Inativo'
        : 'Rascunho';
  });

  /** Opções do select, mais o valor salvo quando ele não estiver na lista. */
  opcoes(campo: CampoOpcao): readonly string[] {
    const atual =
      campo === 'tipoAcessorio' ? this.form.controls.tipoAcessorio.value : this.valorSpec(campo);
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

  /** Categoria no select: o kit entra em categoria de qualquer tipo, então o tipo vai junto. */
  rotuloCategoria(c: Categoria): string {
    return this.ehKit && c.tipo
      ? `${c.nome} · ${c.tipo.id === TIPO_PRODUTO.DRONE ? 'Drone' : 'Acessório'}`
      : c.nome;
  }

  ngOnInit(): void {
    this.marcaService
      .listar(0, 1000)
      .subscribe((p) => this.marcas.set(p.items.filter((x) => x.nome)));
    // drone e acessório só entram em categoria do próprio tipo (D39); o kit, em qualquer uma
    this.categoriaService
      .listar(0, 1000)
      .subscribe((p) =>
        this.categorias.set(
          p.items.filter((x) => x.nome && (this.ehKit || x.tipo?.id === this.tipo)),
        ),
      );
    this.subcategoriaService
      .listar(null, 0, 1000)
      .subscribe((p) => this.subcategorias.set(p.items));
    this.fornecedorService.listar(0, 1000).subscribe((p) => this.fornecedores.set(p.items));
    this.grupoService.listar({}, 0, 1000).subscribe((p) => this.grupos.set(p.items));
    if (this.ehKit)
      this.produtoService
        .listar({ idStatus: STATUS_PRODUTO.ATIVO, kit: false }, 0, 1000)
        .subscribe((p) => this.conhecerOpcoes(p.items.map(opcaoDoProduto)));

    // ao trocar a categoria, limpa a subcategoria se ela for de outra categoria
    this.form.controls.idCategoria.valueChanges.subscribe((id) => {
      const idCategoria = id ? Number(id) : null;
      const sub = this.subcategorias().find(
        (s) => s.id === Number(this.form.controls.idSubcategoria.value),
      );
      if (sub && sub.categoria?.id !== idCategoria)
        this.form.controls.idSubcategoria.setValue(null);
    });

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = Number(idParam);
      this.produtoId.set(id);
      this.carregarProduto(id);
      this.midias.carregar(id);
    }
  }

  private carregarProduto(id: number): void {
    this.carregando.set(true);
    this.produtoService.buscarPorId(id).subscribe({
      next: (produto) => {
        if (produto.tipo.id !== this.tipo) {
          this.erroGeral.set(
            `Este produto é ${artigoTipo(produto.tipo.id)}; abra pela listagem de produtos.`,
          );
          this.carregando.set(false);
          return;
        }
        this.preencherTipo(produto);
        this.form.patchValue({
          nome: produto.nome ?? '',
          sku: produto.sku ?? '',
          descricaoCurta: produto.descricaoCurta ?? '',
          descricao: produto.descricao ?? '',
          slug: produto.slug,
          metaTitle: produto.metaTitle ?? '',
          metaDescription: produto.metaDescription ?? '',
          tags: produto.tags ?? '',
          garantiaMeses: produto.garantiaMeses,
          dataLancamento: produto.dataLancamento ?? '',
          idMarca: produto.marca?.id ?? null,
          idCategoria: produto.subcategoria?.categoria?.id ?? null,
          idSubcategoria: produto.subcategoria?.id ?? null,
          idFornecedor: produto.fornecedor?.id ?? null,
          idStatus: produto.status.id,
          destaque: produto.destaque,
          tipoAcessorio: produto.tipoAcessorio ?? '',
          pesoItemG: produto.pesoItemG ?? null,
          sugerirNosCompativeis: !!produto.sugerirNosCompativeis,
          exibirEmOfertas: !!produto.exibirEmOfertas,
        });
        // no kit, peso e dimensões só vão para o campo quando foram informados nele (D16)
        const sobrescrito =
          !this.ehKit || !!produto.pesoSobrescrito || !!produto.dimensoesSobrescritas;
        this.dadosComerciais.patchValue({
          preco: produto.preco,
          custoAquisicao: produto.custoAquisicao,
          estoqueFisico: produto.estoqueFisico,
          estoqueMinimo: produto.estoqueMinimo ?? null,
          pesoEmbalagemKg: sobrescrito ? produto.pesoEmbalagemKg : null,
          dimensoes: sobrescrito ? formatarDimensoes(produto) : '',
          sobrescreverEmbalagem: this.ehKit && sobrescrito,
        });
        this.pagina.patchValue({
          idGrupo: produto.grupo?.id ?? null,
          rotuloNoGrupo: produto.rotuloNoGrupo ?? '',
          ordemNoGrupo: produto.grupo ? produto.ordemNoGrupo : null,
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
    if (this.ehAcessorio) {
      this.compativeis.set((produto.compativeis ?? []).map((p) => ({ produto: p, salvo: true })));
      return;
    }
    if (this.ehKit) {
      // o componente salvo pode estar inativo e fora da lista de ativos: entra mesmo assim
      const salvos = (produto.componentes ?? []).map((c) => ({
        produto: {
          id: c.idProduto,
          nome: c.nome,
          sku: c.sku,
          tipo: tipoTexto(c.tipo.id),
          disponivel: c.status.id === STATUS_PRODUTO.ATIVO ? c.disponivel : 0,
          imagemPrincipal: c.imagemPrincipal ?? null,
          pesoEmbalagemKg: null,
          alturaEmbalagemCm: null,
          larguraEmbalagemCm: null,
          comprimentoEmbalagemCm: null,
        },
        quantidade: c.quantidade,
      }));
      this.componentes.set(salvos.map((s) => ({ ...s, produto: this.opcaoConhecida(s.produto) })));
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

  /** As medidas da embalagem do componente vêm da lista de ativos; o salvo pode não ter. */
  private opcaoConhecida(o: OpcaoComponente): OpcaoComponente {
    return this.opcoesComponente().find((x) => x.id === o.id) ?? o;
  }

  private conhecerOpcoes(lista: OpcaoComponente[]): void {
    this.opcoesComponente.set(lista);
    this.componentes.update((atuais) =>
      atuais.map((c) => ({ ...c, produto: this.opcaoConhecida(c.produto) })),
    );
  }

  adicionarComponente(evento: Event): void {
    const select = evento.target as HTMLSelectElement;
    const opcao = this.opcoesComponente().find((o) => o.id === Number(select.value));
    select.value = '';
    if (!opcao) return;
    this.componentes.update((lista) => [...lista, { produto: opcao, quantidade: 1 }]);
    this.escolhendoComponente.set(false);
  }

  mudarQuantidade(id: number, evento: Event): void {
    const quantidade = Number((evento.target as HTMLInputElement).value);
    this.componentes.update((lista) =>
      lista.map((c) =>
        c.produto.id === id
          ? { ...c, quantidade: Number.isFinite(quantidade) ? quantidade : 0 }
          : c,
      ),
    );
  }

  removerComponente(id: number): void {
    this.componentes.update((lista) => lista.filter((c) => c.produto.id !== id));
  }

  erroComponente(i: number): string | null {
    const p = `componentes[${i}].`;
    return this.mensagemErro(p + 'idProduto') ?? this.mensagemErro(p + 'quantidade');
  }

  /** Rota de edição do produto citado, conforme o tipo dele. */
  rotaEdicao(produto: { id: number; tipo: { id: number } }): string {
    return `/admin/${rotaDoTipo(produto.tipo.id)}/${produto.id}/editar`;
  }

  campoInvalido(campo: string): boolean {
    return campo in this.errosCampo();
  }

  mensagemErro(campo: string): string | null {
    return this.errosCampo()[campo] ?? null;
  }

  /** As três medidas viram um campo só na tela; o erro de qualquer uma aparece nele. */
  erroDimensoes(): string | null {
    return (
      this.mensagemErro('dimensoes') ??
      this.mensagemErro('alturaEmbalagemCm') ??
      this.mensagemErro('larguraEmbalagemCm') ??
      this.mensagemErro('comprimentoEmbalagemCm')
    );
  }

  moeda(valor: number | null): string {
    return valor === null ? '—' : this.formatoMoeda.format(valor);
  }

  /**
   * "Salvar rascunho" grava como RASCUNHO; "Publicar" grava com o status escolhido na
   * coluna lateral, e Rascunho ali vira Ativo (publicar é tirar do rascunho).
   */
  salvar(publicar: boolean): void {
    this.erroGeral.set(null);
    this.errosCampo.set({});

    const v = this.form.getRawValue();
    const vend = this.dadosComerciais.getRawValue();
    const p = this.pagina.getRawValue();
    const escolhido = Number(v.idStatus);
    const idStatus = !publicar
      ? STATUS_PRODUTO.RASCUNHO
      : escolhido === STATUS_PRODUTO.RASCUNHO
        ? STATUS_PRODUTO.ATIVO
        : escolhido;

    // no kit sem sobrescrita, peso e dimensões vão vazios e o backend deriva (D16)
    const usaEmbalagemPropria = !this.ehKit || vend.sobrescreverEmbalagem;
    const medidas = usaEmbalagemPropria ? lerDimensoes(vend.dimensoes) : [null, null, null];
    if (medidas === undefined) {
      this.errosCampo.set({
        dimensoes: 'Use altura × largura × comprimento em cm, ex.: 15×28×38.',
      });
      this.erroGeral.set('Verifique os campos destacados.');
      return;
    }
    const idGrupo = p.idGrupo ? Number(p.idGrupo) : null;
    const dto: ProdutoDTO = {
      nome: v.nome.trim(),
      sku: v.sku.trim(),
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
      idStatus,
      destaque: v.destaque,
      preco: numero(vend.preco),
      custoAquisicao: numero(vend.custoAquisicao),
      pesoEmbalagemKg: usaEmbalagemPropria ? numero(vend.pesoEmbalagemKg) : null,
      alturaEmbalagemCm: medidas[0],
      larguraEmbalagemCm: medidas[1],
      comprimentoEmbalagemCm: medidas[2],
      idGrupo,
      rotuloNoGrupo: idGrupo ? p.rotuloNoGrupo.trim() : '',
      ordemNoGrupo: idGrupo ? numero(p.ordemNoGrupo) : null,
    };

    this.salvando.set(true);
    const id = this.produtoId();
    const estoque = {
      estoqueFisico: numero(vend.estoqueFisico),
      estoqueMinimo: numero(vend.estoqueMinimo),
    };
    let request$: Observable<Produto | void>;
    if (this.ehDrone) {
      const drone: DroneDTO = { ...dto, ...estoque, ...this.specsDTO() };
      request$ = id ? this.droneService.atualizar(id, drone) : this.droneService.criar(drone);
    } else if (this.ehKit) {
      const kit: KitDTO = {
        ...dto,
        exibirEmOfertas: v.exibirEmOfertas,
        componentes: this.componentes().map((c) => ({
          idProduto: c.produto.id,
          quantidade: c.quantidade,
        })),
      };
      request$ = id ? this.kitService.atualizar(id, kit) : this.kitService.criar(kit);
    } else {
      const acessorio: AcessorioDTO = {
        ...dto,
        ...estoque,
        tipoAcessorio: v.tipoAcessorio,
        pesoItemG: numero(v.pesoItemG),
        sugerirNosCompativeis: v.sugerirNosCompativeis,
        idsCompativeis: this.compativeis().map((c) => c.produto.id),
      };
      request$ = id
        ? this.acessorioService.atualizar(id, acessorio)
        : this.acessorioService.criar(acessorio);
    }

    request$.subscribe({
      next: (produto) => {
        const criado = id === null ? (produto as Produto).id : null;
        if (criado === null) {
          this.salvando.set(false);
          this.router.navigate([this.rotaListagem]);
          return;
        }
        // produto novo: sobe as mídias da fila; se alguma falhar, a tela vira a de edição
        this.midias.enviarFila(criado).subscribe({
          complete: () => {
            this.salvando.set(false);
            this.router.navigate([this.rotaListagem]);
          },
          error: () => {
            this.salvando.set(false);
            this.produtoId.set(criado);
            this.location.replaceState(`/admin/${this.rotaTipo}/${criado}/editar`);
          },
        });
      },
      error: (err: HttpErrorResponse) => this.tratarErro(err),
    });
  }

  private specsDTO() {
    const s = this.specs.getRawValue();
    const alcanceKm = numero(s.alcanceKm);
    return {
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
        // 409: produto em uso, como componente de kit (D24); o detail explica e sugere inativar
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

function tipoTexto(idTipo: number): string {
  return idTipo === TIPO_PRODUTO.DRONE
    ? 'Drone'
    : idTipo === TIPO_PRODUTO.KIT
      ? 'Kit'
      : 'Acessório';
}

function artigoTipo(idTipo: number): string {
  return idTipo === TIPO_PRODUTO.DRONE
    ? 'um drone'
    : idTipo === TIPO_PRODUTO.KIT
      ? 'um kit'
      : 'um acessório';
}

function rotaDoTipo(idTipo: number): string {
  return idTipo === TIPO_PRODUTO.DRONE
    ? 'drones'
    : idTipo === TIPO_PRODUTO.KIT
      ? 'kits'
      : 'acessorios';
}

function opcaoDoProduto(p: Produto): OpcaoComponente {
  return {
    id: p.id,
    nome: p.nome,
    sku: p.sku,
    tipo: tipoTexto(p.tipo.id),
    disponivel: p.disponivel,
    imagemPrincipal: p.imagemPrincipal ?? null,
    pesoEmbalagemKg: p.pesoEmbalagemKg,
    alturaEmbalagemCm: p.alturaEmbalagemCm,
    larguraEmbalagemCm: p.larguraEmbalagemCm,
    comprimentoEmbalagemCm: p.comprimentoEmbalagemCm,
  };
}

/** Mesma regra do KitCalculo: peso e comprimento somados, altura e largura a maior. */
function derivarEmbalagem(lista: LinhaComponente[]): {
  peso: number | null;
  dimensoes: string | null;
} {
  if (!lista.length) return { peso: null, dimensoes: null };
  let peso: number | null = 0;
  let altura: number | null = 0;
  let largura: number | null = 0;
  let comprimento: number | null = 0;
  for (const { produto: p, quantidade } of lista) {
    peso =
      p.pesoEmbalagemKg === null || peso === null ? null : peso + p.pesoEmbalagemKg * quantidade;
    altura =
      p.alturaEmbalagemCm === null || altura === null
        ? null
        : Math.max(altura, p.alturaEmbalagemCm);
    largura =
      p.larguraEmbalagemCm === null || largura === null
        ? null
        : Math.max(largura, p.larguraEmbalagemCm);
    comprimento =
      p.comprimentoEmbalagemCm === null || comprimento === null
        ? null
        : comprimento + p.comprimentoEmbalagemCm * quantidade;
  }
  const dimensoes =
    altura === null || largura === null || comprimento === null
      ? null
      : `${altura}×${largura}×${comprimento}`;
  return { peso: peso === null ? null : Math.round(peso * 1000) / 1000, dimensoes };
}

/** Altura × largura × comprimento da embalagem, como o protótipo mostra ("15×28×38"). */
function formatarDimensoes(p: Produto): string {
  const medidas = [p.alturaEmbalagemCm, p.larguraEmbalagemCm, p.comprimentoEmbalagemCm];
  return medidas.every((m) => m === null) ? '' : medidas.map((m) => m ?? '').join('×');
}

/** Vazio vira [null, null, null]; formato inválido vira undefined. Aceita × ou x. */
function lerDimensoes(texto: string): (number | null)[] | undefined {
  const t = texto.trim();
  if (!t) return [null, null, null];
  const partes = t.split(/\s*[x×]\s*/i);
  if (partes.length !== 3 || partes.some((p) => !/^\d+$/.test(p))) return undefined;
  return partes.map(Number);
}

/** Campo numérico do form: vazio vira null (o input type=number pode devolver ''). */
function numero(valor: number | string | null): number | null {
  return valor === null || `${valor}` === '' ? null : Number(valor);
}
