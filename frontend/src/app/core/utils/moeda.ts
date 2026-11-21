const BRL = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

/** Valor em reais ("R$ 1.234,56"); null vira traço. */
export function reais(valor: number | null | undefined): string {
  return valor == null ? '—' : BRL.format(valor);
}
