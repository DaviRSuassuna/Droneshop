import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { ContaFolha } from './conta-folha';

function montar() {
  TestBed.configureTestingModule({ imports: [ContaFolha], providers: [provideRouter([])] });
  const fixture = TestBed.createComponent(ContaFolha);
  fixture.componentRef.setInput('conta', {
    nome: 'Marina Ribeiro',
    detalhe: 'marina@exemplo.com',
    contagens: { pedidos: 12 },
  });
  fixture.detectChanges();
  const el = fixture.nativeElement as HTMLElement;
  const dialogo = el.querySelector('dialog.cs') as HTMLDialogElement;
  // jsdom não implementa showModal/close: simula o estado "open"
  dialogo.showModal = vi.fn(() => dialogo.setAttribute('open', ''));
  dialogo.close = vi.fn(() => dialogo.removeAttribute('open'));
  return { fixture, el, dialogo };
}

describe('ContaFolha', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    vi.stubGlobal('requestAnimationFrame', (f: FrameRequestCallback) => setTimeout(f, 0));
  });
  afterEach(() => {
    vi.useRealTimers();
    vi.unstubAllGlobals();
  });

  it('lista quem está logado, as áreas com contagem e o "Sair da conta"', () => {
    const { el } = montar();
    expect(el.querySelector('.quem b')?.textContent).toBe('Marina Ribeiro');
    expect(el.querySelector('.quem small')?.textContent).toBe('marina@exemplo.com');
    expect(el.querySelectorAll('dialog a').length).toBe(5);
    expect(el.querySelector('dialog a em')?.textContent).toBe('12');
    expect(el.querySelector('.sair')?.textContent).toContain('Sair da conta');
  });

  it('abre como modal e sobe a folha no quadro seguinte', () => {
    const { fixture, dialogo } = montar();
    fixture.componentInstance.abrir();
    expect(dialogo.showModal).toHaveBeenCalled();
    vi.runAllTimers();
    expect(dialogo.classList.contains('on')).toBe(true);
  });

  it('fecha pela barra, pelo fundo e pelo Esc, depois da animação', () => {
    const { fixture, el, dialogo } = montar();
    fixture.componentInstance.abrir();
    vi.runAllTimers();

    el.querySelector<HTMLButtonElement>('.bar')!.click();
    expect(dialogo.classList.contains('on')).toBe(false);
    vi.advanceTimersByTime(320);
    expect(dialogo.close).toHaveBeenCalledTimes(1);

    fixture.componentInstance.abrir();
    vi.runAllTimers();
    dialogo.dispatchEvent(new MouseEvent('click', { bubbles: true }));
    vi.advanceTimersByTime(320);
    expect(dialogo.close).toHaveBeenCalledTimes(2);

    fixture.componentInstance.abrir();
    vi.runAllTimers();
    const esc = new Event('cancel', { cancelable: true });
    dialogo.dispatchEvent(esc);
    expect(esc.defaultPrevented).toBe(true);
    vi.advanceTimersByTime(320);
    expect(dialogo.close).toHaveBeenCalledTimes(3);
  });

  it('fecha ao arrastar para baixo', () => {
    const { fixture, dialogo } = montar();
    fixture.componentInstance.abrir();
    vi.runAllTimers();
    const toque = (tipo: string, y: number) => {
      const e = new Event(tipo, { bubbles: true });
      Object.defineProperty(e, 'touches', { value: [{ clientY: y }] });
      dialogo.dispatchEvent(e);
    };
    toque('touchstart', 100);
    toque('touchmove', 140);
    expect(dialogo.classList.contains('on')).toBe(true);
    toque('touchmove', 180);
    expect(dialogo.classList.contains('on')).toBe(false);
  });

  it('"Sair da conta" fecha e avisa quem usa', () => {
    const { fixture, el } = montar();
    let saiu = false;
    fixture.componentInstance.sair.subscribe(() => (saiu = true));
    el.querySelector<HTMLButtonElement>('.sair')!.click();
    expect(saiu).toBe(true);
  });
});
