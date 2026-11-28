import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import {
  Carrinho,
  CotacaoFrete,
  ItemCarrinho,
  MODALIDADE_ENTREGA,
} from '../../../core/models/compra.model';
import { ProblemDetail } from '../../../core/models/problem.model';
import { AuthService } from '../../../core/services/auth.service';
import { CarrinhoService } from '../../../core/services/carrinho.service';
import { CheckoutService } from '../../../core/services/checkout.service';
import { EnderecoClienteService } from '../../../core/services/endereco-cliente.service';
import { ListaDesejosService } from '../../../core/services/lista-desejos.service';
import { reais } from '../../../core/utils/moeda';

/** Nomes das modalidades como no protótipo 02 (carrinho.html). */
const NOME_MODALIDADE: Record<number, string> = {
  [MODALIDADE_ENTREGA.EXPRESSA]: 'Expresso',
  [MODALIDADE_ENTREGA.PADRAO]: 'Padrão',
  [MODALIDADE_ENTREGA.RETIRADA_SHOWROOM]: 'Retirar no showroom',
};

/**
 * Carrinho (#24, protótipo 02 carrinho.html). Sem login vale o carrinho anônimo do token
 * (X-Carrinho-Token, RF16); logado, o carrinho do cliente (GET /conta/carrinho), que o
 * backend já juntou ao anônimo no login (RF17). O frete é uma prévia (#30) para o CEP do
 * endereço principal ou o digitado; a escolha vale de novo no checkout.
 */
@Component({
  selector: 'app-carrinho',
  imports: [RouterLink],
  templateUrl: './carrinho.html',
  styleUrl: './carrinho.scss',
})
export class CarrinhoPagina implements OnInit {
  private readonly carrinhos = inject(CarrinhoService);
  private readonly checkout = inject(CheckoutService);
  private readonly auth = inject(AuthService);
  private readonly enderecos = inject(EnderecoClienteService);
  private readonly desejos = inject(ListaDesejosService);

  protected readonly reais = reais;
  protected readonly nomeModalidade = NOME_MODALIDADE;

  readonly carrinho = signal<Carrinho | null>(null);
  readonly carregando = signal(true);
  readonly cep = signal('');
  readonly cotacoes = signal<CotacaoFrete[]>([]);
  readonly modalidade = signal<number | null>(null);
  readonly cupom = signal('');
  readonly erro = signal<string | null>(null);

  readonly itens = computed(() => this.carrinho()?.itens ?? []);
  readonly quantidade = computed(() => this.itens().reduce((s, i) => s + i.quantidade, 0));
  readonly cotacao = computed(
    () => this.cotacoes().find((c) => c.modalidade.id === this.modalidade()) ?? null,
  );
  readonly total = computed(() => (this.carrinho()?.total ?? 0) + (this.cotacao()?.custo ?? 0));

  ngOnInit(): void {
    const logado = this.auth.temPapel('CLIENTE');
    const req = logado
      ? this.carrinhos.doCliente()
      : this.carrinhos.token()
        ? this.carrinhos.buscar()
        : null;
    if (!req) {
      this.carregando.set(false);
      return;
    }
    req.subscribe({
      next: (c) => {
        this.definir(c);
        this.carregando.set(false);
        if (logado) this.cepDoEndereco();
      },
      error: (e: HttpErrorResponse) => {
        // token de carrinho desconhecido: o próximo item cria outro
        if (e.status === 404) this.carrinhos.esquecer();
        this.carregando.set(false);
      },
    });
  }

  private definir(c: Carrinho): void {
    this.carrinho.set(c);
    if (c.cupom) this.cupom.set(c.cupom.codigo);
    // itens mudaram: o frete cotado antes não vale mais
    if (this.cotacoes().length) this.cotar();
  }

  private cepDoEndereco(): void {
    this.enderecos.listar().subscribe({
      next: (lista) => {
        const e = lista.find((x) => x.principal) ?? lista[0];
        if (e) {
          this.cep.set(e.cep.replace(/\D/g, ''));
          this.cotar();
        }
      },
      error: () => undefined,
    });
  }

  alterarCep(valor: string): void {
    this.cep.set(valor.replace(/\D/g, '').slice(0, 8));
  }

  cotar(): void {
    if (this.cep().length !== 8 || !this.itens().length) return;
    this.checkout.cotar(this.cep()).subscribe({
      next: (cot) => {
        this.cotacoes.set(cot);
        const atual = cot.find((c) => c.modalidade.id === this.modalidade());
        this.modalidade.set(atual?.modalidade.id ?? cot[0]?.modalidade.id ?? null);
      },
      error: (e: HttpErrorResponse) =>
        this.erro.set(this.texto(e, 'Não foi possível cotar o frete.')),
    });
  }

  cepFormatado(): string {
    const c = this.cep();
    return c.length > 5 ? `${c.slice(0, 5)}-${c.slice(5)}` : c;
  }

  nome(c: CotacaoFrete): string {
    return this.nomeModalidade[c.modalidade.id] ?? c.modalidade.nome;
  }

  prazo(c: CotacaoFrete): string {
    return `${c.prazoDias} ${c.prazoDias === 1 ? 'dia útil' : 'dias úteis'}`;
  }

  mudarQuantidade(i: ItemCarrinho, delta: number): void {
    const nova = i.quantidade + delta;
    if (nova < 1) return;
    this.executar(this.carrinhos.alterar(i.id, nova));
  }

  remover(i: ItemCarrinho): void {
    this.executar(this.carrinhos.remover(i.id));
  }

  salvarNosFavoritos(i: ItemCarrinho): void {
    if (!this.auth.temPapel('CLIENTE')) {
      this.erro.set('Entre na sua conta para salvar nos favoritos.');
      return;
    }
    this.desejos.adicionar(i.produto.id).subscribe({
      next: () => this.remover(i),
      error: (e: HttpErrorResponse) => this.erro.set(this.texto(e, 'Não foi possível salvar.')),
    });
  }

  aplicarCupom(): void {
    const codigo = this.cupom().trim();
    if (codigo) this.executar(this.carrinhos.aplicarCupom(codigo));
  }

  removerCupom(): void {
    this.cupom.set('');
    this.executar(this.carrinhos.removerCupom());
  }

  private executar(req: Observable<Carrinho>): void {
    this.erro.set(null);
    req.subscribe({
      next: (c) => this.definir(c),
      error: (e: HttpErrorResponse) =>
        this.erro.set(this.texto(e, 'Não foi possível atualizar o carrinho.')),
    });
  }

  private texto(e: HttpErrorResponse, padrao: string): string {
    const p = e.error as ProblemDetail | null;
    return p?.errors?.[0]?.message ?? p?.detail ?? padrao;
  }
}
