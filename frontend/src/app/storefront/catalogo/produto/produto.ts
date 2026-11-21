import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Meta, Title } from '@angular/platform-browser';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { catchError, of, switchMap } from 'rxjs';
import {
  MidiaLoja,
  ProdutoLojaDetalhe,
  ProdutoLojaItem,
  ProdutoLojaResumo,
} from '../../../core/models/loja.model';
import { LojaService } from '../../../core/services/loja.service';
import { Comparar } from '../comparar/comparar';
import { Pcard } from '../pcard/pcard';
import { Perguntas } from '../perguntas/perguntas';
import { PrecoPipe } from '../preco.pipe';

interface Linha {
  rotulo: string;
  valor: string;
}

const sim = (v: boolean | null | undefined, texto: string) => (v ? texto : null);

/**
 * Página do produto (protótipo 02: produto.html). As versões do grupo (D40) trocam de página
 * pelo slug; a galeria monta as fotos das mídias. Comparação (#44) e avaliações ficam de fora.
 */
@Component({
  selector: 'app-produto',
  imports: [RouterLink, Pcard, PrecoPipe, Comparar, Perguntas],
  templateUrl: './produto.html',
})
export class Produto {
  private readonly loja = inject(LojaService);
  private readonly titulo = inject(Title);
  private readonly meta = inject(Meta);

  readonly produto = signal<ProdutoLojaDetalhe | null>(null);
  readonly naoEncontrado = signal(false);
  readonly compativeis = signal<ProdutoLojaResumo[]>([]);
  readonly fotoAtual = signal(0);
  readonly quantidade = signal(1);

  readonly fotos = computed<MidiaLoja[]>(() => {
    const p = this.produto();
    if (!p) return [];
    const imagens = p.midias.filter((m) => m.tipo?.nome !== 'MANUAL' && !m.url.endsWith('.pdf'));
    if (imagens.length) return imagens;
    return p.imagemPrincipal
      ? [
          {
            id: 0,
            tipo: { id: 1, nome: 'IMAGEM' },
            url: p.imagemPrincipal,
            principal: true,
            ordem: 0,
            altText: p.nome,
          },
        ]
      : [];
  });
  readonly foto = computed(() => this.fotos()[this.fotoAtual()] ?? null);

  readonly ficha = computed(() => {
    const p = this.produto();
    if (!p) return [];
    const leituras = [
      { rotulo: 'Autonomia de voo', valor: p.autonomiaMinutos, unidade: 'min' },
      {
        rotulo: 'Alcance de transmissão',
        valor: p.alcanceMetros ? Math.round(p.alcanceMetros / 100) / 10 : null,
        unidade: 'km',
      },
      { rotulo: 'Velocidade máxima', valor: p.velocidadeMaximaKmh, unidade: 'km/h' },
      { rotulo: 'Peso de decolagem', valor: p.pesoGramas, unidade: 'g' },
    ];
    return leituras.filter((l) => l.valor !== null && l.valor !== undefined);
  });

  readonly grupos = computed(() => {
    const p = this.produto();
    if (!p) return [];
    const grupo = (rotulo: string, linhas: (Linha | null)[]) => ({
      rotulo,
      linhas: linhas.filter((l): l is Linha => !!l && !!l.valor),
    });
    const l = (rotulo: string, valor: unknown, sufixo = '') =>
      valor === null || valor === undefined || valor === ''
        ? null
        : { rotulo, valor: `${valor}${sufixo}` };
    return [
      grupo('Estrutura e voo', [
        l('Peso', p.pesoGramas, ' g'),
        l('Dobrado', p.dimensoesDobrado),
        l('Autonomia', p.autonomiaMinutos, ' min'),
        l('Velocidade máx.', p.velocidadeMaximaKmh, ' km/h'),
      ]),
      grupo('Câmera', [
        l(
          'Sensor',
          [p.tipoSensor, p.resolucaoFotoMp ? `${p.resolucaoFotoMp} MP` : null]
            .filter(Boolean)
            .join(' · '),
        ),
        l('Vídeo', p.resolucaoVideo),
        l('Estabilização', p.estabilizacao),
      ]),
      grupo('Bateria e navegação', [
        l(
          'Bateria',
          [p.tipoBateria, p.capacidadeBateriaMah ? `${p.capacidadeBateriaMah} mAh` : null]
            .filter(Boolean)
            .join(' · '),
        ),
        l('Carregamento', p.tempoCarregamentoMinutos, ' min'),
        l('Navegação', sim(p.gpsIntegrado, 'GPS')),
        l('Sensores', sim(p.sensoresAnticolisao, 'Anticolisão')),
      ]),
      grupo('Geral', [
        l('Tipo', p.tipoAcessorio),
        l('SKU', p.sku),
        l('Garantia', p.garantiaMeses, ' meses'),
      ]),
    ].filter((g) => g.linhas.length);
  });

  constructor() {
    const rota = inject(ActivatedRoute);
    rota.paramMap
      .pipe(
        switchMap((params) => {
          const slug = params.get('slug') ?? '';
          this.naoEncontrado.set(false);
          this.fotoAtual.set(0);
          this.quantidade.set(1);
          this.loja
            .listarCompativeis(slug)
            .pipe(catchError(() => of([])))
            .subscribe((c) => this.compativeis.set(c));
          return this.loja.buscarProduto(slug).pipe(
            catchError(() => {
              this.naoEncontrado.set(true);
              return of(null);
            }),
          );
        }),
        takeUntilDestroyed(),
      )
      .subscribe((p) => {
        this.produto.set(p);
        if (!p) return;
        this.titulo.setTitle(`${p.metaTitle || p.nome} · Aeria`);
        if (p.metaDescription)
          this.meta.updateTag({ name: 'description', content: p.metaDescription });
      });
  }

  cardCompativel(c: ProdutoLojaResumo): ProdutoLojaItem {
    return {
      id: c.id,
      nome: c.nome,
      slug: c.slug,
      tipo: { id: 2, nome: 'ACESSORIO' },
      marca: null,
      precoMinimo: c.precoMinimo,
      imagemPrincipal: null,
      disponivel: true,
    };
  }

  mudarQuantidade(delta: number): void {
    const max = Math.max(this.produto()?.disponivel ?? 1, 1);
    this.quantidade.update((q) => Math.min(Math.max(q + delta, 1), max));
  }
}
