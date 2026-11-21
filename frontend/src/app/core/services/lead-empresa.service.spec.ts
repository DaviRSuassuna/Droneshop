import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { LeadEmpresaService } from './lead-empresa.service';

describe('LeadEmpresaService', () => {
  const api = environment.apiUrl;
  let service: LeadEmpresaService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(LeadEmpresaService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('envia a proposta sem login', () => {
    const dto = {
      razaoSocial: 'Fazenda Ltda.',
      cnpj: '11222333000181',
      nomeContato: 'Marina',
      emailContato: 'marina@empresa.com.br',
      telefoneContato: null,
      segmento: 'Agronegócio',
      quantidadeEstimada: 4,
      mensagem: 'Mapeamento',
    };
    service.enviar(dto).subscribe();
    const req = http.expectOne(`${api}/leads-empresas`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dto);
    req.flush(null);
  });

  it('lista e muda o status no admin', () => {
    service.listar(1, 0, 20).subscribe();
    const lista = http.expectOne((r) => r.url === `${api}/admin/leads-empresas`);
    expect(lista.request.method).toBe('GET');
    expect(lista.request.params.get('idStatus')).toBe('1');
    expect(lista.request.params.get('page')).toBe('0');
    lista.flush({ items: [], page: 0, pageSize: 20, totalItems: 0, totalPages: 0 });
    service.alterarStatus(3, 2).subscribe();
    const st = http.expectOne(`${api}/admin/leads-empresas/3/status`);
    expect(st.request.method).toBe('PATCH');
    expect(st.request.body).toEqual({ idStatus: 2 });
    st.flush(null);
  });
});
