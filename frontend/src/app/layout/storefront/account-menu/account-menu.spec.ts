import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { AccountMenu } from './account-menu';
import { ResumoConta, iniciais } from './areas-conta';

@Component({ template: '' })
class Vazia {}

@Component({
  imports: [AccountMenu],
  template: `<app-account-menu [conta]="conta()" (sair)="saidas = saidas + 1" />`,
})
class Hospedeiro {
  readonly conta = signal<ResumoConta | null>(null);
  saidas = 0;
}

async function montar(conta: ResumoConta | null, url = '/conta/perfil') {
  TestBed.configureTestingModule({
    imports: [Hospedeiro],
    providers: [provideRouter([{ path: 'conta/:area', component: Vazia }])],
  });
  await TestBed.inject(Router).navigateByUrl(url);
  const fixture = TestBed.createComponent(Hospedeiro);
  fixture.componentInstance.conta.set(conta);
  fixture.detectChanges();
  await fixture.whenStable();
  fixture.detectChanges();
  return { fixture, el: fixture.nativeElement as HTMLElement };
}

describe('AccountMenu', () => {
  it('lista as cinco áreas da conta na ordem do protótipo', async () => {
    const { el } = await montar(null);
    const links = Array.from(el.querySelectorAll('nav.side a'));
    expect(links.map((a) => a.getAttribute('href'))).toEqual([
      '/conta/pedidos',
      '/conta/perfil',
      '/conta/favoritos',
      '/conta/enderecos',
      '/conta/pagamentos',
    ]);
    expect(el.querySelector('.me')).toBeNull();
  });

  it('mostra avatar, nome, detalhe e as contagens maiores que zero', async () => {
    const { el } = await montar({
      nome: 'Marina Ribeiro',
      detalhe: 'Cliente desde 2024',
      contagens: { pedidos: 12, favoritos: 6, enderecos: 0 },
    });
    expect(el.querySelector('.av')?.textContent?.trim()).toBe('MR');
    expect(el.querySelector('.me b')?.textContent).toBe('Marina Ribeiro');
    expect(el.querySelector('.me div span')?.textContent).toBe('Cliente desde 2024');
    const contagens = Array.from(el.querySelectorAll('nav.side a em')).map((e) => e.textContent);
    expect(contagens).toEqual(['12', '6']);
  });

  it('marca a área atual com aria-current', async () => {
    const { el } = await montar(null, '/conta/favoritos');
    const atual = el.querySelector('nav.side a[aria-current="page"]');
    expect(atual?.getAttribute('href')).toBe('/conta/favoritos');
  });

  it('avisa quem usa ao clicar em "Sair da conta"', async () => {
    const { fixture, el } = await montar(null);
    el.querySelector<HTMLButtonElement>('button.out')!.click();
    expect(fixture.componentInstance.saidas).toBe(1);
  });
});

describe('iniciais', () => {
  it('usa a primeira letra do primeiro e do último nome', () => {
    expect(iniciais('Marina Ribeiro')).toBe('MR');
    expect(iniciais(' ana  maria souza ')).toBe('AS');
    expect(iniciais('Davi')).toBe('D');
    expect(iniciais('')).toBe('');
  });
});
