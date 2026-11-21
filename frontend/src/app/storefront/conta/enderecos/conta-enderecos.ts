import { AccountMenu } from '../../../layout/storefront/account-menu/account-menu';
import { ContaSessao } from '../conta-sessao';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, ElementRef, OnInit, computed, inject, signal, viewChild } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { EnderecoCliente, EnderecoClienteDTO } from '../../../core/models/cliente.model';
import { Estado, Municipio } from '../../../core/models/endereco.model';
import { ProblemDetail } from '../../../core/models/problem.model';
import { CepService } from '../../../core/services/cep.service';
import { EnderecoClienteService } from '../../../core/services/endereco-cliente.service';
import { EstadoService } from '../../../core/services/estado.service';
import { MunicipioService } from '../../../core/services/municipio.service';
import { somenteDigitos } from '../../../core/utils/documentos';
import { MascaraDirective } from '../../../shared/directives/mascara.directive';
import { CepPipe, formatarCep } from '../../../shared/pipes/cep.pipe';

/** "Minha conta > Endereços" (protótipo 02, conta-enderecos.html). */
@Component({
  selector: 'app-conta-enderecos',
  imports: [AccountMenu, ReactiveFormsModule, RouterLink, MascaraDirective, CepPipe],
  providers: [ContaSessao],
  templateUrl: './conta-enderecos.html',
  styleUrl: './conta-enderecos.scss',
})
export class ContaEnderecos implements OnInit {
  protected readonly sessao = inject(ContaSessao);
  private readonly enderecosService = inject(EnderecoClienteService);
  private readonly cepService = inject(CepService);
  private readonly estadoService = inject(EstadoService);
  private readonly municipioService = inject(MunicipioService);
  private readonly fb = inject(FormBuilder);
  private readonly dialogo = viewChild.required<ElementRef<HTMLDialogElement>>('dlgEnd');

  readonly enderecos = signal<EnderecoCliente[]>([]);
  readonly estados = signal<Estado[]>([]);
  readonly municipios = signal<Municipio[]>([]);
  readonly editando = signal<number | null>(null);
  readonly mensagem = signal<string | null>(null);
  readonly erroDialogo = signal<string | null>(null);
  readonly resumo = computed(() => {
    const n = this.enderecos().length;
    if (!n) return 'Nenhum endereço salvo';
    return `${n} ${n === 1 ? 'endereço salvo' : 'endereços salvos'} · 1 definido como padrão`;
  });

  readonly form = this.fb.nonNullable.group({
    apelido: ['', Validators.maxLength(40)],
    cep: ['', [Validators.required, Validators.pattern(/^\d{5}-?\d{3}$/)]],
    logradouro: ['', Validators.required],
    numero: ['', Validators.required],
    complemento: [''],
    bairro: ['', Validators.required],
    idEstado: [0, Validators.min(1)],
    idMunicipio: [0, Validators.min(1)],
    principal: [false],
  });

  ngOnInit(): void {
    this.sessao.carregar();
    this.carregar();
    this.estadoService.listar().subscribe((p) => this.estados.set(p.items));
  }

  carregar(): void {
    this.enderecosService.listar().subscribe((lista) => this.enderecos.set(lista));
  }

  novo(): void {
    this.editando.set(null);
    this.erroDialogo.set(null);
    this.form.reset();
    this.municipios.set([]);
    this.dialogo().nativeElement.showModal();
  }

  editar(e: EnderecoCliente): void {
    this.editando.set(e.id);
    this.erroDialogo.set(null);
    this.form.setValue({
      apelido: e.apelido ?? '',
      cep: formatarCep(e.cep),
      logradouro: e.logradouro,
      numero: e.numero,
      complemento: e.complemento ?? '',
      bairro: e.bairro,
      idEstado: e.municipio.estado.id,
      idMunicipio: e.municipio.id,
      principal: e.principal,
    });
    this.carregarMunicipios(e.municipio.estado.id, e.municipio.id);
    this.dialogo().nativeElement.showModal();
  }

  fechar(): void {
    this.dialogo().nativeElement.close();
  }

  aoMudarEstado(): void {
    this.carregarMunicipios(this.form.controls.idEstado.value, 0);
  }

  /** CEP com 8 dígitos preenche rua, bairro, estado e cidade pela API (que consulta o ViaCEP). */
  buscarCep(): void {
    const cep = somenteDigitos(this.form.controls.cep.value);
    if (cep.length !== 8) return;
    this.cepService.buscar(cep).subscribe({
      next: (r) => {
        this.form.patchValue({
          logradouro: r.logradouro ?? this.form.controls.logradouro.value,
          bairro: r.bairro ?? this.form.controls.bairro.value,
        });
        if (r.municipio) {
          this.form.patchValue({ idEstado: r.municipio.estado.id });
          this.carregarMunicipios(r.municipio.estado.id, r.municipio.id);
        }
      },
      error: () => this.erroDialogo.set('CEP não encontrado. Preencha o endereço manualmente.'),
    });
  }

  salvar(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid) {
      this.erroDialogo.set('Preencha os campos obrigatórios.');
      return;
    }
    const v = this.form.getRawValue();
    const dto: EnderecoClienteDTO = {
      apelido: v.apelido.trim(),
      cep: somenteDigitos(v.cep),
      logradouro: v.logradouro.trim(),
      numero: v.numero.trim(),
      complemento: v.complemento.trim(),
      bairro: v.bairro.trim(),
      idMunicipio: v.idMunicipio,
      principal: v.principal,
    };
    const id = this.editando();
    const pedido: Observable<unknown> =
      id === null ? this.enderecosService.criar(dto) : this.enderecosService.alterar(id, dto);
    pedido.subscribe({
      next: () => {
        this.fechar();
        this.mensagem.set('Endereço salvo.');
        this.carregar();
      },
      error: (e: HttpErrorResponse) => this.erroDialogo.set(this.textoErro(e)),
    });
  }

  tornarPrincipal(e: EnderecoCliente): void {
    this.enderecosService.marcarPrincipal(e.id).subscribe(() => this.carregar());
  }

  remover(e: EnderecoCliente): void {
    this.enderecosService.excluir(e.id).subscribe(() => {
      this.mensagem.set('Endereço removido.');
      this.carregar();
    });
  }

  private carregarMunicipios(idEstado: number, idMunicipio: number): void {
    this.municipios.set([]);
    if (!idEstado) return;
    this.municipioService.listarPorEstado(idEstado).subscribe((p) => {
      this.municipios.set(p.items);
      this.form.patchValue({ idMunicipio });
    });
  }

  private textoErro(e: HttpErrorResponse): string {
    const p = e.error as ProblemDetail | null;
    return p?.errors?.[0]?.message ?? p?.detail ?? 'Não foi possível salvar. Tente novamente.';
  }
}
