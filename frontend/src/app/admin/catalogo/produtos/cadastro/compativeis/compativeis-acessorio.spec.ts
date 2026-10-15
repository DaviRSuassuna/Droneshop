import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../../../../environments/environment';
import { ProdutoResumo, TIPO_PRODUTO } from '../../../../../core/models/produto.model';
import { CompativeisAcessorio } from './compativeis-acessorio';

describe('CompativeisAcessorio', () => {
  const drone: ProdutoResumo = {
    id: 10,
    nome: 'Falcon X2 Pro',
    tipo: { id: TIPO_PRODUTO.DRONE, nome: 'DRONE' },
  };
  const helice: ProdutoResumo = {
    id: 12,
    nome: 'Hélices Falcon',
    tipo: { id: TIPO_PRODUTO.ACESSORIO, nome: 'ACESSORIO' },
  };
  const proprio: ProdutoResumo = {
    id: 20,
    nome: 'Bateria Falcon',
    tipo: { id: TIPO_PRODUTO.ACESSORIO, nome: 'ACESSORIO' },
  };
  let http: HttpTestingController;

  beforeEach(() => {
    vi.useFakeTimers();
    TestBed.configureTestingModule({
      imports: [CompativeisAcessorio],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    vi.useRealTimers();
  });

  function criar() {
    const fixture = TestBed.createComponent(CompativeisAcessorio);
    fixture.componentRef.setInput('idAtual', proprio.id);
    fixture.detectChanges();
    return fixture;
  }

  it('busca por nome e esconde o próprio acessório e o que já está vinculado', () => {
    const fixture = criar();
    const comp = fixture.componentInstance;
    comp.vinculos.set([{ produto: helice, salvo: true }]);

    comp.busca.setValue('fal');
    vi.advanceTimersByTime(300);
    const req = http.expectOne((r) => r.url === `${environment.apiUrl}/produtos`);
    expect(req.request.params.get('nome')).toBe('fal');
    req.flush({ items: [drone, helice, proprio], page: 0, pageSize: 8, totalItems: 3 });

    expect(comp.opcoes().map((p) => p.id)).toEqual([drone.id]);
  });

  it('não busca com menos de 2 letras', () => {
    const comp = criar().componentInstance;
    comp.busca.setValue('f');
    vi.advanceTimersByTime(300);
    http.expectNone(`${environment.apiUrl}/produtos`);
    expect(comp.buscou()).toBe(false);
  });

  it('vincula como novo e desvincula', () => {
    const comp = criar().componentInstance;
    comp.vincular(drone);
    expect(comp.vinculos()).toEqual([{ produto: drone, salvo: false }]);
    expect(comp.busca.value).toBe('');

    comp.desvincular(drone.id);
    expect(comp.vinculos()).toEqual([]);
  });
});
