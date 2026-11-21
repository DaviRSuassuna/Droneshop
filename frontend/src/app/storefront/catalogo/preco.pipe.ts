import { Pipe, PipeTransform } from '@angular/core';

const formato = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
  maximumFractionDigits: 2,
  minimumFractionDigits: 0,
});

/** Preço da vitrine como no protótipo: "R$ 12.499" (centavos só quando existem). */
@Pipe({ name: 'preco' })
export class PrecoPipe implements PipeTransform {
  transform(valor: number | null | undefined): string {
    return valor === null || valor === undefined ? '' : formato.format(valor);
  }
}
